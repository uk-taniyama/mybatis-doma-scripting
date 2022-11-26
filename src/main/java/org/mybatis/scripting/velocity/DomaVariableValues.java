package org.mybatis.scripting.velocity;

import org.seasar.doma.internal.expr.Value;

public interface DomaVariableValues {
  Value getValue(String name);
}
