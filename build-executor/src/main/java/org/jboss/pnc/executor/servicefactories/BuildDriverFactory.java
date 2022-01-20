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
package org.jboss.pnc.executor.servicefactories;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class BuildDriverFactory {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Instance<BuildDriver> availableDrivers;
    private Configuration configuration;
    private Predicate<BuildDriver> configurationPredicate = buildDriver -> true;

    /**
     * @deprecated Only for CDI
     */
    @Deprecated
    public BuildDriverFactory() {
    }

    @Inject
    public BuildDriverFactory(Instance<BuildDriver> availableDrivers, Configuration configuration) {
        this.availableDrivers = availableDrivers;
        this.configuration = configuration;
    }

    @PostConstruct
    public void initConfiguration() throws ConfigurationParseException {
        try {
            String driverId = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class))
                    .getBuildDriverId();
            if (!StringUtils.isEmpty(driverId)) {
                configurationPredicate = buildDriver -> driverId.equals(buildDriver.getDriverId());
            }
        } catch (ConfigurationParseException exception) {
            logger.warn("There is a problem while parsing system configuration. Using defaults.", exception);
        }

    }

    public BuildDriver getBuildDriver() throws ExecutorException {
        Optional<BuildDriver> match = StreamSupport.stream(availableDrivers.spliterator(), false)
                .filter(configurationPredicate)
                .findFirst();

        return match.orElseThrow(
                () -> new ExecutorException(
                        "No valid build driver available." + " Available drivers: " + availableDriverIds()));
    }

    private String availableDriverIds() {
        List<String> ids = new ArrayList<>();
        for (BuildDriver driver : availableDrivers) {
            ids.add(driver.getDriverId());
        }
        return ids.toString();
    }

}
