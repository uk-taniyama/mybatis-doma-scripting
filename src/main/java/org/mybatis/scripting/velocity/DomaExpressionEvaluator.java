package org.mybatis.scripting.velocity;

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

    public DomaExpressionEvaluator(DomaVariableValues variableValues, ExpressionFunctions expressionFunctions, ClassHelper classHelper) {
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
