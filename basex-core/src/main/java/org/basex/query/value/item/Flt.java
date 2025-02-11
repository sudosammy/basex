package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Float item ({@code xs:float}).
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Flt extends ANum {
  /** Value "NaN". */
  public static final Flt NAN = new Flt(Float.NaN);
  /** Value "0". */
  public static final Flt ZERO = new Flt(0);
  /** Value "1". */
  public static final Flt ONE = new Flt(1);
  /** Data. */
  private final float value;

  /**
   * Constructor.
   * @param value value
   */
  private Flt(final float value) {
    super(AtomType.FLOAT);
    this.value = value;
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Flt get(final float value) {
    return value == 0 && Float.floatToRawIntBits(value) == 0 ? ZERO : value == 1 ? ONE :
      Float.isNaN(value) ? NAN : new Flt(value);
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @param ii input info
   * @return instance
   * @throws QueryException query exception
   */
  public static Flt get(final byte[] value, final InputInfo ii) throws QueryException {
    return get(parse(value, ii));
  }

  @Override
  public byte[] string() {
    return Token.token(value);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !Float.isNaN(value) && value != 0;
  }

  @Override
  public long itr() {
    return (long) value;
  }

  @Override
  public float flt() {
    return value;
  }

  @Override
  public double dbl() {
    return value;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) throws QueryException {
    if(Float.isNaN(value) || Float.isInfinite(value))
      throw valueError(AtomType.DECIMAL, string(), ii);
    return new BigDecimal(value);
  }

  @Override
  public Flt abs() {
    return value > 0.0d || 1 / value > 0 ? this : get(-value);
  }

  @Override
  public Flt ceiling() {
    final float v = (float) Math.ceil(value);
    return v == value ? this : get(v);
  }

  @Override
  public Flt floor() {
    final float f = (float) Math.floor(value);
    return f == value ? this : get(f);
  }

  @Override
  public Flt round(final int scale, final boolean even) {
    final float f = Dbl.get(value).round(scale, even).flt();
    return value == f ? this : get(f);
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return item.type == AtomType.DECIMAL ? item.eq(this, coll, sc, ii) : value == item.dbl(ii);
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    if(item.type == AtomType.DECIMAL) return -item.diff(this, coll, ii);
    // cannot be replaced by Double.compare (different semantics)
    final double d = item.dbl(ii);
    return Double.isNaN(value) || Double.isNaN(d) ? UNDEF : value < d ? -1 : value > d ? 1 : 0;
  }

  @Override
  public Float toJava() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Flt && value == ((Flt) obj).value;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the given token to a float value.
   * @param value value to be converted
   * @param ii input info
   * @return float value
   * @throws QueryException query exception
   */
  public static float parse(final byte[] value, final InputInfo ii) throws QueryException {
    final byte[] v = Token.trim(value);
    if(!Token.eq(v, Token.INFINITY, Token.NEGATIVE_INFINITY)) {
      try {
        return Float.parseFloat(Token.string(v));
      } catch(final NumberFormatException ex) {
        Util.debug(ex);
      }
    }

    if(Token.eq(v, Token.INF)) return Float.POSITIVE_INFINITY;
    if(Token.eq(v, Token.NEGATVE_INF)) return Float.NEGATIVE_INFINITY;
    throw AtomType.FLOAT.castError(value, ii);
  }
}
