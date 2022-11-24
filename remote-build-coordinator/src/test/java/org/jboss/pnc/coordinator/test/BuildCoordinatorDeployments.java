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

package org.jboss.pnc.remotecoordinator.test;

import org.jboss.pnc.bpm.Connector;
import org.jboss.pnc.bpm.model.mapper.BuildResultMapper;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.ModuleConfigFactory;
import org.jboss.pnc.remotecoordinator.builder.RemoteBuildCoordinator;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.remotecoordinator.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.remotecoordinator.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.remotecoordinator.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.remotecoordinator.test.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.remotecoordinator.test.mock.EntityManagerMock;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.mock.datastore.BuildTaskRepositoryMock;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.executor.BuildExecutorMock;
import org.jboss.pnc.mock.model.builders.TestEntitiesFactory;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.mock.repository.BuildConfigSetRecordRepositoryMock;
import org.jboss.pnc.mock.repository.BuildConfigurationAuditedRepositoryMock;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildCoordinatorDeployments {

    public enum Options {

        WITH_DATASTORE(BuildCoordinatorDeployments::datastoreArchive),
        WITH_BPM(BuildCoordinatorDeployments::bpmArchive);

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
                .addClass(BuildConfigurationAuditedRepositoryMock.class)
                .addClass(EntityManagerMock.class)
                .addPackages(false, BuildResultMapper.class.getPackage())
                .addPackages(
                        true,
                        BuildCoordinator.class.getPackage(),
                        RemoteBuildCoordinator.class.getPackage(),
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
                        MessageSender.class.getPackage(),
                        SystemConfig.class.getPackage(),
                        ModuleConfigFactory.class.getPackage(),
                        RefToReferenceMapper.class.getPackage())
                .addAsManifestResource("beans.xml")
                .addAsResource("logback-test.xml", "logback.xml");

        log.info("Deployment content: {}", jar.toString(true));
        return jar;
    }

    private static JavaArchive datastoreArchive() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(DatastoreMock.class)
                .addClass(BuildTaskRepositoryMock.class)
                .addClass(BuildConfigSetRecordRepositoryMock.class)
                .addPackages(true, DatastoreAdapter.class.getPackage());
    }

    private static JavaArchive bpmArchive() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(false, Connector.class.getPackage(), BpmBuildTask.class.getPackage());
    }

}
