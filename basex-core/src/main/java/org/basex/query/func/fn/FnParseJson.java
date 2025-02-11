package org.basex.query.func.fn;

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
public class FnParseJson extends FnJsonDoc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item json = exprs[0].atomItem(qc, info);
    return json == Empty.VALUE ? Empty.VALUE : parse(toToken(json), false, qc);
  }
}
