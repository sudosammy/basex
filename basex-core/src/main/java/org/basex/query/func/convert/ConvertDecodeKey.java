package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class ConvertDecodeKey extends ConvertIntegersToBase64 {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] key = toToken(exprs[0], qc);
    final boolean lax = exprs.length > 1 && toBoolean(exprs[1], qc);

    final byte[] string = XMLToken.decode(key, lax);
    if(string == null) throw CONVERT_KEY_X.get(info, key);
    return Str.get(string);
  }
}
