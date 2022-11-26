package org.mybatis.scripting.doma;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.seasar.doma.internal.expr.Value;

public class VariableValuesTest {
    Configuration configuration = new Configuration();

    @Test
    void testGetValueForMap() {
        VariableValues variableValues = new VariableValues(configuration, Map.of("name", "value"));
        // exist
        Value value = variableValues.getValue("name");
        assertEquals("value", value.getValue());
        assertEquals(String.class, value.getType());
        // not found
        assertEquals(null, variableValues.getValue("null"));
    }

    class Bean {
        String name;
        public String getName1() {
            return name;
        }
    }

    @Test
    void testGetValueForBean() {
        Bean bean = new Bean();
        bean.name = "value";
        VariableValues variableValues = new VariableValues(configuration, bean);
        // by member
        Value value = variableValues.getValue("name");
        assertEquals("value", value.getValue());
        assertEquals(String.class, value.getType());
        // by getter
        Value value1 = variableValues.getValue("name1");
        assertEquals("value", value1.getValue());
        assertEquals(String.class, value1.getType());
        // not found
        assertEquals(null, variableValues.getValue("null"));
    }
}
