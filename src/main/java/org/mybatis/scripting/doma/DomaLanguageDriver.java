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

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

/** The {@link LanguageDriver} using Doma */
public class DomaLanguageDriver implements LanguageDriver {
  private final DomaLanguageDriverConfig driverConfig;

  /** Default constructor. */
  public DomaLanguageDriver() {
    this(DomaLanguageDriverConfig.newInstance());
  }

  /**
   * Constructor.
   *
   * @param driverConfig a language driver configuration
   */
  public DomaLanguageDriver(DomaLanguageDriverConfig driverConfig) {
    this.driverConfig = driverConfig;
  }

  /** {@inheritDoc} */
  @Override
  public ParameterHandler createParameterHandler(
      MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
  }

  /** {@inheritDoc} */
  @Override
  public SqlSource createSqlSource(
      Configuration configuration, XNode script, Class<?> parameterTypeClass) {
    return createSqlSource(configuration, script.getNode().getTextContent(), parameterTypeClass);
  }

  /** {@inheritDoc} */
  @Override
  public SqlSource createSqlSource(
      Configuration configuration, String script, Class<?> parameterTypeClass) {
    return new DomaSqlSource(
        driverConfig,
        configuration,
        script,
        parameterTypeClass == null ? Object.class : parameterTypeClass);
  }
}
