package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class StringTab extends StandardFunc {
  /** Tab character. */
  private static final Str TAB = Str.get("\t");

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    return TAB;
  }
}
