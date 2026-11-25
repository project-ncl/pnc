/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.pnc.api.reqour.rest.ScmContentEndpoint;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.ScmContentSource;
import org.jboss.pnc.facade.util.ReqourClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.wildfly.security.http.oidc.OidcSecurityContext;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BuildEndpointImplScmContentTest {

    private static final String BUILD_URL = "git@github.ibm.com:pnc-prod/repo.git";
    private static final String CURRENT_URL = "git@gitlab.cee.redhat.com:pnc-workspace/repo.git";

    @Mock
    private BuildProvider provider;

    private static final String SERVICE_ACCOUNT = "Bearer orch-service-account";

    @Mock
    private ReqourClient reqourClient;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private BuildEndpointImpl endpoint;

    private final ScmContentSource source = ScmContentSource.builder()
            .urls(List.of(BUILD_URL, CURRENT_URL))
            .revisions(List.of("aligned"))
            .bases(List.of("upstream"))
            .heads(List.of("aligned", "1.0.0.redhat-00001"))
            .build();

    @Before
    public void callersWithoutAnOidcTokenUseTheServiceAccount() {
        when(reqourClient.contentAuthorization(Optional.empty())).thenReturn(SERVICE_ACCOUNT);
    }

    private void callerToken(String token) {
        when(httpServletRequest.getAttribute(OidcSecurityContext.class.getName()))
                .thenReturn(new OidcSecurityContext(token, null, null, null));
        when(reqourClient.contentAuthorization(Optional.of(token))).thenReturn("Bearer " + token);
    }

    @Test
    public void theCallersOwnTokenIsSentToReqour() throws Exception {
        callerToken("caller-token");
        when(provider.getScmContentSource("B1")).thenReturn(single());
        Response archive = ok("tgz", "application/gzip");
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), "Bearer caller-token")).thenReturn(archive);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body(response)).isEqualTo("tgz");
        verify(reqourClient, never()).contentAuthorization(Optional.empty());
    }

    @Test
    public void aCallerReqourForbidsIsForbidden() {
        callerToken("caller-token");
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response forbidden = error(403, "{\"errorMessage\":\"needs pnc-users\"}");
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads(), "Bearer caller-token"))
                .thenReturn(forbidden);

        Response response = endpoint.getAlignmentDiff("B1");

        assertThat(response.getStatus()).isEqualTo(403);
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertThat(error.getErrorType()).isEqualTo("ForbiddenException");
        assertThat(error.getErrorMessage())
                .isEqualTo("Reqour does not allow the caller to read SCM content (HTTP 403).");
        assertThat(error.getDetails()).isEqualTo("{\"errorMessage\":\"needs pnc-users\"}");
        verify(forbidden).close();
        verify(reqourClient, never()).scmDiff(eq(CURRENT_URL), anyList(), anyList(), anyString());
    }

    @Test
    public void aCallerTokenReqourDoesNotAcceptIsAGatewayError() {
        callerToken("caller-token");
        when(provider.getScmContentSource("B1")).thenReturn(single());
        Response unauthorized = error(401, "");
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), "Bearer caller-token"))
                .thenReturn(unauthorized);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThat(response.getStatus()).isEqualTo(502);
        assertThat(((ErrorResponse) response.getEntity()).getErrorMessage())
                .isEqualTo(
                        "Reqour does not accept the caller's token (HTTP 401): it may have expired, or been issued by an"
                                + " identity provider reqour does not trust.");
    }

    @Test
    public void serviceAccountTokenFailuresAreGatewayErrors() {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        when(reqourClient.contentAuthorization(any())).thenThrow(
                new WebApplicationException("no token", Response.Status.BAD_GATEWAY));

        assertThatThrownBy(() -> endpoint.getInternalScmArchive("B1"))
                .isInstanceOfSatisfying(
                        WebApplicationException.class,
                        e -> assertThat(e.getResponse().getStatus()).isEqualTo(502));
        assertThat(availablePermitsUnchecked()).isEqualTo(BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT);
    }

    @Test
    public void requestsOverTheConcurrencyLimitDoNotFetchATokenFirst() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), SERVICE_ACCOUNT))
                .thenAnswer(invocation -> ok("tgz", "application/gzip"));
        for (int i = 0; i < BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT; i++) {
            endpoint.getInternalScmArchive("B1");
        }

        assertThat(endpoint.getInternalScmArchive("B1").getStatus()).isEqualTo(503);
        verify(reqourClient, org.mockito.Mockito.times(BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT))
                .contentAuthorization(Optional.empty());
    }

    @Test
    public void streamsTheDiffWithReqourHeaders() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response reqour = ok("diff --git a/x b/x\n", "text/x-diff");
        when(reqour.getHeaderString(ScmContentEndpoint.DIFF_MODE_HEADER)).thenReturn("compare");
        when(reqour.getHeaderString(ScmContentEndpoint.DIFF_BASE_HEADER)).thenReturn("upstream");
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads(), SERVICE_ACCOUNT)).thenReturn(reqour);

        Response response = endpoint.getAlignmentDiff("B1");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaderString(ScmContentEndpoint.DIFF_MODE_HEADER)).isEqualTo("compare");
        assertThat(response.getHeaderString(ScmContentEndpoint.DIFF_BASE_HEADER)).isEqualTo("upstream");
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"build-B1-alignment.diff\"");
        assertThat(body(response)).isEqualTo("diff --git a/x b/x\n");
        verify(reqour).close();
        verify(reqourClient, never()).scmDiff(eq(CURRENT_URL), anyList(), anyList(), anyString());
    }

    @Test
    public void triesTheCurrentInternalUrlWhenTheBuildOneIsNotServed() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response notServed = error(400, "{\"errorMessage\":\"not internal\"}");
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), SERVICE_ACCOUNT)).thenReturn(notServed);
        Response archive = ok("tgz", "application/gzip");
        when(archive.getHeaderString(ScmContentEndpoint.REVISION_HEADER)).thenReturn("aligned");
        when(reqourClient.scmArchive(CURRENT_URL, source.getRevisions(), SERVICE_ACCOUNT)).thenReturn(archive);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaderString(ScmContentEndpoint.REVISION_HEADER)).isEqualTo("aligned");
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"build-B1-sources.tar.gz\"");
        assertThat(body(response)).isEqualTo("tgz");
        verify(notServed).close();
    }

    @Test
    public void reqourRefusingTheServiceAccountIsExplained() {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response notFound = error(404, "{\"errorMessage\":\"no revision\"}");
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), SERVICE_ACCOUNT)).thenReturn(notFound);
        Response unauthorized = error(401, "{\"errorMessage\":\"who are you\"}");
        when(reqourClient.scmArchive(CURRENT_URL, source.getRevisions(), SERVICE_ACCOUNT)).thenReturn(unauthorized);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThat(response.getStatus()).isEqualTo(502);
        assertThat(((ErrorResponse) response.getEntity()).getErrorMessage()).isEqualTo(
                "Reqour refused the service account of orch (HTTP 401): log in with an OIDC token to download sources"
                        + " and diffs through PNC.");
        verify(unauthorized).close();
    }

    @Test
    public void providerErrorsArePassedOn() {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response badGateway = error(502, "{\"errorMessage\":\"HTTP 500 from gitlab\"}");
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads(), SERVICE_ACCOUNT))
                .thenReturn(badGateway);

        Response response = endpoint.getAlignmentDiff("B1");

        assertThat(response.getStatus()).isEqualTo(502);
        verify(reqourClient, never()).scmDiff(eq(CURRENT_URL), anyList(), anyList(), anyString());
    }

    @Test
    public void nonJsonErrorsAreWrappedAndUnknownStatusesAreGatewayErrors() {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        Response gateway = error(503, "<html>Service Unavailable</html>", MediaType.TEXT_HTML_TYPE);
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), SERVICE_ACCOUNT)).thenReturn(gateway);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThat(response.getStatus()).isEqualTo(502);
        assertThat(response.getEntity()).isInstanceOf(ErrorResponse.class);
        assertThat(((ErrorResponse) response.getEntity()).getErrorMessage()).isEqualTo("Reqour answered HTTP 503.");
        verify(gateway).close();
    }

    @Test
    public void reqourBusyAnswersArePassedOnWithRetryAfter() {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response busy = error(503, "{\"errorMessage\":\"Already building 4 diffs\"}");
        when(busy.getHeaderString(HttpHeaders.RETRY_AFTER)).thenReturn("30");
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads(), SERVICE_ACCOUNT)).thenReturn(busy);

        Response response = endpoint.getAlignmentDiff("B1");

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeaderString(HttpHeaders.RETRY_AFTER)).isEqualTo("30");
        assertThat(response.getEntity()).isEqualTo("{\"errorMessage\":\"Already building 4 diffs\"}");
        verify(reqourClient, never()).scmDiff(eq(CURRENT_URL), anyList(), anyList(), anyString());
    }

    @Test
    public void providerTimeoutsArePassedOn() {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        Response timeout = error(504, "{\"errorMessage\":\"timeout\"}");
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads(), SERVICE_ACCOUNT))
                .thenReturn(timeout);

        assertThat(endpoint.getAlignmentDiff("B1").getStatus()).isEqualTo(504);
    }

    @Test
    public void aFailedTransferClosesTheResponseAndFreesThePermit() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        Response broken = mock(Response.class);
        when(broken.getStatus()).thenReturn(200);
        when(broken.getMediaType()).thenReturn(MediaType.valueOf("application/gzip"));
        InputStream failing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("reqour went away");
            }
        };
        when(broken.readEntity(InputStream.class)).thenReturn(failing);
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), SERVICE_ACCOUNT)).thenReturn(broken);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThatThrownBy(() -> body(response)).isInstanceOf(IOException.class);
        verify(broken).close();
        assertThat(availablePermits()).isEqualTo(BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT);
    }

    @Test
    public void concurrentDownloadsAreLimited() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), SERVICE_ACCOUNT))
                .thenAnswer(invocation -> ok("tgz", "application/gzip"));

        List<Response> streaming = new java.util.ArrayList<>();
        for (int i = 0; i < BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT; i++) {
            streaming.add(endpoint.getInternalScmArchive("B1"));
        }
        Response rejected = endpoint.getInternalScmArchive("B1");
        assertThat(rejected.getStatus()).isEqualTo(503);
        assertThat(rejected.getHeaderString("Retry-After")).isEqualTo("30");

        body(streaming.get(0));
        assertThat(endpoint.getInternalScmArchive("B1").getStatus()).isEqualTo(200);
    }

    @Test
    public void errorsFreeThePermit() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), SERVICE_ACCOUNT))
                .thenAnswer(invocation -> error(404, "{}"));

        for (int i = 0; i < BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT + 2; i++) {
            assertThat(endpoint.getInternalScmArchive("B1").getStatus()).isEqualTo(404);
        }
        assertThat(availablePermits()).isEqualTo(BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT);
    }

    @Test
    public void requestsLeaveNoSafetyTaskBehind() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions(), SERVICE_ACCOUNT))
                .thenAnswer(invocation -> error(404, "{}"));
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads(), SERVICE_ACCOUNT))
                .thenAnswer(invocation -> ok("diff", "text/x-diff"));
        int queued = BuildEndpointImpl.PERMIT_RELEASER.getQueue().size();

        for (int i = 0; i < 3; i++) {
            endpoint.getInternalScmArchive("B1");
            body(endpoint.getAlignmentDiff("B1"));
        }

        assertThat(BuildEndpointImpl.PERMIT_RELEASER.getQueue()).hasSize(queued);
    }

    @Test
    public void aResponseThatIsNeverWrittenIsClosedAndFreesItsPermit() throws Exception {
        java.util.concurrent.Semaphore permits = new java.util.concurrent.Semaphore(0);
        BuildEndpointImpl.Permit permit = new BuildEndpointImpl.Permit(
                permits,
                10,
                java.util.concurrent.TimeUnit.MILLISECONDS);
        Response reqour = ok("tgz", "application/gzip");
        permit.pending(reqour);

        assertThat(permits.tryAcquire(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        verify(reqour).close();
        assertThat(permit.start()).isFalse();
    }

    @Test
    public void aLongTransferKeepsItsPermit() throws Exception {
        java.util.concurrent.Semaphore permits = new java.util.concurrent.Semaphore(0);
        BuildEndpointImpl.Permit permit = new BuildEndpointImpl.Permit(
                permits,
                50,
                java.util.concurrent.TimeUnit.MILLISECONDS);

        assertThat(permit.start()).isTrue();
        Thread.sleep(300);
        assertThat(permits.availablePermits()).isZero();

        permit.release();
        permit.release();
        assertThat(permits.availablePermits()).isEqualTo(1);
    }

    private ScmContentSource single() {
        return ScmContentSource.builder()
                .urls(List.of(BUILD_URL))
                .revisions(source.getRevisions())
                .bases(source.getBases())
                .heads(source.getHeads())
                .build();
    }

    private int availablePermitsUnchecked() {
        try {
            return availablePermits();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private int availablePermits() throws Exception {
        java.lang.reflect.Field field = BuildEndpointImpl.class.getDeclaredField("scmContentPermits");
        field.setAccessible(true);
        return ((java.util.concurrent.Semaphore) field.get(endpoint)).availablePermits();
    }

    @Test
    public void buildsWithoutScmDataAreNotFound() {
        when(provider.getScmContentSource("B1")).thenReturn(
                ScmContentSource.builder()
                        .urls(List.of())
                        .revisions(List.of())
                        .bases(List.of())
                        .heads(List.of())
                        .build());

        assertThatThrownBy(() -> endpoint.getInternalScmArchive("B1")).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> endpoint.getAlignmentDiff("B1")).isInstanceOf(NotFoundException.class);
    }

    private static Response ok(String body, String mediaType) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getMediaType()).thenReturn(MediaType.valueOf(mediaType));
        when(response.readEntity(InputStream.class))
                .thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        return response;
    }

    private static Response error(int status, String body) {
        return error(status, body, MediaType.APPLICATION_JSON_TYPE);
    }

    private static Response error(int status, String body, MediaType type) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(status);
        when(response.getMediaType()).thenReturn(type);
        when(response.readEntity(String.class)).thenReturn(body);
        return response;
    }

    private static String body(Response response) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ((StreamingOutput) response.getEntity()).write(out);
        return out.toString(StandardCharsets.UTF_8);
    }
}
