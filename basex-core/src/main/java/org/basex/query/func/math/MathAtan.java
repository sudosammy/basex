package org.basex.query.func.math;

import static java.lang.StrictMath.*;

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
public final class MathAtan extends MathFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = exprs[0].atomItem(qc, info);
    return value == Empty.VALUE ? Empty.VALUE : Dbl.get(atan(toDouble(value)));
  }
}
