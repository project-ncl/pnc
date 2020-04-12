/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.restmodel;

import lombok.Getter;
import lombok.Setter;

import org.jboss.pnc.enums.MilestoneCloseStatus;
import org.jboss.pnc.model.ProductMilestoneRelease;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.Date;

@XmlRootElement(name = "ProductMilestoneRelease")
@Getter
@Setter
@Deprecated
public class ProductMilestoneReleaseRest implements GenericRestEntity<Integer> {

    private Integer id;
    private MilestoneCloseStatus status;
    private String log;
    private Date endDate;
    private Date startingDate;

    public ProductMilestoneReleaseRest() {
    }

    public ProductMilestoneReleaseRest(ProductMilestoneRelease release) {
        throw new UnsupportedOperationException("Use new REST API!");
    }
}
