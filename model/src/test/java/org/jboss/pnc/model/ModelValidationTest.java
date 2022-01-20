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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Assert;
import org.junit.Test;

public class ModelValidationTest extends AbstractModelTest {

    /**
     * Test validation of the version string regex
     * 
     * @throws Exception
     */
    @Test
    public void testVersionStringValidation() throws Exception {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Product product = Product.Builder.newBuilder().name("Test Product").build();
        ProductVersion productVersion = ProductVersion.Builder.newBuilder().product(product).version("1.0").build();

        // Test validation of product version
        Set<ConstraintViolation<ProductVersion>> productVersionViolations = validator.validate(productVersion);
        Assert.assertEquals(0, productVersionViolations.size());

        productVersion.setVersion("1.0.x");
        productVersionViolations = validator.validate(productVersion);
        Assert.assertEquals(1, productVersionViolations.size());

        productVersion.setVersion("foo");
        productVersionViolations = validator.validate(productVersion);
        Assert.assertEquals(1, productVersionViolations.size());

        // Test product milestone versions
        ProductMilestone milestone = ProductMilestone.Builder.newBuilder()
                .productVersion(productVersion)
                .version("1.0.0.ER1")
                .build();
        Set<ConstraintViolation<ProductMilestone>> milestoneVersionViolations = validator.validate(milestone);
        Assert.assertEquals(0, milestoneVersionViolations.size());

        milestone.setVersion("1.0");
        milestoneVersionViolations = validator.validate(milestone);
        Assert.assertEquals(1, milestoneVersionViolations.size());

        milestone.setVersion("1.0-DR1");
        milestoneVersionViolations = validator.validate(milestone);
        Assert.assertEquals(1, milestoneVersionViolations.size());

        milestone.setVersion("1.0-x");
        milestoneVersionViolations = validator.validate(milestone);
        Assert.assertEquals(1, milestoneVersionViolations.size());

        // Test product release versions
        ProductRelease release = ProductRelease.Builder.newBuilder()
                .productMilestone(milestone)
                .version("1.0.0.GA")
                .build();
        Set<ConstraintViolation<ProductRelease>> releaseVersionViolations = validator.validate(release);
        Assert.assertEquals(0, releaseVersionViolations.size());

        release.setVersion("1.0");
        releaseVersionViolations = validator.validate(release);
        Assert.assertEquals(1, releaseVersionViolations.size());

        release.setVersion("1.0-DR1");
        releaseVersionViolations = validator.validate(release);
        Assert.assertEquals(1, releaseVersionViolations.size());

        release.setVersion("1.0-x");
        releaseVersionViolations = validator.validate(release);
        Assert.assertEquals(1, releaseVersionViolations.size());

    }

    /**
     * Test validation of the cloned name of BuildConfigurations
     * 
     * Example: clone1 of pslegr-BC on Wednesday October,21st, 2015: 20151021095415_pslegr-BC
     * 
     * clone2 of 20151021095415_pslegr-BC on Thursday October,22nd, 2015: 20151022nnnnnn_pslegr-BC
     * 
     * clone3 of pslegr-BC on Friday October,23rd, 2015: 20151023nnnnnn_pslegr-BC
     * 
     * 
     * @throws Exception
     */
    @Test
    public void testClonedBcNameStringValidation() throws Exception {

        Date day21 = new SimpleDateFormat(BuildConfiguration.CLONE_PREFIX_DATE_FORMAT).parse("20151021095415");
        Date day22 = new SimpleDateFormat(BuildConfiguration.CLONE_PREFIX_DATE_FORMAT).parse("20151022095415");
        Date day23 = new SimpleDateFormat(BuildConfiguration.CLONE_PREFIX_DATE_FORMAT).parse("20151023095415");

        // Clone on day 21
        String clonedName1 = BuildConfiguration.retrieveCloneName("pslegr-BC", day21);
        Assert.assertEquals("20151021095415_pslegr-BC", clonedName1);

        // Clone of clone on day 22
        String clonedName2 = BuildConfiguration.retrieveCloneName(clonedName1, day22);
        Assert.assertEquals("20151022095415_pslegr-BC", clonedName2);

        // Clone on day 23
        String clonedName3 = BuildConfiguration.retrieveCloneName("pslegr-BC", day23);
        Assert.assertEquals("20151023095415_pslegr-BC", clonedName3);

        // Clone wiht not valid prefix date (must be also CLONE_PREFIX_DATE_FORMAT.lenght())
        String clonedName4 = BuildConfiguration.retrieveCloneName("2015102309541_pslegr-BC", day23);
        Assert.assertEquals("20151023095415_2015102309541_pslegr-BC", clonedName4);

    }

    @Test
    public void testProductVersionStringValidationFailureOnCommit() throws Exception {

        ProductVersion productVersion1 = ProductVersion.Builder.newBuilder()
                .product(Product.Builder.newBuilder().id(1).build())
                .version("foo") // Invalid version string
                .build();

        EntityManager em = getEmFactory().createEntityManager();
        EntityTransaction tx1 = em.getTransaction();

        try {
            tx1.begin();
            em.persist(productVersion1);
            tx1.commit(); // This should throw a Rollback exception caused by the constraint violation

        } catch (RollbackException e) {
            if (tx1 != null && tx1.isActive()) {
                tx1.rollback();
            }
            Assert.assertTrue(e.getCause() instanceof ConstraintViolationException);
        } finally {
            em.close();
        }

    }

}
