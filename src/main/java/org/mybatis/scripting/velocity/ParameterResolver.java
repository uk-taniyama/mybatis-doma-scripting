package org.mybatis.scripting.velocity;

import java.util.function.Function;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

@FunctionalInterface
public interface ParameterResolver extends Function<String, Object> {
  static final ParameterResolver nullResolver = (name) -> null;

  public static ParameterResolver of(Configuration configuration, Object parameterObject) {
    if (parameterObject == null) {
      return nullResolver;
    }

    MetaObject metaObject = configuration.newMetaObject(parameterObject);
    boolean existsTypeHandler =
        configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
    return new BeanResolver(metaObject, existsTypeHandler);
  }

  class BeanResolver implements ParameterResolver {
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
