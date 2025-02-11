package org.basex.query.func.file;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FilePathToUri extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException {
    return Uri.get(toPath(0, qc).toUri().toString());
  }
}
