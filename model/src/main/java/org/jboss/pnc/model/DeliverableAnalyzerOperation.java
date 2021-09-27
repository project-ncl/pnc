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
package org.jboss.pnc.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.jboss.pnc.api.enums.OperationStatus;
import org.jboss.pnc.model.BuildRecord.Builder;

@Entity
@DiscriminatorValue("DelAnalysis")
public class DeliverableAnalyzerOperation extends Operation {

    private static final long serialVersionUID = 4972591927855499338L;

    /**
     * The product milestone for which this deliverable analyzer operation was performed.
     */
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_operation_productmilestone"))
    private ProductMilestone productMilestone;

    public DeliverableAnalyzerOperation() {

    }

    /**
     * The product milestone for which this deliverable analyzer operation was performed
     *
     * @return The product milestone
     */
    public ProductMilestone getProductMilestone() {
        return productMilestone;
    }

    /**
     * Sets the product milestone associated with the deliverable analysis operation.
     *
     * @param milestoneId the milestoneId associated with the deliverable analysis operation
     */
    public void setProductMilestone(ProductMilestone productMilestone) {
        this.productMilestone = productMilestone;
    }

    public static class Builder {

        private Base32LongID id;

        private Date startTime;

        private Date endTime;

        private User user;

        private Map<String, String> operationParameters;

        private OperationStatus status;

        private ProductMilestone productMilestone;

        private Builder() {
            operationParameters = new HashMap<>();
        }

        public DeliverableAnalyzerOperation build() {

            DeliverableAnalyzerOperation operation = new DeliverableAnalyzerOperation();
            operation.setId(id);
            operation.setStartTime(startTime);
            operation.setEndTime(endTime);
            operation.setUser(user);
            operation.setStatus(status);
            operation.setOperationParameters(operationParameters);
            operation.setProductMilestone(productMilestone);

            return operation;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder id(Base32LongID id) {
            this.id = id;
            return this;
        }

        public Builder id(String id) {
            this.id = new Base32LongID(id);
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Date endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder operationParameters(Map<String, String> operationParameters) {
            this.operationParameters = operationParameters;
            return this;
        }

        public Builder status(OperationStatus status) {
            this.status = status;
            return this;
        }

        public Builder productMilestone(ProductMilestone productMilestone) {
            this.productMilestone = productMilestone;
            return this;
        }

        public Base32LongID getId() {
            return id;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public User getUser() {
            return user;
        }

        public Map<String, String> getOperationParameters() {
            return operationParameters;
        }

        public OperationStatus getStatus() {
            return status;
        }

        public ProductMilestone getProductMilestone() {
            return productMilestone;
        }

    }

}
