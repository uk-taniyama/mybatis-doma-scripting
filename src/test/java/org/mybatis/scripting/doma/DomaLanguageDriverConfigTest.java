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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.doma.jdbc.dialect.MssqlDialect;

class DomaLanguageDriverConfigTest {

  private String currentConfigFile;

  @BeforeEach
  void saveCurrentConfig() {
    currentConfigFile = System.getProperty("mybatis-doma.config");
  }

  @AfterEach
  void restoreConfig() {
    if (currentConfigFile == null) {
      System.clearProperty("mybatis-doma.config.file");
    } else {
      System.setProperty("mybatis-doma.config.file", currentConfigFile);
    }
  }

  @Test
  void newInstanceWithEmptyPropertiesFile() {
    System.setProperty("mybatis-doma.config.file", "mybatis-doma-empty.properties");
    DomaLanguageDriverConfig config = DomaLanguageDriverConfig.newInstance();
    assertNull(config.dialect);
  }

  @Test
  void newInstanceWithPropertiesFileNotFound() {
    System.setProperty("mybatis-doma.config.file", "mybatis-doma-notfound.properties");
    DomaLanguageDriverConfig config = DomaLanguageDriverConfig.newInstance();
    assertNull(config.dialect);
  }

  @Test
  void newInstanceWithCustomPropertiesFile() {
    System.setProperty("mybatis-doma.config.file", "mybatis-doma-custom.properties");
    DomaLanguageDriverConfig config = DomaLanguageDriverConfig.newInstance();
    Assertions.assertEquals(MssqlDialect.class, config.dialect.getClass());
  }

  @Test
  void newInstanceWithProperties() {
    Properties properties = new Properties();
    properties.setProperty("dialect", "Unknown");
    assertThrowsExactly(
        IllegalStateException.class,
        () -> {
          DomaLanguageDriverConfig.newInstance(properties);
        },
        "Invalid dialect:Unknown");

    DomaLanguageDriverConfig config;

    // short name
    properties.setProperty("dialect", "Mssql");
    config = DomaLanguageDriverConfig.newInstance(properties);
    Assertions.assertEquals(MssqlDialect.class, config.dialect.getClass());

    // simple name
    properties.setProperty("dialect", "MssqlDialect");
    config = DomaLanguageDriverConfig.newInstance(properties);
    Assertions.assertEquals(MssqlDialect.class, config.dialect.getClass());

    // full name
    properties.setProperty("dialect", "org.seasar.doma.jdbc.dialect.MssqlDialect");
    config = DomaLanguageDriverConfig.newInstance(properties);
    Assertions.assertEquals(MssqlDialect.class, config.dialect.getClass());
  }
}
