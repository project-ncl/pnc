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
package org.jboss.pnc.facade.providers;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.facade.providers.api.HealthCheckProvider;
import org.jboss.pnc.model.GenericSetting;
import org.jboss.pnc.spi.datastore.repositories.GenericSettingRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@PermitAll
@Stateless
@Slf4j
public class HealthCheckProviderImpl implements HealthCheckProvider {

    public static final String HEALTH_CHECK_KEY = "HEALTH_CHECK";

    @Inject
    private GenericSettingRepository genericSettingRepository;

    public Map<String, Boolean> check() {

        Map<String, Boolean> result = new HashMap<>();
        result.put("check-database-read", checkDatabaseRead());
        result.put("check-database-write", checkDatabaseWrite());

        return result;
    }

    private boolean checkDatabaseRead() {
        try {
            genericSettingRepository.queryAll().size();
            return true;
        } catch (Exception e) {
            log.error("Error while reading from database", e);
            return false;
        }
    }

    private boolean checkDatabaseWrite() {

        try {

            GenericSetting healthCheck = genericSettingRepository.queryByKey(HEALTH_CHECK_KEY);

            if (healthCheck == null) {
                healthCheck = new GenericSetting();
                healthCheck.setKey(HEALTH_CHECK_KEY);
            }

            healthCheck.setValue(new Date().toString());
            genericSettingRepository.save(healthCheck);

            return true;
        } catch (Exception e) {
            log.error("Error while writing to database", e);
            return false;
        }
    }
}
