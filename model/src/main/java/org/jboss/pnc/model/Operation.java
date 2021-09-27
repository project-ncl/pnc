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
package org.jboss.pnc.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.validation.constraints.NotNull;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Operation_Type")
public class Operation implements GenericEntity<Base32LongID> {

    private static final long serialVersionUID = 1802920727777133123L;

    @EmbeddedId
    private Base32LongID id;

    @Column(columnDefinition = "timestamp with time zone", updatable = false)
    private Date submitTime;

    @Column(columnDefinition = "timestamp with time zone", updatable = true)
    private Date startTime;

    @Column(columnDefinition = "timestamp with time zone", updatable = true)
    private Date endTime;

    @NotNull
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_operation_user"), updatable = false)
    private User user;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "operation_parameters",
            joinColumns = @JoinColumn(
                    name = "operation_id",
                    foreignKey = @ForeignKey(name = "fk_operation_parameters_operation")))
    @MapKeyColumn(length = 50, name = "key", nullable = false)
    @Column(name = "value", nullable = false, length = 8192)
    private Map<String, String> operationParameters = new HashMap<>();

    @Enumerated(EnumType.STRING)
    private ProgressStatus progressStatus;

    @Enumerated(EnumType.STRING)
    private OperationResult result;

    public Operation() {
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Base32LongID getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    @Override
    public void setId(Base32LongID id) {
        this.id = id;
    }

    /**
     * The time when the operation was submited and created.
     *
     * @return the start time
     */
    public Date getSubmitTime() {
        return submitTime;
    }

    /**
     * Sets the submit time.
     *
     * @param submitTime the start time of the operation
     */
    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    /**
     * The time when the operation was started.
     *
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the start time of the operation
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * The time when the operation was ended.
     *
     * @return the end time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time.
     *
     * @param endTime the end time of the operation
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the user who started the operation.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who started the operation.
     *
     * @param user the user who started the operation
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the operation input parameters.
     *
     * @return the operation input parameters
     */
    public Map<String, String> getOperationParameters() {
        return operationParameters;
    }

    /**
     * Sets the input parameters needed to execute the operation.
     *
     * @param operationParameters the input parameters needed to execute the operation
     */
    public void setOperationParameters(Map<String, String> operationParameters) {
        this.operationParameters = operationParameters;
    }

    /**
     * Gets the progress status.
     *
     * @return the status
     */
    public ProgressStatus getProgressStatus() {
        return progressStatus;
    }

    /**
     * Sets the progress status.
     *
     * @param progressStatus the new status
     */
    public void setProgressStatus(ProgressStatus progressStatus) {
        this.progressStatus = progressStatus;
    }

    /**
     * Gets the result status.
     *
     * @return the status
     */
    public OperationResult getResult() {
        return result;
    }

    /**
     * Sets the result status. If the status is not null, sets the progress status to FINISHED.
     *
     * @param result the new status
     */
    public void setResult(OperationResult result) {
        if (result != null) {
            setProgressStatus(ProgressStatus.FINISHED);
        }
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Operation))
            return false;
        return id != null && id.equals(((Operation) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
