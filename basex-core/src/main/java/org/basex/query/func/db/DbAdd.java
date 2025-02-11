package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DbAdd extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    String path = "";
    if(exprs.length > 2) {
      final String token = toStringOrNull(exprs[2], qc);
      if(token != null) path = toDbPath(token);
    }
    final NewInput input = toNewInput(toNodeOrAtomItem(1, qc), path);
    final Options opts = toOptions(3, new Options(), qc);

    qc.updates().add(new DBAdd(data, input, opts, false, qc, info), qc);
    return Empty.VALUE;
  }
}
