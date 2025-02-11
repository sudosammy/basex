package org.basex.query.value.item;

import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Unsigned long ({@code xs:unsignedLong}).
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Uln extends ANum {
  /** Maximum unsigned long values. */
  public static final BigDecimal MAXULN = BigDecimal.valueOf(Long.MAX_VALUE).
      multiply(Dec.BD_2).add(BigDecimal.ONE);
  /** Decimal value. */
  private final BigInteger value;

  /**
   * Constructor.
   * @param value decimal value
   */
  public Uln(final BigInteger value) {
    super(AtomType.UNSIGNED_LONG);
    this.value = value;
  }

  @Override
  public byte[] string() {
    return token(value.toString());
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value.signum() != 0;
  }

  @Override
  public long itr() {
    return value.longValue();
  }

  @Override
  public float flt() {
    return value.floatValue();
  }

  @Override
  public double dbl() {
    return value.doubleValue();
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return new BigDecimal(value);
  }

  @Override
  public ANum abs() {
    final long l = itr();
    return l >= 0 ? this : Int.get(-l);
  }

  @Override
  public Uln ceiling() {
    return this;
  }

  @Override
  public Uln floor() {
    return this;
  }

  @Override
  public ANum round(final int scale, final boolean even) {
    return scale >= 0 ? this :
      Int.get(Dec.get(new BigDecimal(value)).round(scale, even).dec(null).longValue());
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final Type tp = item.type;
    return tp == AtomType.UNSIGNED_LONG ? value.equals(((Uln) item).value) :
           tp == AtomType.DOUBLE || tp == AtomType.FLOAT ? item.eq(this, coll, sc, ii) :
           value.compareTo(BigInteger.valueOf(item.itr(ii))) == 0;
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    final Type tp = item.type;
    return tp == AtomType.UNSIGNED_LONG ? value.compareTo(((Uln) item).value) :
           tp == AtomType.DOUBLE || tp == AtomType.FLOAT ? -item.diff(this, coll, ii) :
           value.compareTo(BigInteger.valueOf(item.itr(ii)));
  }

  @Override
  public Object toJava() {
    return new BigInteger(value.toString());
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Uln && value.compareTo(((Uln) obj).value) == 0;
  }
}
