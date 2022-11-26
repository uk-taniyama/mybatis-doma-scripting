package org.mybatis.scripting.velocity;

import org.apache.ibatis.session.Configuration;
import org.seasar.doma.internal.expr.Value;

public class VariableValues implements DomaVariableValues {
  final ParameterResolver resolver;

  public VariableValues(Configuration configuration, Object parameterObject) {
    this.resolver = ParameterResolver.of(configuration, parameterObject);
  }

  @Override
  public Value getValue(String name) {
    Object obj = resolver.apply(name);
    if (obj == null) {
      return null;
    }
    return new Value(obj.getClass(), obj);
  }
}
