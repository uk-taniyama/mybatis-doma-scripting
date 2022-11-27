/*
 *    Copyright 2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
    VariableValues variableValues =
        new VariableValues(configuration, Map.of("name", "value"), Map.class);
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
    VariableValues variableValues = new VariableValues(configuration, bean, Bean.class);
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
