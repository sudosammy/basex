package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class BinInsertBefore extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 binary = toB64(exprs[0], qc, true);
    final Long offset = toLong(exprs[1], qc);
    final B64 extra = toB64(exprs[2], qc, true);
    if(binary == null) return Empty.VALUE;

    final byte[] bytes = binary.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(offset, null, bl);

    if(extra == null) return binary;
    final byte[] xtr = extra.binary(info);
    final int xl = xtr.length;

    final byte[] tmp = new byte[bl + xl];
    final int o = bounds[0];
    Array.copy(bytes, o, tmp);
    Array.copyFromStart(xtr, xl, tmp, o);
    Array.copy(bytes, o, bl - o, tmp, o + xl);
    return B64.get(tmp);
  }
}
