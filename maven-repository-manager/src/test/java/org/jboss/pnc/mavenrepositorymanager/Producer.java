package org.jboss.pnc.mavenrepositorymanager;

import org.commonjava.aprox.core.expire.ScheduleManager;
import org.commonjava.aprox.inject.TestData;
import org.commonjava.aprox.model.core.io.AproxObjectMapper;
import org.commonjava.aprox.subsys.maven.MavenComponentManager;
import org.commonjava.maven.galley.auth.AttributePasswordManager;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.HttpImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class Producer {

    private AproxObjectMapper objectMapper;

    private MavenComponentManager componentManager;

    private ScheduleManager scheduleManager;

    private Http http;

    public Producer() {
        objectMapper = new AproxObjectMapper(true);
        PasswordManager passman = new AttributePasswordManager();
        http = new HttpImpl(passman);

        scheduleManager = new ScheduleManager();
        componentManager = new MavenComponentManager();
    }

    // @Produces
    // @Default
    public MavenComponentManager getComponentManager() {
        return componentManager;
    }

    // @Produces
    // @Default
    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    // @Produces
    // @Default
    public Http getHttp() {
        return http;
    }

    @Produces
    @Default
    @TestData
    public AproxObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
