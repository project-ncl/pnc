package org.jboss.pnc.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.RSATokenVerifier;
import org.keycloak.VerificationException;
import org.keycloak.adapters.AuthChallenge;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.installed.KeycloakInstalled;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.KeycloakUriBuilder;

public class ExternalAuthentication {
    
    protected Logger log = Logger.getLogger(ExternalAuthentication.class);
    private KeycloakDeployment keycloakDeployment;
    protected AuthChallenge challenge;
    
    
    
    public ExternalAuthentication(InputStream keycloakConfiguration) {
        keycloakDeployment = new KeycloakInstalled(keycloakConfiguration).getDeployment();
    }
    
    public AccessToken authenticate(String username, String password) throws IOException{
        return authenticateToken(authenticateUser(username, password).getToken());
    }
    
    protected AccessTokenResponse authenticateUser(String username, String password) throws IOException{
        HttpClientBuilder clientbuilder = new HttpClientBuilder();
        HttpClient client = clientbuilder.disableTrustManager().build();
        
        log.info(">>> keycloakDeployment.getAuthServerBaseUrl():" + keycloakDeployment.getAuthServerBaseUrl());
        log.info(">>> keycloakDeployment.getRealm():" + keycloakDeployment.getRealm());
        log.info(">>> keycloakDeployment.getResourceName():" + keycloakDeployment.getResourceName());
        
            HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakDeployment.getAuthServerBaseUrl())
                    .path(ServiceUrlConstants.TOKEN_SERVICE_DIRECT_GRANT_PATH).build(keycloakDeployment.getRealm()));
            List <NameValuePair> formparams = new ArrayList <NameValuePair>();
            formparams.add(new BasicNameValuePair("username", username));
            formparams.add(new BasicNameValuePair("password", password));
            formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, keycloakDeployment.getResourceName()));
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);

            HttpResponse response = client.execute(post);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (status != 200) {
                String json = getContent(entity);
                throw new IOException("Bad status: " + status + " response: " + json);
            }
            if (entity == null) {
                throw new IOException("No Entity");
            }
            String json = getContent(entity);
            AccessTokenResponse accessTokenResponse =  JsonSerialization.readValue(json, AccessTokenResponse.class);
            log.info(">>> accessTokenResponse:" + accessTokenResponse.getToken());
            return accessTokenResponse;            
     }
    
    protected AccessToken authenticateToken(String tokenString) {
        try {
            AccessToken token = RSATokenVerifier.verifyToken(tokenString, keycloakDeployment.getRealmKey(), keycloakDeployment.getRealm());
            return token;
        } catch (VerificationException e) {
            log.error("Failed to verify token", e);
            return null;
        }
    }
    
    public static String getContent(HttpEntity entity) throws IOException {
        if (entity == null) return null;
        InputStream is = entity.getContent();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int c;
            while ((c = is.read()) != -1) {
                os.write(c);
            }
            byte[] bytes = os.toByteArray();
            String data = new String(bytes);
            return data;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {

            }
        }

    }    

}
