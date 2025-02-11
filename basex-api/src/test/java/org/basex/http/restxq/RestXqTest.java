package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * This class contains RESTXQ tests.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class RestXqTest extends HTTPTest {
  /** Query header. */
  private static final String HEADER =
    "module  namespace m = 'http://basex.org/modules/restxq/test';" + Prop.NL +
    "declare namespace R = 'http://exquery.org/ns/restxq';" + Prop.NL;
  /** Counter. */
  private static int count;

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(HTTP_ROOT, true);
  }

  /**
   * Executes the specified GET request and tests the result.
   * @param expected expected result
   * @param function function to test
   * @param query request
   * @throws IOException I/O exception
   */
  protected static void get(final String expected, final String function, final String query)
      throws IOException {
    register(function);
    get(expected, query);
  }

  /**
   * Executes the specified GET request and tests for a status code.
   * @param status status code to check
   * @param function function to test
   * @param query request
   * @throws IOException I/O exception
   */
  protected static void get(final int status, final String function, final String query)
      throws IOException {
    register(function);
    get(status, query);
  }

  /**
   * Executes the specified POST request and tests the result.
   * @param expected expected result
   * @param function function to test
   * @param query request
   * @param payload payload
   * @param type media type
   * @throws IOException I/O exception
   */
  protected static void post(final String expected, final String function, final String query,
      final String payload, final MediaType type) throws IOException {
    register(function);
    assertEquals(expected, post(payload, type, query));
  }

  /**
   * Installs a new module and removes all others.
   * @param function function to be tested
   * @throws IOException I/O exception
   */
  protected static void register(final String function) throws IOException {
    // delete old module
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    for(final IOFile f : new IOFile(path).children()) assertTrue(f.delete());
    // create new module
    module().write(HEADER + function);
    // invalidate module cache
    WebModules.get(context).init(false);
  }

  /**
   * Returns the XQuery test module.
   * @return test module
   */
  private static IOFile module() {
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    return new IOFile(path, NAME + count++ + IO.XQMSUFFIX);
  }
}
