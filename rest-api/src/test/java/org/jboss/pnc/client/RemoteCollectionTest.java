/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RemoteCollectionTest {

    @Test
    public void shouldIterateOverPages() throws Exception {
        AtomicInteger index = new AtomicInteger();
        Function<PageParameters, Page<Entity>> endpoint = (parameters) -> {
            Collection<Entity> collection = new ArrayList();
            collection.add(new Entity(index.getAndIncrement()));
            collection.add(new Entity(index.getAndIncrement()));
            return new Page<>(parameters.getPageIndex(), parameters.getPageSize(), 3, 6, collection);
        };
        RemoteCollectionConfig config = RemoteCollectionConfig.builder().pageSize(2).build();
        RemoteCollection<Entity> collection = new DefaultRemoteCollection<>(endpoint, config);
        Iterator<Entity> iterator = collection.iterator();
        List<Entity> collected = new ArrayList<>();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            System.out.println("Received: " + entity.id);
            collected.add(entity);

            if (collected.size() > 10) {
                throw new Exception("Infinite loop.");
            }
        }

        Assert.assertEquals(6, collected.size());
        Assert.assertEquals(5, collected.get(5).id);
    }

    class Entity {
        int id;

        public Entity(int id) {
            this.id = id;
        }
    }
}
