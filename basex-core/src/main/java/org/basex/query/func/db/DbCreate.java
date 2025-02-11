package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DbCreate extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(0, false, qc);

    final StringList paths = new StringList();
    if(exprs.length > 2) {
      final Iter iter = exprs[2].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        paths.add(toDbPath(toString(item)));
      }
    }

    final NewInput[] inputs;
    if(exprs.length > 1) {
      final Value value = exprs[1].value(qc);
      final long is = value.size(), ps = paths.size();
      // if paths are supplied, number of specified inputs and paths must be identical
      if(ps != 0 && is != ps) throw DB_ARGS_X_X.get(info, is, ps);

      inputs = new NewInput[(int) is];
      for(int i = 0; i < is; i++) {
        qc.checkStop();
        inputs[i] = toNewInput(value.itemAt(i), i < ps ? paths.get(i) : "");
      }
    } else {
      inputs = new NewInput[0];
    }

    final Options opts = toOptions(3, new Options(), qc);
    qc.updates().add(new DBCreate(name, inputs, opts, qc, info), qc);
    return Empty.VALUE;
  }
}
