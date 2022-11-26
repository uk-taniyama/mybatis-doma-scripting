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
package org.mybatis.scripting.doma.use;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.scripting.doma.DomaFacade;

/** Just a test case. Not a real Doma implementation. */
class DomaLanguageTest {

  private static SqlSessionFactory sqlSessionFactory;

  @SuppressWarnings("unused")
  public enum IDS {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE
  }

  @BeforeAll
  static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:bname", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/mybatis/scripting/doma/use/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/mybatis/scripting/doma/use/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @AfterAll
  static void cleanup() {
    DomaFacade.destroy();
  }

  @Test
  void testDynamicSelectWithPropertyParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("org.mybatis.scripting.doma.use.selectNames", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }

      p = new Parameter(false, "Fli%");
      answer = sqlSession.selectList("org.mybatis.scripting.doma.use.selectNames", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertNull(n.getLastName());
      }

      p = new Parameter(false, "Rub%");
      answer = sqlSession.selectList("org.mybatis.scripting.doma.use.selectNames", p);
      assertEquals(2, answer.size());
      for (Name n : answer) {
        assertNull(n.getLastName());
      }
    }
  }

  @Test
  void testDynamicSelectWithExpressionParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Parameter p = new Parameter(true, "Fli");
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithExpressions", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }

      p = new Parameter(false, "Fli");
      answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithExpressions", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertNull(n.getLastName());
      }

      p = new Parameter(false, "Rub");
      answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithExpressions", p);
      assertEquals(2, answer.size());
      for (Name n : answer) {
        assertNull(n.getLastName());
      }
    }
  }

  @Test
  void testSelectNamesWithFormattedParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Parameter p = new Parameter(true, "Fli");
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithFormattedParam", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testEnumBinding() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      List<Name> answer = sqlSession.selectList("org.mybatis.scripting.doma.use.selectEnumBinding");
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testSelectNamesWithFormattedParamSafe() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Parameter p = new Parameter(true, "Fli");
      List<Name> answer =
          sqlSession.selectList(
              "org.mybatis.scripting.doma.use.selectNamesWithFormattedParamSafe", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testDynamicSelectWithIteration() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Integer[] ids = {2, 4, 5};
      Map<String, Integer[]> param = new HashMap<>();
      param.put("ids", ids);
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithIteration", param);
      assertEquals(3, answer.size());
      for (int i = 0; i < ids.length; i++) {
        assertEquals(ids[i], answer.get(i).getId());
      }
    }
  }

  @Test
  void testEmptyWhere() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      int[] ids = {};
      Map<String, int[]> param = new HashMap<>();
      param.put("ids", ids);
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithIteration", param);
      assertEquals(5, answer.size());
    }
  }

  @Test
  void testTrim() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      int[] ids = {};
      Map<String, int[]> param = new HashMap<>();
      param.put("ids", ids);
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectWithTrim", param);
      assertEquals(5, answer.size());
    }
  }

  @Test
  void testDynamicSelectWithIterationOverMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Map<Integer, String> ids = new HashMap<>();
      ids.put(2, "Wilma");
      ids.put(4, "Barney");
      ids.put(5, "Betty");
      Map<String, Map<Integer, String>> param = new HashMap<>();
      param.put("ids", ids);
      List<Name> answer =
          sqlSession.selectList(
              "org.mybatis.scripting.doma.use.selectNamesWithIterationOverMap", param);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals(ids.get(n.getId()), n.getFirstName());
      }
    }
  }

  @Test
  void testDynamicSelectWithIterationComplex() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Name[] names = {new Name(2), new Name(4), new Name(5)};
      Map<String, Name[]> param = new HashMap<>();
      param.put("names", names);
      List<Name> answer =
          sqlSession.selectList(
              "org.mybatis.scripting.doma.use.selectNamesWithIterationComplex", param);
      assertEquals(3, answer.size());
      for (int i = 0; i < names.length; i++) {
        assertEquals(names[i].getId(), answer.get(i).getId());
      }
    }
  }

  @Test
  void testDynamicSelectWithIterationBoundary() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      List<Name> names = new ArrayList<>();
      for (int i = 0; i < 1001; i++) {
        names.add(new Name(i));
      }

      Map<String, List<Name>> param = new HashMap<>();
      param.put("names", names);
      List<Name> answer =
          sqlSession.selectList(
              "org.mybatis.scripting.doma.use.selectNamesWithIterationComplex", param);
      assertEquals(5, answer.size());
    }
  }

  @Test
  void testSelectKey() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Name fred = new Name();
      fred.setFirstName("Fred");
      fred.setLastName("Flinstone");
      sqlSession.insert("org.mybatis.scripting.doma.use.insertName", fred);
      assertTrue(fred.getId() != 0);
    }
  }

  @Test
  void testSelectWithCustomUserDirective() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, List<Name>> param = new HashMap<>();
      List<Name> answer =
          sqlSession.selectList(
              "org.mybatis.scripting.doma.use.selectWithCustomUserDirective", param);
      assertEquals(5, answer.size());
    }
  }

  @Test
  void testDynamicSelectWithInDirectiveForOneThousandPlusOne() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Name> names = new ArrayList<>();
      for (int i = 0; i < 1001; i++) {
        names.add(new Name(i + 1));
      }

      Map<String, List<Name>> param = new HashMap<>();
      param.put("names", names);
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithInDirective", param);
      assertEquals(5, answer.size());
    }
  }

  @Test
  void testDynamicSelectWithInDirectiveForOneThousand() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Name> names = new ArrayList<>();
      for (int i = 0; i < 1000; i++) {
        names.add(new Name(i + 1));
      }

      Map<String, List<Name>> param = new HashMap<>();
      param.put("names", names);
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithInDirective", param);
      assertEquals(5, answer.size());
    }
  }

  @Test
  void testDynamicSelectWithInDirectiveForOneThousandMinusOne() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Name> names = new ArrayList<>();
      for (int i = 0; i < 999; i++) {
        names.add(new Name(i + 1));
      }

      Map<String, List<Name>> param = new HashMap<>();
      param.put("names", names);
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithInDirective", param);
      assertEquals(5, answer.size());
    }
  }

  @Test
  void testDynamicSelectWithInDirectiveForOneItem() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Name> names = new ArrayList<>();
      for (int i = 0; i < 1; i++) {
        names.add(new Name(i + 1));
      }

      Map<String, List<Name>> param = new HashMap<>();
      param.put("names", names);
      List<Name> answer =
          sqlSession.selectList("org.mybatis.scripting.doma.use.selectNamesWithInDirective", param);
      assertEquals(1, answer.size());
    }
  }

  // @Test
  // void testAdditionalContextAttributes() {
  //   Object template = DomaFacade.compile("SELECT * FROM users WHERE id = /* id */1", "test");
  //   Map<String, Object> context = new HashMap<>();
  //   context.put("id", 123);
  //   String sql = DomaFacade.apply(template, context);
  //   assertEquals(1, context.size());
  //   assertEquals("SELECT * FROM users WHERE id = 123", sql);
  // }
}
