/*
 *    Copyright 2022 the original author or authors.
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

import org.seasar.doma.expr.ExpressionFunctions;
import org.seasar.doma.internal.expr.EvaluationResult;
import org.seasar.doma.internal.expr.ExpressionEvaluator;
import org.seasar.doma.internal.expr.ExpressionException;
import org.seasar.doma.internal.expr.Value;
import org.seasar.doma.internal.expr.node.ExpressionLocation;
import org.seasar.doma.internal.expr.node.VariableNode;
import org.seasar.doma.jdbc.ClassHelper;
import org.seasar.doma.message.Message;

public class DomaExpressionEvaluator extends ExpressionEvaluator {
  final DomaVariableValues variableValues;

  public DomaExpressionEvaluator(
      DomaVariableValues variableValues,
      ExpressionFunctions expressionFunctions,
      ClassHelper classHelper) {
    super(expressionFunctions, classHelper);
    this.variableValues = variableValues;
  }

  @Override
  public EvaluationResult visitVariableNode(VariableNode node, Void p) {
    String variableName = node.getExpression();
    Value value = variableValues.getValue(node.getExpression());
    if (value == null) {
      ExpressionLocation location = node.getLocation();
      throw new ExpressionException(
          Message.DOMA3003, location.getExpression(), location.getPosition(), variableName);
    }
    return new EvaluationResult(value.getValue(), value.getType());
  }
}
