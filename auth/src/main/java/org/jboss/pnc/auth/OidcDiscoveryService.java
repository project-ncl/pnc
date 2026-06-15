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
package org.jboss.pnc.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for discovering OIDC provider endpoints using the OpenID Connect Discovery standard. Caches discovery
 * metadata to avoid repeated HTTP requests.
 *
 * @see <a href="https://openid.net/specs/openid-connect-discovery-1_0.html">OpenID Connect Discovery</a>
 */
@ApplicationScoped
public class OidcDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(OidcDiscoveryService.class);
    private static final String WELL_KNOWN_PATH = "/.well-known/openid-configuration";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, OidcProviderMetadata> metadataCache = new ConcurrentHashMap<>();

    /**
     * Discovers OIDC provider metadata from the issuer's well-known endpoint. Results are cached to avoid repeated HTTP
     * requests.
     *
     * @param issuerUrl The OIDC issuer URL (e.g., "https://keycloak.example.com/realms/myrealm")
     * @return The provider metadata, or null if discovery fails
     */
    public OidcProviderMetadata discover(String issuerUrl) {
        if (issuerUrl == null || issuerUrl.trim().isEmpty()) {
            logger.warn("Issuer URL is null or empty");
            return null;
        }

        // Check cache first
        OidcProviderMetadata cached = metadataCache.get(issuerUrl);
        if (cached != null) {
            logger.debug("Using cached OIDC metadata for issuer: {}", issuerUrl);
            return cached;
        }

        // Perform discovery
        try {
            String discoveryUrl = buildDiscoveryUrl(issuerUrl);
            logger.info("Discovering OIDC metadata from: {}", discoveryUrl);

            OidcProviderMetadata metadata = fetchMetadata(discoveryUrl);
            if (metadata != null) {
                // Cache the result
                metadataCache.put(issuerUrl, metadata);
                logger.info("Successfully discovered OIDC metadata for issuer: {}", issuerUrl);
            }
            return metadata;

        } catch (Exception e) {
            logger.error("Failed to discover OIDC metadata for issuer: " + issuerUrl, e);
            return null;
        }
    }

    /**
     * Clears the metadata cache. Useful for testing or when configuration changes.
     */
    public void clearCache() {
        metadataCache.clear();
        logger.info("OIDC metadata cache cleared");
    }

    private String buildDiscoveryUrl(String issuerUrl) {
        // Remove trailing slash if present
        String normalizedIssuer = issuerUrl.endsWith("/") ? issuerUrl.substring(0, issuerUrl.length() - 1) : issuerUrl;
        return normalizedIssuer + WELL_KNOWN_PATH;
    }

    private OidcProviderMetadata fetchMetadata(String discoveryUrl) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(discoveryUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.warn("Discovery endpoint returned non-200 status: {} for URL: {}", responseCode, discoveryUrl);
                return null;
            }

            try (InputStream inputStream = connection.getInputStream()) {
                JsonNode root = objectMapper.readTree(inputStream);
                return parseMetadata(root);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private OidcProviderMetadata parseMetadata(JsonNode root) {
        OidcProviderMetadata metadata = new OidcProviderMetadata();

        // Extract standard OIDC endpoints
        metadata.setIssuer(getTextValue(root, "issuer"));
        metadata.setAuthorizationEndpoint(getTextValue(root, "authorization_endpoint"));
        metadata.setTokenEndpoint(getTextValue(root, "token_endpoint"));
        metadata.setUserinfoEndpoint(getTextValue(root, "userinfo_endpoint"));
        metadata.setJwksUri(getTextValue(root, "jwks_uri"));
        metadata.setEndSessionEndpoint(getTextValue(root, "end_session_endpoint"));

        // Log discovered endpoints
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Discovered OIDC metadata: issuer={}, end_session_endpoint={}",
                    metadata.getIssuer(),
                    metadata.getEndSessionEndpoint());
        }

        return metadata;
    }

    private String getTextValue(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return node != null && node.isTextual() ? node.asText() : null;
    }

    /**
     * Represents OIDC provider metadata from the discovery endpoint.
     */
    public static class OidcProviderMetadata {
        private String issuer;
        private String authorizationEndpoint;
        private String tokenEndpoint;
        private String userinfoEndpoint;
        private String jwksUri;
        private String endSessionEndpoint;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getAuthorizationEndpoint() {
            return authorizationEndpoint;
        }

        public void setAuthorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
        }

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public void setTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
        }

        public String getUserinfoEndpoint() {
            return userinfoEndpoint;
        }

        public void setUserinfoEndpoint(String userinfoEndpoint) {
            this.userinfoEndpoint = userinfoEndpoint;
        }

        public String getJwksUri() {
            return jwksUri;
        }

        public void setJwksUri(String jwksUri) {
            this.jwksUri = jwksUri;
        }

        public String getEndSessionEndpoint() {
            return endSessionEndpoint;
        }

        public void setEndSessionEndpoint(String endSessionEndpoint) {
            this.endSessionEndpoint = endSessionEndpoint;
        }
    }
}
