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
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.session.Configuration;
import org.seasar.doma.internal.expr.Value;
import org.seasar.doma.template.SqlStatement;

public class VelocityFacade {

  private static final Map<String, Object> additionalCtxAttributes = new HashMap<>();
  private static final Logger logger = Logger.getLogger(VelocityFacade.class.getName());

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

  // private ParameterMapping buildParameterMapping(String content) {
  // Map<String, String> propertiesMap = parseParameterMapping(content);
  // String property = propertiesMap.get("property");
  // String jdbcType = propertiesMap.get("jdbcType");
  // Class<?> propertyType;
  // if (this.typeHandlerRegistry.hasTypeHandler(this.parameterType)) {
  // propertyType = this.parameterType;
  // } else if (JdbcType.CURSOR.name().equals(jdbcType)) {
  // propertyType = java.sql.ResultSet.class;
  // } else if (property != null) {
  // ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
  // MetaClass metaClass = MetaClass.forClass(this.parameterType,
  // reflectorFactory);
  // if (metaClass.hasGetter(property)) {
  // propertyType = metaClass.getGetterType(property);
  // } else {
  // propertyType = Object.class;
  // }
  // } else {
  // propertyType = Object.class;
  // }
  // ParameterMapping.Builder builder =
  // new ParameterMapping.Builder(this.configuration, property, propertyType);
  // if (jdbcType != null) {
  // builder.jdbcType(resolveJdbcType(jdbcType));
  // }
  // Class<?> javaType = null;
  // String typeHandlerAlias = null;
  // for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
  // String name = entry.getKey();
  // String value = entry.getValue();
  // if ("javaType".equals(name)) {
  // javaType = resolveClass(value);
  // builder.javaType(javaType);
  // } else if ("jdbcType".equals(name)) {
  // builder.jdbcType(resolveJdbcType(value));
  // } else if ("mode".equals(name)) {
  // builder.mode(resolveParameterMode(value));
  // } else if ("numericScale".equals(name)) {
  // builder.numericScale(Integer.valueOf(value));
  // } else if ("resultMap".equals(name)) {
  // builder.resultMapId(value);
  // } else if ("typeHandler".equals(name)) {
  // typeHandlerAlias = value;
  // } else if ("jdbcTypeName".equals(name)) {
  // builder.jdbcTypeName(value);
  // } else if ("property".equals(name)) {
  // // Do Nothing
  // } else if ("expression".equals(name)) {
  // throw new BuilderException("Expression based parameters are not supported
  // yet");
  // } else {
  // throw new BuilderException(
  // "An invalid property '"
  // + name
  // + "' was found in mapping @{"
  // + content
  // + "}. Valid properties are "
  // + VALID_PROPERTIES);
  // }
  // }
  // if (typeHandlerAlias != null) {
  // builder.typeHandler(resolveTypeHandler(javaType, typeHandlerAlias));
  // }
  // return builder.build();
  // }

  // private Map<String, String> parseParameterMapping(String content) {
  // try {
  // return new ParameterExpression(content);
  // } catch (BuilderException ex) {
  // throw ex;
  // } catch (Exception ex) {
  // throw new BuilderException(
  // "Parsing error was found in mapping @{"
  // + content
  // + "}. Check syntax #{property|(expression), var1=value1, var2=value2, ...} ",
  // ex);
  // }
  // }

  public static interface Resolver extends Function<String, Object> {
    static Resolver nullResolver = (name) -> null;

    public static Resolver of(Configuration configuration, Object parameterObject) {
      if (parameterObject == null) {
        return nullResolver;
      }

      MetaObject metaObject = configuration.newMetaObject(parameterObject);
      boolean existsTypeHandler =
          configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
      return new BeanResolver(metaObject, existsTypeHandler);
    }

    class BeanResolver implements Resolver {
      private final MetaObject parameterMetaObject;
      private final boolean fallbackParameterObject;

      BeanResolver(MetaObject parameterMetaObject, boolean fallbackParameterObject) {
        this.parameterMetaObject = parameterMetaObject;
        this.fallbackParameterObject = fallbackParameterObject;
      }

      @Override
      public Object apply(String key) {
        if (fallbackParameterObject && !parameterMetaObject.hasGetter(key)) {
          return parameterMetaObject.getOriginalObject();
        } else {
          return parameterMetaObject.getValue(key);
        }
      }
    }
  }

  public static BoundSql getBoundSql(
      Configuration configuration,
      ParameterMappingCollector pmc,
      Object template,
      Object parameterObject) {
    String script = (String) template;
    logger.info("getBoudSql:Srouce:" + script);
    DomaSqlTemplate sqlTemplate = new DomaSqlTemplate(script);
    // BeanWrapper wrapper = new BeanWrapper(parameterObject);
    // ParameterHandler parameterHandler = new DefaultParameterHandler();
    // context.forEach(
    //     (name, value) -> {
    //       if(value!=null) {
    //         System.out.println(name +":" + value);
    //         sqlTemplate.add(name, value.getClass(), value);
    //       }
    //     });
    Resolver resolver = Resolver.of(configuration, parameterObject);
    SqlStatement sqlStatement =
        sqlTemplate.execute(
            name -> {
              Object obj = resolver.apply(name);
              if (obj == null) {
                return null;
              }
              return new Value(obj.getClass(), obj);
            });
    String sql = sqlStatement.getFormattedSql();
    logger.info("getBoudSql:Result:" + sql);
    BoundSql boundSql =
        new BoundSql(configuration, sql, pmc.getParameterMappings(), parameterObject);
    return boundSql;
  }
}
