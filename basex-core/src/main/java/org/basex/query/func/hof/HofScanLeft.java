package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public final class HofScanLeft extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final FItem action = toFunction(exprs[2], 2, qc);

    return new Iter() {
      private Value acc = exprs[1].value(qc);
      private Iter inner = acc.iter();

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item in = inner.next();
          if(in != null) return in;
          final Item out = input.next();
          if(out == null) return null;

          acc = action.invoke(qc, info, acc, out);
          inner = acc.iter();
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = exprs[0], zero = exprs[1];
    if(input == Empty.VALUE) return zero;

    exprType.assign(input.seqType().union(Occ.ZERO)).data(input);
    return this;
  }
}
