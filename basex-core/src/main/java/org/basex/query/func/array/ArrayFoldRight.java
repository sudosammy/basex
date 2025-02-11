package org.basex.query.func.array;

import java.util.*;

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
public final class ArrayFoldRight extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(exprs[0], qc);
    Value result = exprs[1].value(qc);
    final FItem action = toFunction(exprs[2], 2, qc);

    for(final ListIterator<Value> iter = array.iterator(array.arraySize()); iter.hasPrevious();) {
      result = action.invoke(qc, info, iter.previous(), result);
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = exprs[0], zero = exprs[1];
    if(array == XQArray.empty()) return zero;

    FnFoldLeft.opt(this, cc, true, false);
    return this;
  }
}
