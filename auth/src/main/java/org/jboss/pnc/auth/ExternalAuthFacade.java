/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.keycloak.adapters.HttpClientBuilder;

/**
 * @author Pavel Slegr
 *
 */
public class ExternalAuthFacade {
    public final static Logger log = Logger.getLogger(ExternalAuthFacade.class);
    
    private String pnc_ext_oauth_username;
    private String pnc_ext_oauth_password;
    private InputStream keycloakConfiguration;
    private String baseRestURL;
    
    Configuration configuration = new Configuration();
    
    /**
     * Load configuration from config json file
     * 
     * @param keycloakConfiguration
     * @throws Exception
     */
    public ExternalAuthFacade(InputStream keycloakConfiguration) throws Exception{
        
        AuthenticationModuleConfig config = configuration
                .getModuleConfig(new PncConfigProvider<AuthenticationModuleConfig>(AuthenticationModuleConfig.class));
        this.pnc_ext_oauth_username = config.getUsername();
        this.pnc_ext_oauth_password = config.getPassword();
        this.baseRestURL = config.getBaseAuthUrl(); 

        if(this.pnc_ext_oauth_password == null || 
            this.pnc_ext_oauth_username == null ||
            this.pnc_ext_oauth_password.equals("") ||
            this.pnc_ext_oauth_username.equals("") || 
            this.baseRestURL == null || 
            this.baseRestURL.equals("")) {
                throw new Exception("Wrong keycloak configuration");
        }
        // check keycloakConfig
        this.keycloakConfiguration = keycloakConfiguration;
        if(this.keycloakConfiguration == null) {
            throw new Exception("Keycloak configuration were not provided");
        }
    }
    
    /**
     * Provide configuration properties directly 
     * 
     * @param username
     * @param password
     * @param keycloakConfiguration
     * @param baseRestURL
     * @throws Exception
     */
    ExternalAuthFacade(String username, String password, InputStream keycloakConfiguration, String baseRestURL) throws Exception{
        // get credentials as parameter
        this.pnc_ext_oauth_username = username;
        this.pnc_ext_oauth_password = password;
        
        if(this.pnc_ext_oauth_password == null || 
            this.pnc_ext_oauth_username == null ||
            this.pnc_ext_oauth_password.equals("") ||
            this.pnc_ext_oauth_username.equals("")) {
            throw new Exception("Credentials were not provided! + \n"
                     + "Provide those passing ExternalAuthFacade(String username, String password,...) + \n");
        }
        
        // check keycloakConfig
        this.keycloakConfiguration = keycloakConfiguration;
        if(this.keycloakConfiguration == null) {
            throw new Exception("Keycloak configuration were not provided");
        }
        //check base REST URL 
        this.baseRestURL = baseRestURL;
        if(this.baseRestURL == null || this.baseRestURL.equals("")) {
            throw new Exception("Basic URL of REST endpoints were not provided + \n" 
                        + "Provide this one passing ExternalAuthFacade(...,String baseRestURL) + \n");
        }
    }
    

    /**
     * @param endpoint Must be in the form of /customers or /products/id
     * @return InputStream for the endpoint
     * @throws Exception if an error occurs
     */
    public InputStream restEndpoint(String endpoint) throws Exception {
        // obtain AccessToken first
        ExternalAuthentication externalAuthentication = new ExternalAuthentication(keycloakConfiguration);
        AuthenticationProvider provider = externalAuthentication.authenticate(this.pnc_ext_oauth_username,this.pnc_ext_oauth_password);
        if(provider == null) {
            throw new Exception("Invalid authentication");
        }
        
        HttpClient client = new HttpClientBuilder()
        .disableTrustManager().build();        
        HttpGet get = new HttpGet(this.baseRestURL + endpoint);
        get.setHeader("Accept", "application/json");
        get.setHeader("Authorization", "Bearer " + provider.getTokenString());
        
        HttpResponse response = client.execute(get);
        log.debug(">>> status code: " + response.getStatusLine().getStatusCode());
        if (response.getStatusLine().getStatusCode() == 200) {
            return response.getEntity().getContent();
        } else {
            throw new Exception(response.getStatusLine().toString());
        }
        
        
    } 

    public static void print(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (String l = br.readLine(); l != null; l = br.readLine()) {
            log.info(l);
        }
    }    
    
}
