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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DeliverableArtifactTest {

    /**
     * Make sure that a DeliverableArtifact with the same artifact and report but different distributions are considered
     * unique
     */
    @Test
    public void testEquals() {
        Artifact artifact = new Artifact();
        artifact.setId(1234);

        DeliverableAnalyzerReport report = new DeliverableAnalyzerReport();
        report.setId(new Base32LongID(1235));

        DeliverableAnalyzerDistribution distribution1 = new DeliverableAnalyzerDistribution();
        distribution1.setId(new Base32LongID(123));

        DeliverableAnalyzerDistribution distribution2 = new DeliverableAnalyzerDistribution();
        distribution2.setId(new Base32LongID(321));

        DeliverableArtifact deliverableArtifact1 = new DeliverableArtifact();
        deliverableArtifact1.setArtifact(artifact);
        deliverableArtifact1.setReport(report);
        deliverableArtifact1.setDistribution(distribution1);

        DeliverableArtifact deliverableArtifact2 = new DeliverableArtifact();
        deliverableArtifact2.setArtifact(artifact);
        deliverableArtifact2.setReport(report);
        deliverableArtifact2.setDistribution(distribution2);

        assertNotEquals(deliverableArtifact1, deliverableArtifact2);
        assertNotEquals(deliverableArtifact1.hashCode(), deliverableArtifact2.hashCode());
    }

    @Test
    public void testEqualsDifferentArtifact() {
        Artifact artifact1 = new Artifact();
        artifact1.setId(1234);

        Artifact artifact2 = new Artifact();
        artifact2.setId(4321);

        DeliverableAnalyzerReport report = new DeliverableAnalyzerReport();
        report.setId(new Base32LongID(1235));

        DeliverableAnalyzerDistribution distribution1 = new DeliverableAnalyzerDistribution();
        distribution1.setId(new Base32LongID(321));

        DeliverableArtifact deliverableArtifact1 = new DeliverableArtifact();
        deliverableArtifact1.setArtifact(artifact1);
        deliverableArtifact1.setReport(report);
        deliverableArtifact1.setDistribution(distribution1);

        DeliverableArtifact deliverableArtifact2 = new DeliverableArtifact();
        deliverableArtifact2.setArtifact(artifact2);
        deliverableArtifact2.setReport(report);
        deliverableArtifact2.setDistribution(distribution1);

        assertNotEquals(deliverableArtifact1, deliverableArtifact2);
        assertNotEquals(deliverableArtifact1.hashCode(), deliverableArtifact2.hashCode());
    }

    @Test
    public void testEqualsDifferentReport() {
        Artifact artifact = new Artifact();
        artifact.setId(1234);

        DeliverableAnalyzerReport report1 = new DeliverableAnalyzerReport();
        report1.setId(new Base32LongID(1235));

        DeliverableAnalyzerReport report2 = new DeliverableAnalyzerReport();
        report2.setId(new Base32LongID(5321));

        DeliverableAnalyzerDistribution distribution1 = new DeliverableAnalyzerDistribution();
        distribution1.setId(new Base32LongID(123));

        DeliverableArtifact deliverableArtifact1 = new DeliverableArtifact();
        deliverableArtifact1.setArtifact(artifact);
        deliverableArtifact1.setReport(report1);
        deliverableArtifact1.setDistribution(distribution1);

        DeliverableArtifact deliverableArtifact2 = new DeliverableArtifact();
        deliverableArtifact2.setArtifact(artifact);
        deliverableArtifact2.setReport(report2);
        deliverableArtifact2.setDistribution(distribution1);

        assertNotEquals(deliverableArtifact1, deliverableArtifact2);
        assertNotEquals(deliverableArtifact1.hashCode(), deliverableArtifact2.hashCode());
    }

    @Test
    public void testEqualsEverythingSame() {
        Artifact artifact = new Artifact();
        artifact.setId(1234);

        DeliverableAnalyzerReport report = new DeliverableAnalyzerReport();
        report.setId(new Base32LongID(1235));

        DeliverableAnalyzerDistribution distribution = new DeliverableAnalyzerDistribution();
        distribution.setId(new Base32LongID(89));

        DeliverableArtifact deliverableArtifact1 = new DeliverableArtifact();
        deliverableArtifact1.setArtifact(artifact);
        deliverableArtifact1.setReport(report);
        deliverableArtifact1.setDistribution(distribution);

        DeliverableArtifact deliverableArtifact2 = new DeliverableArtifact();
        deliverableArtifact2.setArtifact(artifact);
        deliverableArtifact2.setReport(report);
        deliverableArtifact2.setDistribution(distribution);

        assertEquals(deliverableArtifact1, deliverableArtifact2);
        assertEquals(deliverableArtifact1.hashCode(), deliverableArtifact2.hashCode());
    }
}