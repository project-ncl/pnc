package org.jboss.pnc.auth;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.keycloak.representations.AccessToken;

public class ExternalAuthenticationTest {

    @Test
    public void testDAG() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            AuthenticationProvider provider = ea.authenticate(System.getenv("PNC_EXT_OAUTH_USERNAME"), System.getenv("PNC_EXT_OAUTH_PASSWORD"));
            assertNotNull(provider);
            assertNotNull(provider.getPrefferedUserName());
            
        } catch (IOException e) {
            // do nothing
        }
    }

}
