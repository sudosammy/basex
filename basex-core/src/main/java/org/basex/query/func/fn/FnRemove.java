package org.basex.query.func.fn;

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
 * @author Christian Gruen
 */
public final class FnRemove extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final long pos = toLong(exprs[1], qc), size = input.size();

    // position out of bounds: return original value
    if(pos <= 0 || size != -1 && pos > size) return input;
    // value-based iterator
    if(input.valueIter()) return value(input.value(qc, null), pos, qc).iter();

    return new Iter() {
      long c;

      @Override
      public Item next() throws QueryException {
        return ++c != pos || input.next() != null ? input.next() : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return input.get(i + 1 < pos ? i : i + 1);
      }
      @Override
      public long size() {
        return Math.max(-1, size - 1);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return value(exprs[0].value(qc), toLong(exprs[1], qc), qc);
  }

  /**
   * Returns the result value.
   * @param value original value
   * @param pos position of the item to remove
   * @param qc query context
   * @return resulting value
   */
  private static Value value(final Value value, final long pos, final QueryContext qc) {
    final long size = value.size();
    // position out of bounds: return original value
    if(pos <= 0 || pos > size) return value;
    // remove first or last item (size > 0)
    if(pos == 1 || pos == size) return value.subsequence(pos == 1 ? 1 : 0, size - 1, qc);
    // remove item at supplied position
    return ((Seq) value).remove(pos - 1, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values to speed up evaluation of result
    if(allAreValues(false)) return value(cc.qc);

    final Expr input = exprs[0], pos = exprs[1];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    long sz = -1;
    if(pos instanceof Value) {
      // position is static...
      final long p = toLong(pos, cc.qc);
      // return all items
      final long size = input.size();
      if(p < 1 || size > 0 && p > size) return input;
      // skip first item
      if(p == 1) return cc.function(Function.TAIL, info, input);
      // skip last item
      if(p == size) return cc.function(Function.TRUNK, info, input);
      // decrement result size
      sz--;
    }

    exprType.assign(st.union(Occ.ZERO), sz).data(input);
    return this;
  }

  @Override
  public boolean ddo() {
    return exprs[0].ddo();
  }
}
