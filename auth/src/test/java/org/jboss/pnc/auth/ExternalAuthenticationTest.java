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

import java.io.InputStream;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author pslegr
 *
 */
@Category(ContainerTest.class)
public class ExternalAuthenticationTest {
    
    protected Logger log = Logger.getLogger(ExternalAuthenticationTest.class);


    @Test
    public void testAccesTokenPropertiesReturned() {
        try {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(
                    new PncConfigProvider<AuthenticationModuleConfig>(AuthenticationModuleConfig.class));
            InputStream is = ExternalAuthenticationTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            AuthenticationProvider authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            String access_token = authProvider.getTokenString();
            Assert.assertTrue(access_token.startsWith("eyJhbGciOiJSUzI1NiJ9."));
            log.info(authProvider.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

}
