/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
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

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.pnc.api.reqour.rest.ScmContentEndpoint;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.ScmContentSource;
import org.jboss.pnc.facade.util.ReqourClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BuildEndpointImplScmContentTest {

    private static final String BUILD_URL = "git@github.ibm.com:pnc-prod/repo.git";
    private static final String CURRENT_URL = "git@gitlab.cee.redhat.com:pnc-workspace/repo.git";

    @Mock
    private BuildProvider provider;

    @Mock
    private ReqourClient reqourClient;

    @InjectMocks
    private BuildEndpointImpl endpoint;

    private final ScmContentSource source = ScmContentSource.builder()
            .urls(List.of(BUILD_URL, CURRENT_URL))
            .revisions(List.of("aligned"))
            .bases(List.of("upstream"))
            .heads(List.of("aligned", "1.0.0.redhat-00001"))
            .build();

    @Test
    public void streamsTheDiffWithReqourHeaders() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response reqour = ok("diff --git a/x b/x\n", "text/x-diff");
        when(reqour.getHeaderString(ScmContentEndpoint.DIFF_MODE_HEADER)).thenReturn("compare");
        when(reqour.getHeaderString(ScmContentEndpoint.DIFF_BASE_HEADER)).thenReturn("upstream");
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads())).thenReturn(reqour);

        Response response = endpoint.getAlignmentDiff("B1");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaderString(ScmContentEndpoint.DIFF_MODE_HEADER)).isEqualTo("compare");
        assertThat(response.getHeaderString(ScmContentEndpoint.DIFF_BASE_HEADER)).isEqualTo("upstream");
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"build-B1-alignment.diff\"");
        assertThat(body(response)).isEqualTo("diff --git a/x b/x\n");
        verify(reqour).close();
        verify(reqourClient, never()).scmDiff(eq(CURRENT_URL), anyList(), anyList());
    }

    @Test
    public void triesTheCurrentInternalUrlWhenTheBuildOneIsNotServed() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response notServed = error(400, "{\"errorMessage\":\"not internal\"}");
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions())).thenReturn(notServed);
        Response archive = ok("tgz", "application/gzip");
        when(archive.getHeaderString(ScmContentEndpoint.REVISION_HEADER)).thenReturn("aligned");
        when(reqourClient.scmArchive(CURRENT_URL, source.getRevisions())).thenReturn(archive);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaderString(ScmContentEndpoint.REVISION_HEADER)).isEqualTo("aligned");
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"build-B1-sources.tar.gz\"");
        assertThat(body(response)).isEqualTo("tgz");
        verify(notServed).close();
    }

    @Test
    public void passesOnTheLastErrorAndHidesReqourAuthErrors() {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response notFound = error(404, "{\"errorMessage\":\"no revision\"}");
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions())).thenReturn(notFound);
        Response unauthorized = error(401, "{\"errorMessage\":\"who are you\"}");
        when(reqourClient.scmArchive(CURRENT_URL, source.getRevisions())).thenReturn(unauthorized);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThat(response.getStatus()).isEqualTo(502);
        assertThat(response.getEntity()).isEqualTo("{\"errorMessage\":\"who are you\"}");
    }

    @Test
    public void providerErrorsArePassedOn() {
        when(provider.getScmContentSource("B1")).thenReturn(source);
        Response badGateway = error(502, "{\"errorMessage\":\"HTTP 500 from gitlab\"}");
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads())).thenReturn(badGateway);

        Response response = endpoint.getAlignmentDiff("B1");

        assertThat(response.getStatus()).isEqualTo(502);
        verify(reqourClient, never()).scmDiff(eq(CURRENT_URL), anyList(), anyList());
    }

    @Test
    public void nonJsonErrorsAreWrappedAndUnknownStatusesAreGatewayErrors() {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        Response gateway = error(503, "<html>Service Unavailable</html>", MediaType.TEXT_HTML_TYPE);
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions())).thenReturn(gateway);

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
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads())).thenReturn(busy);

        Response response = endpoint.getAlignmentDiff("B1");

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeaderString(HttpHeaders.RETRY_AFTER)).isEqualTo("30");
        assertThat(response.getEntity()).isEqualTo("{\"errorMessage\":\"Already building 4 diffs\"}");
        verify(reqourClient, never()).scmDiff(eq(CURRENT_URL), anyList(), anyList());
    }

    @Test
    public void providerTimeoutsArePassedOn() {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        Response timeout = error(504, "{\"errorMessage\":\"timeout\"}");
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads())).thenReturn(timeout);

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
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions())).thenReturn(broken);

        Response response = endpoint.getInternalScmArchive("B1");

        assertThatThrownBy(() -> body(response)).isInstanceOf(IOException.class);
        verify(broken).close();
        assertThat(availablePermits()).isEqualTo(BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT);
    }

    @Test
    public void concurrentDownloadsAreLimited() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions()))
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
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions()))
                .thenAnswer(invocation -> error(404, "{}"));

        for (int i = 0; i < BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT + 2; i++) {
            assertThat(endpoint.getInternalScmArchive("B1").getStatus()).isEqualTo(404);
        }
        assertThat(availablePermits()).isEqualTo(BuildEndpointImpl.MAX_CONCURRENT_SCM_CONTENT);
    }

    @Test
    public void requestsLeaveNoSafetyTaskBehind() throws Exception {
        when(provider.getScmContentSource("B1")).thenReturn(single());
        when(reqourClient.scmArchive(BUILD_URL, source.getRevisions())).thenAnswer(invocation -> error(404, "{}"));
        when(reqourClient.scmDiff(BUILD_URL, source.getBases(), source.getHeads()))
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
