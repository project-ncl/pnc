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
package org.jboss.pnc.dto.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;

import static org.jboss.pnc.enums.JobNotificationProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationType.PRODUCT_MILESTONE_CLOSE;

@Data
public class ProductMilestoneCloseResultNotification extends Notification {

    private static final String MILESTONE_CLOSE_RESULT = "PRODUCT_MILESTONE_CLOSE_RESULT";

    private final ProductMilestoneCloseResult productMilestoneCloseResult;

    @JsonCreator
    public ProductMilestoneCloseResultNotification(
            @JsonProperty("productMilestoneCloseResult") ProductMilestoneCloseResult productMilestoneCloseResult) {
        super(PRODUCT_MILESTONE_CLOSE, MILESTONE_CLOSE_RESULT, FINISHED, IN_PROGRESS);
        this.productMilestoneCloseResult = productMilestoneCloseResult;
    }
}
