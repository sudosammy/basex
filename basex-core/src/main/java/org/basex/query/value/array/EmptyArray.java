package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * The empty array.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
final class EmptyArray extends XQArray {
  /** The empty array. */
  static final EmptyArray EMPTY = new EmptyArray();

  /** Hidden constructor. */
  private EmptyArray() {
    super(SeqType.ARRAY);
  }

  @Override
  public void refineType(final Expr expr) {
  }

  @Override
  public XQArray cons(final Value head) {
    return new SmallArray(new Value[] { head }, ArrayType.get(head.seqType()));
  }

  @Override
  public XQArray snoc(final Value last) {
    return new SmallArray(new Value[] { last }, ArrayType.get(last.seqType()));
  }

  @Override
  public Value get(final long index) {
    throw Util.notExpected();
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    throw Util.notExpected();
  }

  @Override
  public long arraySize() {
    return 0;
  }

  @Override
  public XQArray concat(final XQArray seq) {
    return seq;
  }

  @Override
  public Value head() {
    throw Util.notExpected();
  }

  @Override
  public Value last() {
    throw Util.notExpected();
  }

  @Override
  public XQArray init() {
    throw Util.notExpected();
  }

  @Override
  public XQArray tail() {
    throw Util.notExpected();
  }

  @Override
  public XQArray subArray(final long pos, final long length, final QueryContext qc) {
    return this;
  }

  @Override
  public boolean isEmptyArray() {
    return true;
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    return this;
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    return new SmallArray(new Value[] { value }, ArrayType.get(value.seqType()));
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public ListIterator<Value> iterator(final long size) {
    return Collections.emptyListIterator();
  }

  @Override
  void checkInvariants() {
    // nothing can go wrong
  }

  @Override
  XQArray prepend(final SmallArray array) {
    return array;
  }
}
