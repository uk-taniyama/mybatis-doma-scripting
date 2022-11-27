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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.seasar.doma.internal.expr.Value;
import org.seasar.doma.jdbc.InParameter;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.dialect.MssqlDialect;

class DomaSqlTemplateTest {

  @Test
  void execute() {
    String sql = "select * from emp where name = /* name */'' and salary = /* salary */0";
    DomaSqlTemplate template = new DomaSqlTemplate(sql);
    PreparedSql preparedSql =
        template.execute(
            (name) -> {
              if (name.equals("name")) {
                return new Value(String.class, "abc");
              }
              if (name.equals("salary")) {
                return new Value(int.class, 1234);
              }
              fail("Unknown name:" + name);
              return null;
            });
    assertEquals("select * from emp where name = ? and salary = ?", preparedSql.getRawSql());
    assertEquals(
        "select * from emp where name = 'abc' and salary = 1234", preparedSql.getFormattedSql());
    List<InParameter<?>> params = preparedSql.getParameters();
    assertEquals(2, params.size());
    Iterator<InParameter<?>> iterator = params.iterator();
    InParameter<?> param1 = iterator.next();
    InParameter<?> param2 = iterator.next();
    assertEquals(String.class, param1.getWrapper().getBasicClass());
    assertEquals("abc", param1.getWrapper().get());
    assertEquals(Integer.class, param2.getWrapper().getBasicClass());
    assertEquals(1234, param2.getWrapper().get());
  }

  @Test
  void defaultDialect() {
    String sql = "select * from emp where name like /* @prefix(name) */'' escape '$'";
    PreparedSql preparedSql =
        new DomaSqlTemplate(sql)
            .execute(
                (name) -> {
                  if (name.equals("name")) {
                    return new Value(String.class, "a[b]%c");
                  }
                  fail("Unknown name:" + name);
                  return null;
                });

    assertEquals("select * from emp where name like ? escape '$'", preparedSql.getRawSql());
    assertEquals(
        "select * from emp where name like 'a[b]$%c%' escape '$'", preparedSql.getFormattedSql());
    List<InParameter<?>> params = preparedSql.getParameters();
    assertEquals(1, params.size());
    Iterator<InParameter<?>> iterator = params.iterator();
    InParameter<?> param1 = iterator.next();
    assertEquals(String.class, param1.getWrapper().getBasicClass());
    assertEquals("a[b]$%c%", param1.getWrapper().get());
  }

  @Test
  void msSqlDialect() {
    String sql = "select * from emp where name like /* @prefix(name) */'' escape '$'";
    PreparedSql preparedSql =
        new DomaSqlTemplate(sql, new MssqlDialect())
            .execute(
                (name) -> {
                  if (name.equals("name")) {
                    return new Value(String.class, "a[b]%c");
                  }
                  fail("Unknown name:" + name);
                  return null;
                });
    assertEquals("select * from emp where name like ? escape '$'", preparedSql.getRawSql());
    assertEquals(
        "select * from emp where name like 'a$[b]$%c%' escape '$'", preparedSql.getFormattedSql());
    List<InParameter<?>> params = preparedSql.getParameters();
    assertEquals(1, params.size());
    Iterator<InParameter<?>> iterator = params.iterator();
    InParameter<?> param1 = iterator.next();
    assertEquals(String.class, param1.getWrapper().getBasicClass());
    assertEquals("a$[b]$%c%", param1.getWrapper().get());
  }

  @Test
  void expandDirective() {
    String sql =
        "select /*%expand name */* from emp where name = /* name */'' and salary = /* salary */0";
    assertThrowsExactly(
        UnsupportedOperationException.class,
        () -> {
          new DomaSqlTemplate(sql)
              .execute(
                  (name) -> {
                    if (name.equals("name")) {
                      return new Value(String.class, "abc");
                    }
                    if (name.equals("salary")) {
                      return new Value(int.class, 1234);
                    }
                    fail("Unknown name:" + name);
                    return null;
                  });
        },
        "The '%expand' directive is not supported.");
  }

  @Test
  void populateDirective() {
    String sql = "update employee set /*%populate*/ id = id where age < 30";
    assertThrowsExactly(
        UnsupportedOperationException.class,
        () -> {
          new DomaSqlTemplate(sql)
              .execute(
                  (name) -> {
                    return null;
                  });
        },
        "The '%populate' directive is not supported.");
  }
}
