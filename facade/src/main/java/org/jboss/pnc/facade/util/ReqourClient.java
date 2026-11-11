/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.jboss.pnc.api.reqour.dto.TranslateRequest;
import org.jboss.pnc.api.reqour.dto.TranslateResponse;
import org.jboss.pnc.api.reqour.rest.ScmContentEndpoint;
import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Dependent
@Slf4j
public class ReqourClient {

    private static final String TRANSLATE_ENDPOINT = "/external-to-internal";

    @Inject
    private GlobalModuleGroup globalModuleGroupConfiguration;

    @Inject
    private ServiceAccountClient serviceAccountClient;

    private final Client client = ClientBuilder.newClient();

    /** Archives and diffs are streamed and can take minutes (the routes allow 10); created on first use. */
    private Client contentClient;

    public String translateExternalUrl(String externalUrl) {
        try {
            TranslateRequest request = TranslateRequest.builder().externalUrl(externalUrl).build();
            TranslateResponse response = client.target(globalModuleGroupConfiguration.getExternalReqourUrl())
                    .path(TRANSLATE_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, serviceAccountClient.getAuthHeaderValue())
                    .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), TranslateResponse.class);
            if (StringUtils.isEmpty(response.getInternalUrl())) {
                throw new RuntimeException("Failed to read translation of external URL to internal one.");
            }
            return response.getInternalUrl();
        } catch (ProcessingException ex) {
            throw new RuntimeException("Failed to translate external URL to internal one.", ex);
        }
    }

    /**
     * The source archive of the first existing revision of an internal repository. The caller streams the response
     * entity and closes the response.
     */
    public Response scmArchive(String url, List<String> revisions) {
        return get(
                contentTarget(withValues(contentUri("/scm/archive", url), "revision", revisions)),
                ScmContentEndpoint.GZIP);
    }

    /**
     * The complete alignment diff of an internal repository. The caller streams the response entity and closes the
     * response.
     */
    public Response scmDiff(String url, List<String> bases, List<String> heads) {
        return get(
                contentTarget(withValues(withValues(contentUri("/scm/diff", url), "base", bases), "head", heads)),
                ScmContentEndpoint.DIFF);
    }

    private UriBuilder contentUri(String path, String url) {
        return withValues(
                UriBuilder.fromUri(globalModuleGroupConfiguration.getExternalReqourUrl()).path(path),
                "url",
                List.of(url));
    }

    private WebTarget contentTarget(UriBuilder uri) {
        return contentClient().target(uri);
    }

    private synchronized Client contentClient() {
        if (contentClient == null) {
            contentClient = ClientBuilder.newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build();
        }
        return contentClient;
    }

    /**
     * Adds the query values through resolved templates, which encodes them completely: a value given directly to
     * queryParam would be read as a template if it contained braces.
     */
    static UriBuilder withValues(UriBuilder uri, String name, List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            String template = name + i;
            uri = uri.queryParam(name, "{" + template + "}").resolveTemplate(template, values.get(i));
        }
        return uri;
    }

    @PreDestroy
    synchronized void close() {
        client.close();
        if (contentClient != null) {
            contentClient.close();
        }
    }

    private Response get(WebTarget target, String mediaType) {
        try {
            return target.request(mediaType, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, serviceAccountClient.getAuthHeaderValue())
                    .get();
        } catch (ProcessingException ex) {
            throw unreachable(target.getUri().getPath(), ex);
        }
    }

    /**
     * A call that got no answer from reqour: 504 when it timed out, 502 otherwise (connection refused, unknown host,
     * TLS...), answered as an {@code ErrorResponse} by the REST exception mappers.
     */
    static WebApplicationException unreachable(String path, ProcessingException ex) {
        boolean timeout = false;
        for (Throwable cause = ex; cause != null && !timeout; cause = cause.getCause()) {
            timeout = cause instanceof SocketTimeoutException;
        }
        log.warn("No answer from reqour at {}", path, ex);
        Response.Status status = timeout ? Response.Status.GATEWAY_TIMEOUT : Response.Status.BAD_GATEWAY;
        String message = timeout ? "Reqour did not answer in time." : "Reqour cannot be reached.";
        return new WebApplicationException(message, ex, status);
    }
}
