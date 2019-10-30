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
package org.jboss.pnc.rest.provider;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.model.HealthCheck;
import org.jboss.pnc.spi.datastore.repositories.HealthCheckRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PermitAll
@Stateless
@Slf4j
public class HealthCheckProvider {

    @Inject
    private HealthCheckRepository healthCheckRepository;

    @Deprecated
    public HealthCheckProvider() {}

    public Map<String, Boolean> check() {

        Map<String, Boolean> result = new HashMap<>();
        result.put("check-database-read", checkDatabaseRead());
        result.put("check-database-write", checkDatabaseWrite());

        return result;
    }

    private boolean checkDatabaseRead() {
        try {
            healthCheckRepository.queryAll().size();
            return true;
        } catch (Exception e) {
            log.error("Error while reading from database", e);
            return false;
        }
    }

    private boolean checkDatabaseWrite() {

        try {
            List<HealthCheck> healthCheckList = healthCheckRepository.queryAll();

            HealthCheck healthCheck;

            if (healthCheckList.size() > 0) {
                healthCheck = healthCheckList.get(0);
            } else {
                healthCheck = new HealthCheck();
            }

            healthCheck.setDate(new Date());
            healthCheckRepository.save(healthCheck);

            return true;
        } catch (Exception e) {
            log.error("Error while writing to database", e);
            return false;
        }
    }
}
