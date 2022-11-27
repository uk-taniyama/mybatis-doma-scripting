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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.seasar.doma.jdbc.dialect.Dialect;

/**
 * Configuration class for {@link Driver}.
 *
 * @author Kazuki Shimizu
 * @since 2.1.0
 */
public class DomaLanguageDriverConfig {

  private static final String PROPERTY_KEY_CONFIG_FILE = "mybatis-doma.config.file";
  private static final String DEFAULT_PROPERTIES_FILE = "mybatis-doma.properties";

  private static final Log log = LogFactory.getLog(DomaLanguageDriverConfig.class);

  Dialect dialect = null;

  /**
   * Create an instance from default properties file. <br>
   * If you want to customize a default {@link RuntimeInstance}, you can configure some property
   * using mybatis-doma.properties that encoded by UTF-8. Also, you can change the properties file
   * that will read using system property (-Dmybatis-doma.config.file=...
   * -Dmybatis-doma.config.encoding=...). <br>
   * Supported properties are as follows:
   *
   * <table border="1">
   * <caption>Supported properties</caption>
   * <tr>
   * <th>Property Key</th>
   * <th>Description</th>
   * <th>Default</th>
   * </tr>
   * <tr>
   * <th colspan="3">Directive configuration</th>
   * </tr>
   * <tr>
   * <td>dialect</td>
   * <td>org.seasar.doma.jdbc.dialect.Dialect.</td>
   * <td>None(StandardDialect)</td>
   * </tr>
   * <tr>
   * </table>
   *
   * @return a configuration instance
   */
  public static DomaLanguageDriverConfig newInstance() {
    return newInstance(loadDefaultProperties());
  }

  /**
   * Create an instance from specified properties.
   *
   * @param customProperties custom configuration properties
   * @return a configuration instance
   * @see #newInstance()
   */
  public static DomaLanguageDriverConfig newInstance(Properties customProperties) {
    DomaLanguageDriverConfig config = new DomaLanguageDriverConfig();
    Properties properties = loadDefaultProperties();
    Optional.ofNullable(customProperties).ifPresent(properties::putAll);
    configure(config, properties);
    return config;
  }

  /**
   * Create an instance using specified customizer and override using a default properties file.
   *
   * @param customizer baseline customizer
   * @return a configuration instance
   * @see #newInstance()
   */
  public static DomaLanguageDriverConfig newInstance(
      Consumer<DomaLanguageDriverConfig> customizer) {
    DomaLanguageDriverConfig config = new DomaLanguageDriverConfig();
    Properties properties = loadDefaultProperties();
    customizer.accept(config);
    configure(config, properties);
    return config;
  }

  public void setDialect(Dialect dialect) {
    this.dialect = dialect;
  }

  public void setDialect(String dialect) {
    try {
      String className = dialect;
      log.debug("setDialect:" + dialect);
      if (className.indexOf('.') < 0) {
        className = "org.seasar.doma.jdbc.dialect." + dialect;
        if (!dialect.endsWith("Dialect")) {
          className += "Dialect";
        }
      }
      Class<?> dialectClass = Class.forName(className);
      if (!Dialect.class.isAssignableFrom(dialectClass)) {
        throw new Exception();
      }
      this.dialect = (Dialect) dialectClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Invalid dialect:" + dialect, e);
    }
  }

  private static void configure(DomaLanguageDriverConfig config, Properties properties) {
    properties.forEach(
        (name, value) -> {
          if (name.equals("dialect")) {
            config.setDialect(value.toString());
          }
        });
  }

  private static Properties loadDefaultProperties() {
    return loadProperties(System.getProperty(PROPERTY_KEY_CONFIG_FILE, DEFAULT_PROPERTIES_FILE));
  }

  private static Properties loadProperties(String resourcePath) {
    Properties properties = new Properties();
    InputStream in;
    try {
      in = Resources.getResourceAsStream(resourcePath);
    } catch (IOException e) {
      in = null;
    }
    if (in != null) {
      Charset encoding = StandardCharsets.UTF_8;
      try (InputStreamReader inReader = new InputStreamReader(in, encoding);
          BufferedReader bufReader = new BufferedReader(inReader)) {
        properties.load(bufReader);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    return properties;
  }
}
