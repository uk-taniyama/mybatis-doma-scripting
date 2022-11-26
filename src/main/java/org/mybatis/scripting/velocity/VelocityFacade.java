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
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.session.Configuration;
import org.seasar.doma.template.SqlStatement;

public class VelocityFacade {

  private static final Map<String, Object> additionalCtxAttributes = new HashMap<>();
  private static final Log log = LogFactory.getLog(VelocityFacade.class);

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

  public static BoundSql getBoundSql(
      Configuration configuration,
      ParameterMappingCollector pmc,
      Object template,
      Object parameterObject) {
    String script = (String) template;
    log.warn("getBoundSql:Source:" + script);
    DomaSqlTemplate sqlTemplate = new DomaSqlTemplate(script);
    VariableValues variableValues = new VariableValues(configuration, parameterObject);
    SqlStatement sqlStatement = sqlTemplate.execute(variableValues);
    String sql = sqlStatement.getFormattedSql();
    log.warn("getBoundSql:Result:" + sqlStatement.getFormattedSql());
    // log.warn("getBoundSql:Result:" + sqlStatement.getArguments().forEach(null););
    BoundSql boundSql =
        new BoundSql(configuration, sql, pmc.getParameterMappings(), parameterObject);
    return boundSql;
  }
}
