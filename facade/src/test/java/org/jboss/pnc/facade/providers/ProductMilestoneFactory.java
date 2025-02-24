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

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.jboss.pnc.api.constants.Attributes.BREW_TAG_PREFIX;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ProductMilestoneFactory {

    private static final Map<String, ProductMilestoneFactory> INSTANCE = new ConcurrentHashMap<>();
    private Supplier<Integer> idSupplier;
    private AtomicInteger internalId;

    private ProductMilestoneFactory() {
        this.internalId = new AtomicInteger();
    }

    public void setIdSupplier(Supplier<Integer> idSupplier) {
        this.idSupplier = idSupplier;
    }

    public static ProductMilestoneFactory getInstance() {
        return INSTANCE.computeIfAbsent("INST", k -> new ProductMilestoneFactory());
    }

    public ProductMilestone prepareNewProductMilestone(String productVersion, String milestoneVersion) {
        Product product = Product.Builder.newBuilder().id(getNextId()).name(Sequence.nextId().toString()).build();

        ProductVersion pV = ProductVersion.Builder.newBuilder()
                .id(getNextId())
                .version(productVersion)
                .product(product)
                .attributes(Map.of(BREW_TAG_PREFIX, "tag-prefix"))
                .build();

        return createNewProductMilestoneFromProductVersion(pV, milestoneVersion);
    }

    public ProductMilestone createNewProductMilestoneFromProductVersion(
            ProductVersion productVersion,
            String milestoneVersion) {

        return ProductMilestone.Builder.newBuilder()
                .id(getNextId())
                .productVersion(productVersion)
                .version(milestoneVersion)
                .performedBuild(createNewBuild())
                .performedBuild(createNewBuild())
                .build();
    }

    private BuildRecord createNewBuild() {
        return BuildRecord.Builder.newBuilder().id(Sequence.nextBase32Id()).status(BuildStatus.SUCCESS).build();
    }

    private Integer getNextId() {
        if (idSupplier != null) {
            return idSupplier.get();
        } else {
            return internalId.incrementAndGet();
        }
    }
}
