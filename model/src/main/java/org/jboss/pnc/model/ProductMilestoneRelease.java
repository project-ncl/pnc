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
package org.jboss.pnc.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/30/16
 * Time: 12:57 PM
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(indexes = @Index(name = "idx_productmilestonerelease_milestone", columnList = "milestone_id"))
public class ProductMilestoneRelease implements GenericEntity<Integer> {
    private static final long serialVersionUID = -9033616377795309672L;

    public static final String SEQUENCE_NAME = "product_milestone_release_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "fk_productmilestone_milestonerelease"))
    private ProductMilestone milestone;
    
    @Enumerated(EnumType.STRING)
    private MilestoneReleaseStatus status;
    
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String log;

    private Date startingDate;

    private Date endDate;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public ProductMilestone getMilestone() {
        return milestone;
    }

    public void setMilestone(ProductMilestone milestone) {
        this.milestone = milestone;
    }

    public MilestoneReleaseStatus getStatus() {
        return status;
    }

    public void setStatus(MilestoneReleaseStatus status) {
        this.status = status;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Date getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
