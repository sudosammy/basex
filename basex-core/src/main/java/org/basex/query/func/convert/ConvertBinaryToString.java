package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ConvertBinaryToString extends ConvertFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin value = toBin(exprs[0], qc);
    final String encoding = toEncodingOrNull(1, CONVERT_ENCODING_X, qc);
    final boolean validate = exprs.length < 3 || !toBoolean(exprs[2], qc);

    try(BufferInput bi = value.input(info)) {
      return Str.get(toString(bi, encoding, validate));
    } catch(final IOException ex) {
      throw CONVERT_STRING_X.get(info, ex);
    }
  }
}
