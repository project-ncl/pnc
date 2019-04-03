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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commonjava.indy.boot.BootStatus;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.test.fixture.core.CoreServerFixture;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ModuleConfigJson;
import org.jboss.pnc.common.json.PNCModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AbstractRepositoryManagerDriverTest {

    protected static final String CONFIG_SYSPROP = "pnc-config-file";
    
    protected static final String PNC_BUILDS = "pnc-builds";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    protected RepositoryManagerDriver driver;
    protected String accessToken;
    protected CoreServerFixture fixture;
    protected String url;

    private String oldIni;
    private File etcDir;
    private File dataDir;

    @Before
    public void setup() throws Exception {
        fixture = newServerFixture();

        Properties sysprops = System.getProperties();
        oldIni = sysprops.getProperty(CONFIG_SYSPROP);

        url = fixture.getUrl();
        File configFile = temp.newFile("pnc-config.json");
        ModuleConfigJson moduleConfigJson = new ModuleConfigJson("pnc-config");
        MavenRepoDriverModuleConfig mavenRepoDriverModuleConfig = new MavenRepoDriverModuleConfig(fixture.getUrl());
        mavenRepoDriverModuleConfig.setInternalRepoPatterns(getInternalRepoPatterns());
        SystemConfig systemConfig = new SystemConfig(
                "",
                "",
                "JAAS",
                "4",
                "4",
                "4",
                "",
                "5",
                null,
                "14",
                "",
                "10");
        PNCModuleGroup pncGroup = new PNCModuleGroup();
        pncGroup.addConfig(mavenRepoDriverModuleConfig);
        pncGroup.addConfig(systemConfig);
        moduleConfigJson.addConfig(pncGroup);

        ObjectMapper mapper = new ObjectMapper();
        PncConfigProvider<MavenRepoDriverModuleConfig> pncProvider = 
                new PncConfigProvider<MavenRepoDriverModuleConfig>(MavenRepoDriverModuleConfig.class);
        pncProvider.registerProvider(mapper);
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

    protected List<String> getInternalRepoPatterns() {
        return null;
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

    protected final CoreServerFixture newServerFixture()
            throws Exception
    {
        final CoreServerFixture fixture = new CoreServerFixture( temp );

        etcDir = new File( fixture.getBootOptions().getIndyHome(), "etc/indy" );
        dataDir = new File( fixture.getBootOptions().getIndyHome(), "var/lib/indy/data" );

        initBaseTestConfig( fixture );
        initTestConfig( fixture );
        initTestData( fixture );

        return fixture;
    }

    protected void initTestConfig( CoreServerFixture fixture )
            throws IOException
    {
    }

    protected void initTestData( CoreServerFixture fixture )
            throws IOException
    {
    }

    protected final void initBaseTestConfig( CoreServerFixture fixture )
            throws IOException
    {
        writeConfigFile( "conf.d/scheduler.conf", "[scheduler]\nenabled=false" );
        writeConfigFile( "conf.d/threadpools.conf", "[threadpools]\nenabled=false" );
    }

    protected String readTestResource( String resource )
            throws IOException
    {
        return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource));
    }

    protected void writeConfigFile( String confPath, String contents )
            throws IOException
    {
        File confFile = new File( etcDir, confPath );
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info( "Writing configuration to: {}\n\n{}\n\n", confFile, contents );

        confFile.getParentFile().mkdirs();

        FileUtils.write( confFile, contents );
    }

    protected void writeDataFile( String path, String contents )
            throws IOException
    {
        File confFile = new File( dataDir, path );

        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info( "Writing data file to: {}\n\n{}\n\n", confFile, contents );
        confFile.getParentFile().mkdirs();

        FileUtils.write(confFile, contents);
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
