/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.client;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.pnc.client.patch.PatchBase;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek
 */
public abstract class ClientBase<T> implements Closeable {

    private Logger logger = LoggerFactory.getLogger(ClientBase.class);

    protected final String BASE_PATH = "/pnc-rest";

    protected final String BASE_REST_PATH = BASE_PATH + "/v2";

    protected final Client client;

    protected final WebTarget target;

    protected T proxy;

    protected Configuration configuration;

    protected Class<T> iface;

    protected BearerAuthentication bearerAuthentication;

    protected ClientBase(Configuration configuration, Class<T> iface) {
        this.iface = iface;
        this.configuration = configuration;

        // Build base URL
        String baseUrl = configuration.getProtocol() + "://" + configuration.getHost()
                + (configuration.getPort() == null ? "" : ":" + configuration.getPort()) + BASE_REST_PATH;

        // Create the MicroProfile REST Client proxy with redirects enabled by default (NCL-3766)
        proxy = createProxy(baseUrl, true);

        // Also create a standard JAX-RS client for backward compatibility with patch/getInputStream methods
        client = ClientBuilder.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        client.register(JacksonProviderWithDateISO8601.class);
        client.register(new MdcToHeadersFilter(configuration.getMdcToHeadersMappings()));
        client.register(RequestLoggingFilter.class);

        target = client.target(baseUrl);

        Configuration.BasicAuth basicAuth = configuration.getBasicAuth();
        if (basicAuth != null) {
            target.register(new BasicAuthentication(basicAuth.getUsername(), basicAuth.getPassword()));
        } else if (bearerAuthentication != null) {
            target.register(bearerAuthentication);
        }
    }

    /**
     * Creates a MicroProfile REST Client proxy with the specified configuration.
     *
     * @param baseUrl the base URL for the REST client
     * @param followRedirects whether to follow HTTP redirects
     * @return the REST client proxy
     */
    private T createProxy(String baseUrl, boolean followRedirects) {
        URI baseUri = URI.create(baseUrl);

        // Use MicroProfile REST Client Builder (standard, portable across all runtimes)
        RestClientBuilder restClientBuilder = RestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS);

        // Register providers
        restClientBuilder.register(JacksonProviderWithDateISO8601.class);
        restClientBuilder.register(new MdcToHeadersFilter(configuration.getMdcToHeadersMappings()));
        restClientBuilder.register(RequestLoggingFilter.class);

        // Configure authentication
        Configuration.BasicAuth basicAuth = configuration.getBasicAuth();
        if (basicAuth != null) {
            restClientBuilder.register(new BasicAuthentication(basicAuth.getUsername(), basicAuth.getPassword()));
        } else {
            if (configuration.getBearerTokenSupplier() != null) {
                bearerAuthentication = new BearerAuthentication(configuration.getBearerTokenSupplier());
                restClientBuilder.register(bearerAuthentication);
            } else {
                String bearerToken = configuration.getBearerToken();
                if (bearerToken != null && !bearerToken.isEmpty()) {
                    bearerAuthentication = new BearerAuthentication(() -> bearerToken);
                    restClientBuilder.register(bearerAuthentication);
                }
            }
        }

        // Set redirect following using standard MicroProfile REST Client property (NCL-3766)
        // This is the standard property since MicroProfile REST Client 1.4
        restClientBuilder.followRedirects(followRedirects);

        // Build and return the proxy using MicroProfile REST Client
        return restClientBuilder.build(iface);
    }

    /**
     * Sets whether the client should follow HTTP redirects. This recreates the proxy with the new redirect setting.
     *
     * @param followRedirects true to follow redirects, false otherwise
     */
    public void setFollowRedirects(boolean followRedirects) {
        String baseUrl = configuration.getProtocol() + "://" + configuration.getHost()
                + (configuration.getPort() == null ? "" : ":" + configuration.getPort()) + BASE_REST_PATH;
        proxy = createProxy(baseUrl, followRedirects);
    }

    protected T getEndpoint() {
        return proxy;
    }

    RemoteCollectionConfig getRemoteCollectionConfig() {
        int pageSize = configuration.getPageSize();
        if (pageSize < 1) {
            pageSize = 100;
        }
        return new RemoteCollectionConfig(pageSize);
    }

    protected void setSortAndQuery(PageParameters pageParameters, Optional<String> sort, Optional<String> q) {
        sort.ifPresent(pageParameters::setSort);
        q.ifPresent(pageParameters::setQ);
    }

    public <S> S patch(String id, String jsonPatch, Class<S> clazz) throws RemoteResourceException {
        Path path = iface.getAnnotation(Path.class);
        WebTarget patchTarget;
        if (!path.value().equals("") && !path.value().equals("/")) {
            patchTarget = target.path(path.value() + "/" + id);
        } else {
            patchTarget = target.path(id);
        }

        logger.debug("Json patch: {}", jsonPatch);

        try {
            Response response = patchTarget.request()
                    .build(HttpMethod.PATCH, Entity.entity(jsonPatch, MediaType.APPLICATION_JSON_PATCH_JSON))
                    .invoke();

            // Follow redirects manually for portability (NCL-3766)
            response = followRedirects(response);

            // Check for error status codes and throw appropriate exception
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new WebApplicationException(response);
            }

            return response.readEntity(clazz);
        } catch (WebApplicationException e) {
            throw new RemoteResourceException(readErrorResponse(e), e);
        }
    }

    public InputStream getInputStream(String methodPath, String id) {
        Path path = iface.getAnnotation(Path.class);
        String interfacePath = path.value();
        WebTarget webTarget = target.path(interfacePath + methodPath).resolveTemplate("id", id);

        // Make the request
        Response response = webTarget.request().build(HttpMethod.GET).invoke();

        // Follow redirects manually for portability (NCL-3766)
        response = followRedirects(response);

        // Return the final response as InputStream
        return response.readEntity(InputStream.class);
    }

    /**
     * Manually follows HTTP redirects for portability across JAX-RS implementations. Follows up to 5 redirects to
     * prevent infinite loops.
     *
     * @param response the initial response
     * @return the final response after following redirects
     */
    private Response followRedirects(Response response) {
        int redirectCount = 0;
        int maxRedirects = 5;

        while (isRedirect(response.getStatus()) && redirectCount < maxRedirects) {
            String location = response.getHeaderString("Location");
            if (location == null) {
                break; // No location header, stop following redirects
            }

            response.close(); // Close the redirect response

            // Follow the redirect
            WebTarget redirectTarget = client.target(location);
            response = redirectTarget.request().build(HttpMethod.GET).invoke();
            redirectCount++;
        }

        return response;
    }

    private boolean isRedirect(int status) {
        return status >= 300 && status < 400;
    }

    public <S> S patch(String id, PatchBase patchBase) throws PatchBuilderException, RemoteResourceException {
        String jsonPatch = patchBase.getJsonPatch();
        try {
            return patch(id, jsonPatch, (Class<S>) patchBase.getClazz());
        } catch (WebApplicationException e) {
            throw new RemoteResourceException(readErrorResponse(e), e);
        }
    }

    protected ErrorResponse readErrorResponse(WebApplicationException ex) {
        Response response = ex.getResponse();
        try {
            if (response.hasEntity()) {
                return response.readEntity(ErrorResponse.class);
            }
        } catch (ProcessingException | IllegalStateException e) {
            logger.debug("Can't map response to ErrorResponse.", e);
        } catch (RuntimeException e) {
            logger.warn("Unexpected exception when trying to read ErrorResponse.", e);
        }
        return null;
    }

    protected boolean shouldRetryOn401(Exception e) {
        // Check if it's a 401 error and we have a token supplier to refresh
        if (e instanceof javax.ws.rs.WebApplicationException) {
            javax.ws.rs.WebApplicationException wae = (javax.ws.rs.WebApplicationException) e;
            int status = wae.getResponse().getStatus();

            if (status == 401 && configuration.getBearerTokenSupplier() != null && bearerAuthentication != null) {
                // Refresh the token for the retry
                bearerAuthentication.setTokenSupplier(configuration.getBearerTokenSupplier());
                return true;
            }
        }
        return false;
    }

    protected RemoteResourceException handleException(Exception e) {
        // If it's already a RemoteResourceException, don't wrap it again
        if (e instanceof RemoteResourceException) {
            return (RemoteResourceException) e;
        }

        // Check if it's a WebApplicationException (MicroProfile REST Client behavior)
        if (e instanceof javax.ws.rs.WebApplicationException) {
            javax.ws.rs.WebApplicationException wae = (javax.ws.rs.WebApplicationException) e;
            int status = wae.getResponse().getStatus();

            // Read error response BEFORE creating new exception (which consumes the response)
            ErrorResponse errorResponse = readErrorResponse(wae);

            // Handle 400 Bad Request - convert to BadRequestException for backward compatibility
            if (status == 400) {
                javax.ws.rs.BadRequestException badRequestException = new javax.ws.rs.BadRequestException(
                        wae.getResponse());
                return new RemoteResourceException(errorResponse, badRequestException);
            }
            // Handle 401 Unauthorized - convert to NotAuthorizedException for backward compatibility
            else if (status == 401) {
                // Convert WebApplicationException to NotAuthorizedException for backward compatibility
                javax.ws.rs.NotAuthorizedException notAuthException = new javax.ws.rs.NotAuthorizedException(
                        wae.getResponse());

                if (configuration.getBearerTokenSupplier() != null && bearerAuthentication != null) {
                    try {
                        // Refresh the token
                        bearerAuthentication.setTokenSupplier(configuration.getBearerTokenSupplier());
                        // Note: The actual retry would need to happen in the generated client code
                        // For now, we just wrap the exception
                        return new RemoteResourceException(errorResponse, notAuthException);
                    } catch (javax.ws.rs.WebApplicationException retryException) {
                        return new RemoteResourceException(readErrorResponse(retryException), retryException);
                    }
                } else {
                    return new RemoteResourceException(errorResponse, notAuthException);
                }
            }
            // Handle 403 Forbidden - convert to ForbiddenException for backward compatibility
            else if (status == 403) {
                javax.ws.rs.ForbiddenException forbiddenException = new javax.ws.rs.ForbiddenException(
                        wae.getResponse());
                return new RemoteResourceException(errorResponse, forbiddenException);
            }
            // Handle 404 Not Found - convert to NotFoundException for backward compatibility
            else if (status == 404) {
                javax.ws.rs.NotFoundException notFoundException = new javax.ws.rs.NotFoundException(wae.getResponse());
                return new RemoteResourceNotFoundException(notFoundException);
            }
            // Handle other 4xx client errors - convert to ClientErrorException for backward compatibility
            else if (status >= 400 && status < 500) {
                javax.ws.rs.ClientErrorException clientErrorException = new javax.ws.rs.ClientErrorException(
                        wae.getResponse());
                return new RemoteResourceException(errorResponse, clientErrorException);
            }
            // Handle all other WebApplicationExceptions (5xx server errors, etc.)
            else {
                return new RemoteResourceException(errorResponse, wae);
            }
        }
        // Handle NotAuthorizedException (already in correct format)
        else if (e instanceof javax.ws.rs.NotAuthorizedException) {
            javax.ws.rs.NotAuthorizedException notAuthException = (javax.ws.rs.NotAuthorizedException) e;
            if (configuration.getBearerTokenSupplier() != null && bearerAuthentication != null) {
                try {
                    bearerAuthentication.setTokenSupplier(configuration.getBearerTokenSupplier());
                    return new RemoteResourceException(readErrorResponse(notAuthException), notAuthException);
                } catch (javax.ws.rs.WebApplicationException retryException) {
                    return new RemoteResourceException(readErrorResponse(retryException), retryException);
                }
            } else {
                return new RemoteResourceException(readErrorResponse(notAuthException), notAuthException);
            }
        }
        // Handle NotFoundException (already in correct format)
        else if (e instanceof javax.ws.rs.NotFoundException) {
            return new RemoteResourceNotFoundException((javax.ws.rs.NotFoundException) e);
        }
        // Handle any other exceptions
        else {
            return new RemoteResourceException(e);
        }
    }

    @Override
    public void close() {
        client.close();
    }
}
