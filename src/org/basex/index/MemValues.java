package org.basex.index;

import java.util.Arrays;
import org.basex.core.Main;
import org.basex.util.TokenSet;
import org.basex.util.Token;

/**
 * This class provides a main-memory access to attribute values and
 * text contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class MemValues extends Index {
  /** Index instance. */
  private final MemValueIndex index = new MemValueIndex();

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param id id value
   * @return index position
   */
  public int index(final byte[] key, final int id) {
    return index.index(key, id);
  }

  /**
   * Returns the token for the specified id.
   * @param id id
   * @return token
   */
  public byte[] token(final int id) {
    return index.key(id);
  }

  @Override
  public IndexIterator ids(final IndexToken tok) {
    return index.ids(tok.get());
  }

  @Override
  public int nrIDs(final IndexToken it) {
    return ids(it).size();
  }

  @Override
  public byte[] info() {
    return Token.token(Main.name(this));
  }

  @Override
  public void close() { }

  /** MemValue Index. */
  static final class MemValueIndex extends TokenSet {
    /** IDs. */
    int[][] ids = new int[CAP][];
    /** ID array lengths. */
    int[] len = new int[CAP];

    /**
     * Indexes the specified keys and values.
     * @param key key
     * @param id id value
     * @return index position
     */
    int index(final byte[] key, final int id) {
      int i = add(key);
      if(i > 0) {
        ids[i] = new int[] { id };
      } else {
        i = -i;
        final int l = len[i];
        if(l == ids[i].length) ids[i] = Arrays.copyOf(ids[i], l << 1);
        ids[i][l] = id;
      }
      len[i]++;
      return i;
    }

    /**
     * Returns the ids for the specified key.
     * @param key index key
     * @return ids
     */
    IndexIterator ids(final byte[] key) {
      final int i = id(key);
      if(i == 0) return IndexIterator.EMPTY;

      return new IndexIterator() {
        int p = -1;
        @Override
        public boolean more() { return ++p < len[i]; }
        @Override
        public int next() { return ids[i][p]; }
        @Override
        public double score() { return -1; }
      };
    }

    @Override
    public void rehash() {
      super.rehash();
      final int s = size << 1;
      ids = Arrays.copyOf(ids, s);
      len = Arrays.copyOf(len, s);
    }
  }
}
