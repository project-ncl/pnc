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

package org.jboss.pnc.executor;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.executor.servicefactories.BuildDriverFactory;
import org.jboss.pnc.mock.builddriver.BuildDriverMock;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.environmentdriver.EnvironmentDriverMock;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.mock.repositorymanager.RepositoryManagerMock;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.test.arquillian.ShrinkwrapDeployerUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutorDeployments {

    public enum Options {

        NOT_YET_AVAILABLE (() -> noop());

        Supplier<Archive> archiveSupplier;

        Options(Supplier archiveSupplier) {
            this.archiveSupplier = archiveSupplier;
        }

        public Archive getArchive() {
            return archiveSupplier.get();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(BuildExecutorDeployments.class);

    public static JavaArchive deployment(Options... options) {

        JavaArchive jar = defaultLibs();

        for (Options option : options) {
            jar.merge(option.getArchive());
        }

        log.debug(jar.toString(true));
        return jar;
    }

    private static JavaArchive defaultLibs() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(Configuration.class)
                .addClass(BuildEnvironment.Builder.class)
                .addClass(DatastoreMock.class)
                .addClass(TestProjectConfigurationBuilder.class)
                .addClass(RepositoryManagerMock.class)
                .addClass(EnvironmentDriverMock.class)
                .addPackages(true,
                        BuildDriverFactory.class.getPackage(),
                        BuildDriverMock.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("simplelogger.properties");

        ShrinkwrapDeployerUtils.addPomLibs(jar, "org.slf4j:slf4j-simple");

        return jar;
    }

    private static Archive noop() {
        return null;
    }

}
