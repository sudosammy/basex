package org.basex.query.func.ws;

import java.util.*;
import java.util.function.*;

import org.basex.core.jobs.*;
import org.basex.http.ws.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class WsEval extends WsFn {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final IOContent query = toContent(0, qc);
    final HashMap<String, Value> bindings = toBindings(1, qc);
    final WsOptions wo = toOptions(2, new WsOptions(), qc);

    final JobOptions opts = new JobOptions();
    opts.set(JobOptions.BASE_URI, toBaseUri(query.url(), wo, WsOptions.BASE_URI));
    opts.set(JobOptions.ID, wo.get(WsOptions.ID));

    final QueryJobSpec spec = new QueryJobSpec(opts, bindings, query);
    final WebSocket ws = ws(qc);
    final Consumer<QueryJobResult> notify = result -> {
      try {
        WsPool.send(result.value, ws.id);
      } catch(final Exception ex) {
        ws.error(ex);
      }
    };

    final QueryJob job = new QueryJob(spec, qc.context, info, notify, qc);
    return Str.get(job.jc().id());
  }
}
