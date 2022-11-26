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

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.ibatis.scripting.ScriptingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DomaLanguageDriverConfigTest {

  private String currentConfigFile;
  private String currentConfigEncoding;

  @BeforeEach
  void saveCurrentConfig() {
    currentConfigFile = System.getProperty("mybatis-velocity.config");
    currentConfigEncoding = System.getProperty("mybatis-velocity.config.encoding");
  }

  @AfterEach
  void restoreConfig() {
    if (currentConfigFile == null) {
      System.clearProperty("mybatis-velocity.config.file");
    } else {
      System.setProperty("mybatis-velocity.config.file", currentConfigFile);
    }
    if (currentConfigEncoding == null) {
      System.clearProperty("mybatis-velocity.config.encoding");
    } else {
      System.setProperty("mybatis-velocity.config.encoding", currentConfigEncoding);
    }
  }

  @Test
  void newInstanceWithEmptyPropertiesFile() {
    System.setProperty("mybatis-velocity.config.file", "mybatis-velocity-empty.properties");
    DomaLanguageDriverConfig config = DomaLanguageDriverConfig.newInstance();
    Assertions.assertEquals(0, config.getAdditionalContextAttributes().size());
    Assertions.assertEquals(0, config.getVelocitySettings().size());
    // Assertions.assertEquals("class",
    // config.getVelocitySettings().get("resource.loaders"));
    // Assertions.assertEquals(
    // "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader",
    // config.getVelocitySettings().get("resource.loader.class.class"));
  }

  @Test
  void newInstanceWithPropertiesFileNotFound() {
    System.setProperty("mybatis-velocity.config.file", "mybatis-velocity-notfound.properties");
    DomaLanguageDriverConfig config = DomaLanguageDriverConfig.newInstance();
    Assertions.assertEquals(0, config.getAdditionalContextAttributes().size());
    Assertions.assertEquals(0, config.getVelocitySettings().size());
    // Assertions.assertEquals("class",
    // config.getVelocitySettings().get("resource.loaders"));
    // Assertions.assertEquals(
    // "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader",
    // config.getVelocitySettings().get("resource.loader.class.class"));
  }

  @Test
  void newInstanceWithCustomPropertiesFile() {
    System.setProperty("mybatis-velocity.config.file", "mybatis-velocity-custom.properties");
    DomaLanguageDriverConfig config = DomaLanguageDriverConfig.newInstance();
    Assertions.assertEquals(2, config.getAdditionalContextAttributes().size());
    Assertions.assertEquals(
        "org.mybatis.scripting.doma.use.TrailingWildCardFormatter",
        config.getAdditionalContextAttributes().get("trailingWildCardFormatter"));
    Assertions.assertEquals(
        "org.mybatis.scripting.doma.use.EnumBinder",
        config.getAdditionalContextAttributes().get("enumBinder"));
    // Assertions.assertEquals(
    // "attribute1Value",
    // config.getAdditionalContextAttributes().get("attribute1"));
    // Assertions.assertEquals(
    // "attribute2Value",
    // config.getAdditionalContextAttributes().get("attribute2"));
    Assertions.assertEquals(6, config.getVelocitySettings().size());
    // Assertions.assertEquals("class",
    // config.getVelocitySettings().get("resource.loaders"));
    // Assertions.assertEquals(
    // "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader",
    // config.getVelocitySettings().get("resource.loader.class.class"));
    Assertions.assertEquals(
        "Windows-31J", config.getVelocitySettings().get("resource.default_encoding"));
    Assertions.assertEquals(
        "100", config.getVelocitySettings().get("resource.manager.cache.default_size"));
    Assertions.assertEquals("20", config.getVelocitySettings().get("directive.foreach.max_loops"));
    Assertions.assertEquals(
        "org.apache.velocity", config.getVelocitySettings().get("runtime.log.name"));
  }

  @Test
  void newInstanceWithCustomProperties() {
    Properties properties = new Properties();
    properties.setProperty(
        "additional-context-attributes.trailingWildCardFormatter",
        "org.mybatis.scripting.doma.use.TrailingWildCardFormatter");
    properties.setProperty(
        "additional-context-attributes.enumBinder",
        "org.mybatis.scripting.doma.use.EnumBinder");
    properties.setProperty(
        "velocity-settings.resource.default_encoding", StandardCharsets.ISO_8859_1.name());
    properties.setProperty("velocity-settings.resource.manager.cache.default_size", "200");
    properties.setProperty(
        "additional.context.attributes",
        "attribute1 : attribute1Value , attribute2 : attribute2Value");
    properties.setProperty("directive.foreach.max_loops", "30");
    properties.setProperty("runtime.log.name", "org.apache.velocity");
    DomaLanguageDriverConfig config = DomaLanguageDriverConfig.newInstance(properties);
    Assertions.assertEquals(2, config.getAdditionalContextAttributes().size());
    Assertions.assertEquals(
        "org.mybatis.scripting.doma.use.TrailingWildCardFormatter",
        config.getAdditionalContextAttributes().get("trailingWildCardFormatter"));
    Assertions.assertEquals(
        "org.mybatis.scripting.doma.EnumBinder",
        config.getAdditionalContextAttributes().get("enumBinder"));
    // Assertions.assertEquals(
    // "attribute1Value",
    // config.getAdditionalContextAttributes().get("attribute1"));
    // Assertions.assertEquals(
    // "attribute2Value",
    // config.getAdditionalContextAttributes().get("attribute2"));
    Assertions.assertEquals(6, config.getVelocitySettings().size());
    // Assertions.assertEquals("class",
    // config.getVelocitySettings().get("resource.loaders"));
    // Assertions.assertEquals(
    //     null,
    //     config.getVelocitySettings().get("resource.loader.class.class"));
    Assertions.assertEquals(
        StandardCharsets.ISO_8859_1.name(),
        config.getVelocitySettings().get("resource.default_encoding"));
    Assertions.assertEquals(
        "200", config.getVelocitySettings().get("resource.manager.cache.default_size"));
    Assertions.assertEquals("30", config.getVelocitySettings().get("directive.foreach.max_loops"));
    Assertions.assertEquals(
        "org.apache.velocity", config.getVelocitySettings().get("runtime.log.name"));
  }

  @Test
  void newInstanceWithLegacyPropertiesFile() {
    System.setProperty("mybatis-velocity.config.file", "mybatis-velocity-legacy.properties");
    DomaLanguageDriverConfig config = DomaLanguageDriverConfig.newInstance();
    Assertions.assertEquals(0, config.getAdditionalContextAttributes().size());
    // Assertions.assertEquals(
    // "org.mybatis.scripting.doma.use.TrailingWildCardFormatter",
    // config.getAdditionalContextAttributes().get("trailingWildCardFormatter"));
    // Assertions.assertEquals(
    // "org.mybatis.scripting.doma.use.EnumBinder",
    // config.getAdditionalContextAttributes().get("enumBinder"));
    Assertions.assertEquals(4, config.getVelocitySettings().size());
    System.out.println(config.getVelocitySettings());
    // Assertions.assertEquals("class",
    // config.getVelocitySettings().get("resource.loaders"));
    // Assertions.assertEquals(
    // "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader",
    // null,
    // config.getVelocitySettings().get("resource.loader.class.class"));
    Assertions.assertEquals("20", config.getVelocitySettings().get("directive.foreach.max_loops"));
    Assertions.assertEquals(
        "org.apache.velocity", config.getVelocitySettings().get("runtime.log.name"));
  }

  @Test
  void newInstanceWithConsumer() {
    DomaLanguageDriverConfig config =
        DomaLanguageDriverConfig.newInstance(
            c -> c.getVelocitySettings().put("resource.default_encoding", "Windows-31J"));
    Assertions.assertEquals(2, config.getVelocitySettings().size());
    System.out.println(config.getVelocitySettings());
    // Assertions.assertEquals("class", config.getVelocitySettings().get("resource.loaders"));
    // Assertions.assertEquals(
    //     "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader",
    //     config.getVelocitySettings().get("resource.loader.class.class"));
    Assertions.assertEquals(
        "Windows-31J", config.getVelocitySettings().get("resource.default_encoding"));
  }

  // @Test
  void invalidAdditionalContextAttributeValue() {
    {
      Properties properties = new Properties();
      properties.setProperty("additional.context.attributes", "");
      try {
        DomaLanguageDriverConfig.newInstance(properties);
        Assertions.fail();
      } catch (ScriptingException e) {
        Assertions.assertEquals(
            "Invalid additional context property '' on 'additional.context.attributes'. Must be specify by 'key:value' format.",
            e.getMessage());
      }
    }
    {
      Properties properties = new Properties();
      properties.setProperty("additional.context.attributes", "key");
      try {
        DomaLanguageDriverConfig.newInstance(properties);
        Assertions.fail();
      } catch (ScriptingException e) {
        Assertions.assertEquals(
            "Invalid additional context property 'key' on 'additional.context.attributes'. Must be specify by 'key:value' format.",
            e.getMessage());
      }
    }
    {
      Properties properties = new Properties();
      properties.setProperty("additional.context.attributes", "key:value:note");
      try {
        DomaLanguageDriverConfig.newInstance(properties);
        Assertions.fail();
      } catch (ScriptingException e) {
        Assertions.assertEquals(
            "Invalid additional context property 'key:value:note' on 'additional.context.attributes'. Must be specify by 'key:value' format.",
            e.getMessage());
      }
    }
  }
}