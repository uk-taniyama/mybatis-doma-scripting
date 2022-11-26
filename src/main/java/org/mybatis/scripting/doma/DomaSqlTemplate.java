package org.mybatis.scripting.doma;

import java.util.Objects;
import java.util.function.Function;
import javax.sql.DataSource;
import org.seasar.doma.internal.expr.ExpressionEvaluator;
import org.seasar.doma.internal.jdbc.sql.NodePreparedSqlBuilder;
import org.seasar.doma.internal.jdbc.sql.SqlParser;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SqlKind;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.SqlNode;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.StandardDialect;

/** Represents a SQL template. */
public class DomaSqlTemplate {
  private final String sql;
  private final Config config;

  /**
   * @param sql a template. Must not be null.
   */
  public DomaSqlTemplate(String sql) {
    this(sql, new StandardDialect());
  }

  /**
   * @param sql a template. Must not be null.
   * @param dialect a dialect. Must not be null.
   */
  public DomaSqlTemplate(String sql, Dialect dialect) {
    this(
        sql,
        new Config() {

          @Override
          public DataSource getDataSource() {
            throw new UnsupportedOperationException();
          }

          @Override
          public Dialect getDialect() {
            return dialect;
          }
        });
    Objects.requireNonNull(dialect);
  }

  /**
   * @param sql a template. Must not be null.
   * @param config a configuration. Must not be null.
   */
  public DomaSqlTemplate(String sql, Config config) {
    this.sql = Objects.requireNonNull(sql);
    this.config = Objects.requireNonNull(config);
  }

  /**
   * Creates a SQL statement from this template.
   *
   * @return a SQL statement. Must not be null.
   */
  public PreparedSql execute(DomaVariableValues values) {
    SqlParser parser = new SqlParser(sql);
    SqlNode node = parser.parse();
    NodePreparedSqlBuilder builder = createNodePreparedSqlBuilder(values);
    return builder.build(node, Function.identity());
  }

  private NodePreparedSqlBuilder createNodePreparedSqlBuilder(DomaVariableValues values) {
    ExpressionEvaluator evaluator =
        new DomaExpressionEvaluator(
            values, config.getDialect().getExpressionFunctions(), config.getClassHelper());
    return new NodePreparedSqlBuilder(
        config, SqlKind.SCRIPT, null, evaluator, SqlLogType.FORMATTED);
  }
}
