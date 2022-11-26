package org.mybatis.scripting.doma;

import org.seasar.doma.internal.expr.Value;

public interface DomaVariableValues {
  Value getValue(String name);
}
