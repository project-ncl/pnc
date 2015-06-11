package org.jboss.pnc.datastore.predicates.rsql;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgumentHelperTest {

    static enum enumType {
        YES_IT_WORKS, NO_IT_DOESNT
    }

    static class InnerClass {
        public String simpleString;
    }

    static class TestClass {
        private Integer privateField;
        public Integer simpleIntField;
        InnerClass innerField;
        enumType enumType;
    }

    @Test
    public void shouldConvertSimpleValue() throws Exception {
        //given
        ArgumentHelper helper = new ArgumentHelper();

        //when
        Object returnedValue = helper.getConvertedType(TestClass.class, "simpleIntField", "1");

        //then
        assertThat(returnedValue).isInstanceOf(Integer.class);
        assertThat((Integer) returnedValue).isEqualTo(1);
    }

    @Test
    public void shouldConvertSimpleFieldEvenIfItIsPrivate() throws Exception {
        //given
        ArgumentHelper helper = new ArgumentHelper();

        //when
        Object returnedValue = helper.getConvertedType(TestClass.class, "privateField", "1");

        //then
        assertThat(returnedValue).isInstanceOf(Integer.class);
    }

    @Test
    public void shouldConvertSimpleInnerField() throws Exception {
        //given
        ArgumentHelper helper = new ArgumentHelper();

        //when
        Object returnedValue = helper.getConvertedType(TestClass.class, "innerField.simpleString", "test");

        //then
        assertThat(returnedValue).isInstanceOf(String.class);
    }

    @Test
    public void shouldConvertEnumField() throws Exception {
        //given
        ArgumentHelper helper = new ArgumentHelper();

        //when
        Object returnedValue = helper.getConvertedType(TestClass.class, "enumType", "YES_IT_WORKS");

        //then
        assertThat(returnedValue).isInstanceOf(enumType.class);
    }

}