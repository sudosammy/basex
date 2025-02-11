package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ArrayFoldLeft extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(exprs[0], qc);
    Value result = exprs[1].value(qc);
    final FItem action = toFunction(exprs[2], 2, qc);

    for(final Value value : array.members()) {
      result = action.invoke(qc, info, result, value);
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = exprs[0], zero = exprs[1];
    if(array == XQArray.empty()) return zero;

    FnFoldLeft.opt(this, cc, true, true);
    return this;
  }
}
