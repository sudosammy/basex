package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class UserCreate extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toInactiveName(0, qc);
    final String password = toString(exprs[1], qc);
    final ArrayList<Perm> perms = toPermissions(2, qc);
    final StringList patterns = toPatterns(3, qc);

    final User user = new User(name, password);
    if(name.equals(UserText.ADMIN)) throw USER_ADMIN.get(info);

    if(exprs.length > 4) {
      final ANode node = toElem(exprs[4], qc);
      if(!T_INFO.matches(node)) throw ELM_X_X.get(info, Q_INFO.prefixId(), node);
      user.info(node.materialize(n -> false, info, qc));
    }

    qc.updates().add(new Create(user, perms, patterns, qc, info), qc);
    return Empty.VALUE;
  }

  /** Update primitive. */
  private static final class Create extends UserPermUpdate {
    /**
     * Constructor.
     * @param user user
     * @param perms permissions
     * @param patterns database patterns
     * @param qc query context
     * @param info input info
     * @throws QueryException query exception
     */
    private Create(final User user, final ArrayList<Perm> perms, final StringList patterns,
        final QueryContext qc, final InputInfo info) throws QueryException {
      super(UpdateType.USERCREATE, user, perms, patterns, qc, info);
    }

    @Override
    public void apply() {
      final User old = users.get(user.name());
      if(old != null) users.drop(old);
      users.add(user);
      grant();
    }

    @Override
    public String operation() {
      return "created";
    }
  }
}
