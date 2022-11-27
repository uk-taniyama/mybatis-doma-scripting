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

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.seasar.doma.internal.util.ResourceUtil;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.StandardDialect;

public class DomaSqlSource implements SqlSource {
  private static final Log log = LogFactory.getLog(DomaSqlSource.class);

  private final DomaLanguageDriverConfig driverConfig;
  private final Dialect dialect;
  private final String script;
  private final Configuration configuration;
  private final Class<?> parameterTypeClass;

  public DomaSqlSource(
      DomaLanguageDriverConfig driverConfig,
      Configuration newConfiguration,
      String script,
      Class<?> parameterTypeClass) {
    this.driverConfig = driverConfig;
    this.dialect =
        Optional.ofNullable(this.driverConfig.dialect).orElseGet(() -> new StandardDialect());
    this.script = getScript(script);
    this.configuration = newConfiguration;
    this.parameterTypeClass = parameterTypeClass;
  }

  public static String getScript(String script) {
    if (!script.startsWith("//")) {
      return script;
    }

    try {
      String text = ResourceUtil.getResourceAsString(script.substring(2));
      Objects.requireNonNull(text);
      return text;
    } catch (Exception e) {
      throw new BuilderException("Error parsing doma script '" + script + "'", e);
    }
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    log.debug("getBoundSql:Source:" + script);

    DomaSqlTemplate sqlTemplate = new DomaSqlTemplate(script, dialect);
    VariableValues variableValues =
        new VariableValues(configuration, parameterObject, parameterTypeClass);
    PreparedSql preparedSql = sqlTemplate.execute(variableValues);

    log.debug("getBoundSql:Result:" + preparedSql.getRawSql());

    String sql = preparedSql.getFormattedSql();
    return new BoundSql(configuration, sql, Collections.emptyList(), parameterObject);
  }
}
