package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value;
    if(exprs.length == 0) {
      final Item item = ctxValue(qc).item(qc, info);
      if(item instanceof FItem && !(item instanceof XQJava)) throw FISTRING_X.get(info, item);
      value = item == Empty.VALUE ? Token.EMPTY : item.string(info);
    } else {
      value = toZeroToken(exprs[0], qc);
    }
    return Int.get(Token.length(value));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // if(string-length(E))  ->  if(string(E))
    return cc.simplify(this, mode == Simplify.EBV ? cc.function(STRING, info, exprs) : this, mode);
  }
}
