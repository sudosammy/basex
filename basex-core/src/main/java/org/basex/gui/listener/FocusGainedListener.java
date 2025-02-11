package org.basex.gui.listener;

import java.awt.event.*;

/**
 * Listener interface for gained focus.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public interface FocusGainedListener extends FocusListener {
  @Override
  default void focusLost(final FocusEvent e) { }
}
