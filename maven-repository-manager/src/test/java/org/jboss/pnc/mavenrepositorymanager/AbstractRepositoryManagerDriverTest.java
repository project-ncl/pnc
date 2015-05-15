/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.commonjava.aprox.boot.BootStatus;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.test.fixture.core.CoreServerFixture;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ModuleConfigJson;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class AbstractRepositoryManagerDriverTest {

    protected static final String CONFIG_SYSPROP = "pnc-config-file";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    protected RepositoryManagerDriver driver;
    protected CoreServerFixture fixture;
    protected String url;

    private String oldIni;

    @Before
    public void setup() throws Exception {
        fixture = new CoreServerFixture(temp);

        Properties sysprops = System.getProperties();
        oldIni = sysprops.getProperty(CONFIG_SYSPROP);

        url = fixture.getUrl();
        File configFile = temp.newFile("pnc-config.json");
        ModuleConfigJson moduleConfigJson = new ModuleConfigJson("pnc-config");
        MavenRepoDriverModuleConfig mavenRepoDriverModuleConfig = new MavenRepoDriverModuleConfig(fixture.getUrl());
        moduleConfigJson.addConfig(mavenRepoDriverModuleConfig);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(configFile, moduleConfigJson);

        sysprops.setProperty(CONFIG_SYSPROP, configFile.getAbsolutePath());
        System.setProperties(sysprops);

        fixture.start();

        if (!fixture.isStarted()) {
            final BootStatus status = fixture.getBootStatus();
            throw new IllegalStateException("server fixture failed to boot.", status.getError());
        }

        Properties props = new Properties();
        props.setProperty("base.url", url);

        System.out.println("Using base URL: " + url);

        Configuration config = new Configuration();
        driver = new RepositoryManagerDriver(config);
    }

    @After
    public void teardown() throws Exception {
        Properties sysprops = System.getProperties();
        if (oldIni == null) {
            sysprops.remove(CONFIG_SYSPROP);
        } else {
            sysprops.setProperty(CONFIG_SYSPROP, oldIni);
        }
        System.setProperties(sysprops);

        if (fixture != null) {
            fixture.stop();
        }
    }

    protected void assertGroupConstituents(Group buildGroup, StoreKey... constituents) {
        List<StoreKey> groupConstituents = buildGroup.getConstituents();
        for (int i = 0; i < constituents.length; i++) {
            assertThat("Group constituency too small to contain all the expected members.", groupConstituents.size() > i,
                    equalTo(true));

            StoreKey expected = constituents[i];
            StoreKey actual = groupConstituents.get(i);
            assertThat(actual, equalTo(expected));
        }
    }

}
