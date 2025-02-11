package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Lukas Kircher
 */
public final class CryptoHmac extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toBytes(exprs[0], qc);
    final byte[] key = toBytes(exprs[1], qc);
    final String algorithm = toString(exprs[2], qc);
    final String encoding = exprs.length == 4 ? toString(exprs[3], qc) : null;
    return new Encryption(info).hmac(value, key, algorithm, encoding);
  }
}
