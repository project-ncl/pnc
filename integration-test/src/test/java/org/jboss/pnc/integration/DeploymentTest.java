package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DeploymentTest {

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-web.war");
        war.addClass(DeploymentTest.class);
        System.out.println(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    public void shouldDeployApp() {
        //empty
    }

}
