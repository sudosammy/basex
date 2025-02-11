package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
abstract class UserFn extends StandardFunc {
  /** Root node test. */
  static final QNm Q_INFO = new QNm(UserText.INFO);
  /** Root node test. */
  static final NameTest T_INFO = new NameTest(Q_INFO);

  /**
   * Checks if the specified expression contains valid database patterns.
   * @param i expression index
   * @param qc query context
   * @return patterns
   * @throws QueryException query exception
   */
  protected final StringList toPatterns(final int i, final QueryContext qc) throws QueryException {
    final StringList patterns = new StringList();
    if(exprs.length > i) {
      final Iter iter = exprs[i].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        final String pattern = toString(item);
        if(!pattern.isEmpty() && !Databases.validPattern(pattern))
          throw USER_PATTERN_X.get(info, pattern);
        patterns.add(pattern);
      }
    } else {
      patterns.add("");
    }
    return patterns;
  }

  /**
   * Evaluates an expression to a username.
   * @param i index of argument
   * @param qc query context
   * @return username
   * @throws QueryException query exception
   */
  protected final String toName(final int i, final QueryContext qc) throws QueryException {
    return toName(i, false, USER_NAME_X, qc);
  }

  /**
   * Checks if the specified expression references an existing user.
   * @param i expression index
   * @param qc query context
   * @return user
   * @throws QueryException query exception
   */
  protected final User toUser(final int i, final QueryContext qc) throws QueryException {
    final String name = toName(i, qc);
    final User user = qc.context.users.get(name);
    if(user != qc.context.user()) checkPerm(qc, Perm.ADMIN);
    if(user == null) throw USER_UNKNOWN_X.get(info, name);
    return user;
  }

  /**
   * Checks if the specified expression contains valid permissions.
   * @param i expression index
   * @param qc query context
   * @return permissions
   * @throws QueryException query exception
   */
  protected final ArrayList<Perm> toPermissions(final int i, final QueryContext qc)
      throws QueryException {

    final ArrayList<Perm> perms = new ArrayList<>();
    if(exprs.length > i) {
      final Iter iter = exprs[i].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        final String perm = toString(item);
        final Perm p = Perm.get(perm);
        if(p == null) throw USER_PERMISSION_X.get(info, perm);
        perms.add(p);
      }
    } else {
      perms.add(Perm.NONE);
    }
    return perms;
  }

  /**
   * Ensures that no user with the specified name is logged in.
   * @param i expression index
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  protected final String toInactiveName(final int i, final QueryContext qc) throws QueryException {
    final String name = toName(i, qc);
    toInactiveUser(qc.context.users.get(name), qc);
    return name;
  }

  /**
   * Ensures that the specified user is not logged in.
   * @param i expression index
   * @param qc query context
   * @return user
   * @throws QueryException query exception
   */
  protected final User toInactiveUser(final int i, final QueryContext qc) throws QueryException {
    return toInactiveUser(toUser(i, qc), qc);
  }

  /**
   * Ensures that the specified user is not logged in.
   * @param user user (can be {@code null})
   * @param qc query context
   * @return specified user
   * @throws QueryException query exception
   */
  private User toInactiveUser(final User user, final QueryContext qc) throws QueryException {
    if(user != null) {
      final String name = user.name();
      for(final ClientListener cl : qc.context.sessions) {
        if(cl.context().user().name().equals(name)) throw USER_LOGGEDIN_X.get(info, name);
      }
    }
    return user;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (!updating() || visitor.lock(Locking.USER)) && super.accept(visitor);
  }
}
