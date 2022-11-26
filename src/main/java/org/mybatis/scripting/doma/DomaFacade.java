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
package org.mybatis.scripting.doma;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.StandardDialect;

public class DomaFacade {

  private static final Log log = LogFactory.getLog(DomaFacade.class);
  private static Dialect dialect = null;

  /**
   * Initialize a template engine.
   *
   * @param driverConfig a language driver configuration
   * @since 2.1.0
   */
  public static void initialize(DomaLanguageDriverConfig driverConfig) {
    if (driverConfig.dialect != null) {
      DomaFacade.dialect = driverConfig.dialect;
    } else {
      DomaFacade.dialect = new StandardDialect();
    }
  }

  /**
   * Destroy a template engine.
   *
   * @since 2.1.0
   */
  public static void destroy() {
    DomaFacade.dialect = null;
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
    log.debug("getBoundSql:Source:" + script);
    DomaSqlTemplate sqlTemplate = new DomaSqlTemplate(script, dialect);
    VariableValues variableValues = new VariableValues(configuration, parameterObject);
    PreparedSql preparedSql = sqlTemplate.execute(variableValues);
    String sql = preparedSql.getFormattedSql();
    log.debug("getBoundSql:Result:" + preparedSql.getRawSql());
    BoundSql boundSql =
        new BoundSql(configuration, sql, pmc.getParameterMappings(), parameterObject);
    return boundSql;
  }
}
