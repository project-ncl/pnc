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
package org.jboss.pnc.rest.notifications.websockets;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class JSonOutputConverterTest {

    @Test
    public void shouldConvertProperObject() throws Exception {
        //given
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

        JSonOutputConverter converter = new JSonOutputConverter();

        //when
        String convertedSting = converter.apply(objectToConvert);

        //than
        assertThat(convertedSting).isEqualTo("{\"sampleField\":\"test\"}");
    }

    @Test
    public void shouldFailWhenThereAreNoProperties() throws Exception {
        //given
        class SampleObject {
            String sampleField;
        }

        SampleObject objectToConvert = new SampleObject();
        objectToConvert.sampleField = "test";

        JSonOutputConverter converter = new JSonOutputConverter();

        //when//then
        try {
            converter.apply(objectToConvert);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void shouldNotFailWhenPassingNull() throws Exception {
        //given
        JSonOutputConverter converter = new JSonOutputConverter();

        //when
        String convertedString = converter.apply(null);

        //then
        assertThat(convertedString).isEqualTo("{}");
    }

    @Test
    public void shouldNotRenderNulls() throws Exception {
        //given
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
        JSonOutputConverter converter = new JSonOutputConverter();

        //when
        String convertedString = converter.apply(sampleObject);

        //then
        assertThat(convertedString).isEqualTo("{\"sampleField\":\"test\"}");
    }
}