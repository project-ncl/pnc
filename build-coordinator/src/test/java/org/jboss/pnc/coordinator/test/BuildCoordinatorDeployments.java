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

package org.jboss.pnc.coordinator.test;

import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.coordinator.builder.DefaultBuildCoordinator;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.coordinator.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.coordinator.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.coordinator.test.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.executor.DefaultBuildExecutor;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.executor.BuildExecutorMock;
import org.jboss.pnc.mock.model.builders.TestEntitiesFactory;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.test.arquillian.ShrinkwrapDeployerUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildCoordinatorDeployments {

    public enum Options {

        WITH_DATASTORE (() -> datastoreArchive());

        Supplier<Archive> archiveSupplier;

        Options(Supplier archiveSupplier) {
            this.archiveSupplier = archiveSupplier;
        }

        public Archive getArchive() {
            return archiveSupplier.get();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(BuildCoordinatorDeployments.class);

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
                .addClass(BuildSetStatusChangedEvent.class)
                .addClass(DefaultBuildSetStatusChangedEvent.class)
                .addClass(BuildEnvironment.Builder.class)
                .addClass(TestEntitiesFactory.class)
                .addClass(BuildCoordinatorFactory.class)
                .addPackages(true,
                        BuildCoordinator.class.getPackage(),
                        DefaultBuildCoordinator.class.getPackage(),
                        BuildSetStatusNotifications.class.getPackage(),
                        TestProjectConfigurationBuilder.class.getPackage(),
                        ContentIdentityManager.class.getPackage(),
                        BuildConfigSetRecordRepository.class.getPackage(),
                        TestCDIBuildStatusChangedReceiver.class.getPackage(),
                        BuildSetCallBack.class.getPackage(),
                        BuildCallBack.class.getPackage(),
                        BuildCoordinationStatus.class.getPackage(),
                        DefaultBuildStatusChangedEvent.class.getPackage(),
                        BuildExecutorMock.class.getPackage(),
                        DefaultBuildExecutor.class.getPackage(),
                        BpmManager.class.getPackage())
                .addAsManifestResource(new StringAsset(Descriptors.create(BeansDescriptor.class).getOrCreateAlternatives().clazz(BuildExecutorMock.class.getName()).up().exportAsString()), "beans.xml")
                .addAsResource("simplelogger.properties");

        ShrinkwrapDeployerUtils.addPomLibs(jar, "org.slf4j:slf4j-simple");

        return jar;
    }

    private static JavaArchive datastoreArchive() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(DatastoreMock.class)
                .addPackages(true, DatastoreAdapter.class.getPackage());
    }


}
