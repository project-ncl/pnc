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
}