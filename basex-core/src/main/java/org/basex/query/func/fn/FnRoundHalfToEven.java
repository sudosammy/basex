package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnRoundHalfToEven extends FnRound {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return round(qc, true);
  }
}
