/*
 *    Copyright 2012-2022 the original author or authors.
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
package org.mybatis.scripting.velocity;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.scripting.ScriptingException;
import org.seasar.doma.template.SqlStatement;
import org.seasar.doma.template.SqlTemplate;

public class VelocityFacade {

  private static final Map<String, Object> additionalCtxAttributes = new HashMap<>();

  private VelocityFacade() {
    // Prevent instantiation
  }

  /**
   * Initialize a template engine.
   *
   * @param driverConfig a language driver configuration
   * @since 2.1.0
   */
  public static void initialize(VelocityLanguageDriverConfig driverConfig) {
    Properties properties = new Properties();
    driverConfig.getVelocitySettings().forEach(properties::setProperty);
    additionalCtxAttributes.putAll(
        driverConfig.getAdditionalContextAttributes().entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    v -> {
                      try {
                        return Resources.classForName(v.getValue()).getConstructor().newInstance();
                      } catch (Exception e) {
                        throw new ScriptingException(
                            "Cannot load additional context attribute class.", e);
                      }
                    })));
  }

  /**
   * Destroy a template engine.
   *
   * @since 2.1.0
   */
  public static void destroy() {
    additionalCtxAttributes.clear();
  }

  public static Object compile(String script, String name) {
    try {
      return script;
    } catch (Exception ex) {
      throw new BuilderException("Error parsing velocity script '" + name + "'", ex);
    }
  }

  public static String apply(Object template, Map<String, Object> context) {
    String script = (String) template;
    SqlTemplate sqlTemplate = new SqlTemplate(script);
    context.forEach(
        (name, value) -> {
          sqlTemplate.add(name, value.getClass(), value);
        });
    SqlStatement sqlStatement = sqlTemplate.execute();
    return sqlStatement.getFormattedSql();
  }
}
