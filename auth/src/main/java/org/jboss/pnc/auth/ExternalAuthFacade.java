package org.jboss.pnc.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jboss.logging.Logger;
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
    
    public ExternalAuthFacade(String username, String password, InputStream keycloakConfiguration, String baseRestURL) throws Exception{
        // get credentials as parameter
        this.pnc_ext_oauth_username = username;
        this.pnc_ext_oauth_password = password;
        
        if(this.pnc_ext_oauth_password == null || 
            this.pnc_ext_oauth_username == null ||
            this.pnc_ext_oauth_password.equals("") ||
            this.pnc_ext_oauth_username.equals("")) {
            
            // try to get credentials from System env 
            this.pnc_ext_oauth_username = System.getenv("PNC_EXT_OAUTH_USERNAME");
            this.pnc_ext_oauth_password = System.getenv("PNC_EXT_OAUTH_PASSWORD");
            
            if(this.pnc_ext_oauth_password == null || 
                    this.pnc_ext_oauth_username == null ||
                    this.pnc_ext_oauth_password.equals("") ||
                    this.pnc_ext_oauth_username.equals("")) {
                     throw new Exception("Credentials were not provided! + \n"
                             + "Either, provide those passing ExternalAuthFacade(String username, String password,...) + \n"
                             + "or provide those as System env props + \n +"
                             + "like export PNC_EXT_OAUTH_USERNAME=your_username + \n +"
                             + "export PNC_EXT_OAUTH_PASSWORD=your_password");
                 }
        }
        // check keycloakConfig
        this.keycloakConfiguration = keycloakConfiguration;
        if(this.keycloakConfiguration == null) {
            throw new Exception("Keycloak configuration were not provided");
        }
        //check base REST URL 
        this.baseRestURL = baseRestURL;
        if(this.baseRestURL == null || this.baseRestURL.equals("")) {
            this.baseRestURL = System.getenv("PNC_EXT_REST_BASE_URL");
            if(this.baseRestURL == null || this.baseRestURL.equals("")) { 
                throw new Exception("Basic URL of REST endpoints were not provided + \n" 
                            + "Either, provide this one passing ExternalAuthFacade(...,String baseRestURL) + \n"
                            + "or provide those as System env property + \n +"
                            + "like export PNC_EXT_REST_BASE_URL=basic_rest_endpoint + \n +"
                            + "Example: export PNC_EXT_REST_BASE_URL=http://localhost:8080/pnc-rest/rest");
            }
        }
    }

    /**
     * @param endpoint Must be in the form of /customer or /product/id
     * @return InputStream for the endpoint
     * @throws Exception if an error occurs
     */
    public InputStream restEndpoint(String endpoint) throws Exception {
        // obtain AccessToken first
        ExternalAuthentication externalAuthentication = new ExternalAuthentication(keycloakConfiguration);
        AuthenticationProvider provider = externalAuthentication.authenticate(this.pnc_ext_oauth_username,this.pnc_ext_oauth_password);
        
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
