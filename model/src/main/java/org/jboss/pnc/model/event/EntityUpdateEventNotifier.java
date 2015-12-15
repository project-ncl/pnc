/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.model.event;

import org.jboss.pnc.model.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import java.util.Optional;

import static org.jboss.pnc.model.event.EntityOperationType.*;

/**
 * @author Jakub Senko
 */
public class EntityUpdateEventNotifier {

    public static final Logger logger = LoggerFactory.getLogger(EntityUpdateEventNotifier.class);


    private static Optional<BeanManager> getBeanManager() {
        try {
            InitialContext initialContext = new InitialContext();
            return Optional.of((BeanManager) initialContext.lookup("java:comp/BeanManager"));
        } catch (Exception e) {
            logger.error("Could not get Bean Manager in EntityUpdateEventNotifier.", e);
            return Optional.empty();
        }
    }

    @PostPersist
    public void observePostPersist(Object obj) {
        observe(obj, CREATE);
    }

    @PostUpdate
    public void observePostUpdate(Object obj) {
        observe(obj, UPDATE);
    }

    @PostRemove
    public void observePostDelete(Object obj) {
        observe(obj, DELETE);
    }

    private void observe(Object obj, EntityOperationType operationType) {
        getBeanManager().ifPresent(beanManager -> {
            try {
                GenericEntity<Integer> entity = (GenericEntity<Integer>) obj;
                beanManager.fireEvent(new EntityUpdateEvent(entity.getId(), entity.getClass(), operationType));
            } catch (ClassCastException ex) {
                logger.debug("Object " + obj + " is not a GenericEntity<Integer>. Not firing a EntityUpdateEvent.", ex);
            }
        });
    }
}
