package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnSubstringAfter extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(exprs[0], qc), sub = toZeroToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    if(value.length == 0) return Str.EMPTY;
    if(sub.length == 0) return Str.get(value);

    if(coll == null) {
      final int pos = indexOf(value, sub);
      return pos == -1 ? Str.EMPTY : Str.get(substring(value, pos + sub.length));
    }
    return Str.get(coll.after(value, sub, info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = exprs[0], sub = exprs[1];
    final SeqType st = value.seqType(), stSub = sub.seqType();

    if((st.zero() || st.one() && st.type.isStringOrUntyped()) &&
       (stSub.zero() || stSub.one() && stSub.type.isStringOrUntyped()) && exprs.length < 3) {
      if(value == Empty.VALUE || value == Str.EMPTY || value.equals(sub)) return Str.EMPTY;
      if(sub == Empty.VALUE || sub == Str.EMPTY) return cc.function(STRING, info, value);
    }
    return this;
  }
}
