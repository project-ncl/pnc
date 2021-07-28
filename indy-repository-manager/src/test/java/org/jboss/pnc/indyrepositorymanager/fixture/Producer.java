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
package org.jboss.pnc.indyrepositorymanager.fixture;

import org.commonjava.indy.core.expire.DefaultScheduleManager;
import org.commonjava.indy.core.expire.ScheduleManager;
import org.commonjava.indy.inject.TestData;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.commonjava.maven.galley.auth.AttributePasswordManager;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.HttpImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class Producer {

    private IndyObjectMapper objectMapper;

    private ScheduleManager scheduleManager;

    private Http http;

    public Producer() {
        objectMapper = new IndyObjectMapper(true);
        PasswordManager passman = new AttributePasswordManager();
        http = new HttpImpl(passman);

        scheduleManager = new DefaultScheduleManager();
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
    public IndyObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
