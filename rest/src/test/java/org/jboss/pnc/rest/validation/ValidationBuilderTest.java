/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.rest.validation;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.EmptyEntityException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;
import org.jboss.pnc.rest.validation.groups.ValidationGroup;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.rest.validation.helpers.Entity;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Test;

import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ValidationBuilderTest {

    class ValidationTesterClass {
        @NotNull
        private String notNullWithoutGroup;

        @NotNull(groups = WhenCreatingNew.class)
        private String notNullWhenCreateNew;

        @NotNull(groups = WhenUpdating.class)
        private String notNullWhenUpdating;

        private String testField;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectValidationGroupClass() throws Exception {
        ValidationBuilder.validateObject(null, ValidationGroup.class);
    }

    @Test
    public void shouldFailValidation() throws Exception {
        //given
        ValidationTesterClass testedObj = new ValidationTesterClass();

        //when
        try {
            ValidationBuilder.validateObject(testedObj, WhenCreatingNew.class).validateAnnotations();
            fail();
        } catch (InvalidEntityException e) {
            //then
            assertThat(e.getField()).isEqualTo("notNullWhenCreateNew");
        }
    }

    @Test
    public void shouldValidateOnlyWhenCreatingNewGroup() throws Exception {
        //given
        ValidationTesterClass testedObj = new ValidationTesterClass();
        testedObj.notNullWhenCreateNew = "test";

        //when
        //then
        ValidationBuilder.validateObject(testedObj, WhenCreatingNew.class).validateAnnotations();
    }

    @Test
    public void shouldValidateNotNullField() throws Exception {
        //given
        ValidationTesterClass testedObj = new ValidationTesterClass();
        testedObj.testField = "test";

        //when
        //then
        ValidationBuilder.validateObject(testedObj, WhenCreatingNew.class).validateField("testField", "test");
    }

    @Test
    public void shouldValidateNullField() throws Exception {
        //given
        ValidationTesterClass testedObj = new ValidationTesterClass();

        //when
        //then
        ValidationBuilder.validateObject(testedObj, WhenCreatingNew.class).validateField("testField", null);
    }

    @Test
    public void shouldFailFieldValidation() throws Exception {
        //given
        ValidationTesterClass testedObj = new ValidationTesterClass();
        testedObj.testField = "test";

        //when
        try {
            ValidationBuilder.validateObject(testedObj, WhenCreatingNew.class).validateField("testField", null);
            fail();
        } catch (InvalidEntityException e) {
            //then
            assertThat(e.getField()).isEqualTo("testField");
        }
    }

    @Test
    public void shouldValidateConflict() throws Exception {
        //given
        ValidationTesterClass testedObj = new ValidationTesterClass();

        //when
        try {
            ValidationBuilder.validateObject(testedObj, WhenCreatingNew.class).validateConflict(
                    () -> new ConflictedEntryValidator.ConflictedEntryValidationError(1, BuildConfiguration.class, "test")
            );
            fail();
        } catch (ConflictedEntryException e) {
            //then
            assertThat(e.getConflictedEntity()).isEqualTo(BuildConfiguration.class);
            assertThat(e.getConflictedRecordId()).isEqualTo(1);
            assertThat(e.getMessage()).isEqualTo("test");
        }
    }

    @Test
    public void shouldValidateNotEmptyArgument() throws Exception {
        //when
        try {
            ValidationBuilder.validateObject(null, WhenCreatingNew.class).validateNotEmptyArgument();
            fail();
        } catch (EmptyEntityException ok) {
            //then
        }
    }

    @Test
    public void shouldAllowEmptyValidationObject() throws Exception {
        //when//then
        ValidationBuilder.validateObject(WhenCreatingNew.class);
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldValidateCondition() throws Exception {
        //when//then
        ValidationBuilder.validateObject(WhenCreatingNew.class)
            .validateCondition(false, "message");
    }

    @Test
    public void shouldValidateAgainstRepository() throws Exception {
        //given
        Entity testedEntity = new Entity();
        Repository<Entity, Integer> repositoryMock = mock(Repository.class);
        doReturn(testedEntity).when(repositoryMock).queryById(anyInt());

        //when//then
        ValidationBuilder.validateObject(WhenCreatingNew.class).
                validateAgainstRepository(repositoryMock, 1, true);
    }

    @Test(expected = RepositoryViolationException.class)
    public void shouldFailWhenNotExpectingEntryToBeThere() throws Exception {
        //given
        Entity testedEntity = new Entity();
        Repository<Entity, Integer> repositoryMock = mock(Repository.class);
        doReturn(testedEntity).when(repositoryMock).queryById(anyInt());

        //when//then
        ValidationBuilder.validateObject(WhenCreatingNew.class).
                validateAgainstRepository(repositoryMock, 1, false);
    }

    @Test
    public void shouldValidateIfNoEntryIsExpected() throws Exception {
        //given
        Repository<Entity, Integer> repositoryMock = mock(Repository.class);

        //when//then
        ValidationBuilder.validateObject(WhenCreatingNew.class).
                validateAgainstRepository(repositoryMock, 1, false);
    }

    @Test(expected = RepositoryViolationException.class)
    public void shouldFailWhenExpectingEntryAndItsNotThere() throws Exception {
        //given
        Repository<Entity, Integer> repositoryMock = mock(Repository.class);

        //when//then
        ValidationBuilder.validateObject(WhenCreatingNew.class).
                validateAgainstRepository(repositoryMock, 1, true);
    }
}