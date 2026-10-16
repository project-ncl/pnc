/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class JSonOutputConverterTest {

    @Test
    public void shouldConvertProperObject() throws Exception {
        // given
        class SampleObject {

            String sampleField;

            public String getSampleField() {
                return sampleField;
            }

            public void setSampleField(String sampleField) {
                this.sampleField = sampleField;
            }
        }

        SampleObject objectToConvert = new SampleObject();
        objectToConvert.sampleField = "test";

        // when
        String convertedSting = JsonOutputConverterMapper.apply(objectToConvert);

        // than
        assertThat(convertedSting).isEqualTo("{\"sampleField\":\"test\"}");
    }

    @Test
    public void shouldNotFailWhenThereAreNoProperties() throws Exception {
        // given
        class SampleObject {
            String sampleField;
        }

        SampleObject objectToConvert = new SampleObject();
        objectToConvert.sampleField = "test";

        // when//then
        try {
            JsonOutputConverterMapper.apply(objectToConvert);
        } catch (IllegalArgumentException expected) {
            fail();
        }
    }

    @Test
    public void shouldNotFailWhenPassingNull() throws Exception {
        // when
        String convertedString = JsonOutputConverterMapper.apply(null);

        // then
        assertThat(convertedString).isEqualTo("{}");
    }

    @Test
    public void shouldNotRenderNulls() throws Exception {
        // given
        class SampleObject {

            String sampleField;
            String sampleNullField;

            public String getSampleField() {
                return sampleField;
            }

            public void setSampleField(String sampleField) {
                this.sampleField = sampleField;
            }

            public String getSampleNullField() {
                return sampleNullField;
            }

            public void setSampleNullField(String sampleNullField) {
                this.sampleNullField = sampleNullField;
            }
        }

        SampleObject sampleObject = new SampleObject();
        sampleObject.sampleField = "test";

        // when
        String convertedString = JsonOutputConverterMapper.apply(sampleObject);

        // then
        assertThat(convertedString).isEqualTo("{\"sampleField\":\"test\"}");
    }
}