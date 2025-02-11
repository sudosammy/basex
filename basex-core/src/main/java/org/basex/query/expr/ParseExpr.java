package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.NodeType.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract parse expression. All non-value expressions are derived from this class.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Expression type. */
  public final ExprType exprType;
  /** Input information. */
  protected InputInfo info;

  /**
   * Constructor.
   * @param info input info (can be {@code null}
   * @param seqType sequence type
   */
  protected ParseExpr(final InputInfo info, final SeqType seqType) {
    this.info = info;
    exprType = new ExprType(seqType);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return item(qc, info);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value(qc).item(qc, info);
  }

  @Override
  public final Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value(qc).atomValue(qc, info);
  }

  @Override
  public final Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(seqType().zeroOrOne()) {
      final Item item = item(qc, info);
      return item == Empty.VALUE ? Bln.FALSE : item;
    }

    final Iter iter = iter(qc);
    final Item item = iter.next();
    if(item == null) return Bln.FALSE;

    // effective boolean value is only defined for node sequences or single items
    if(!(item instanceof ANode)) {
      final Item next = iter.next();
      if(next != null) throw ebvError(ValueBuilder.concat(item, next, qc), info);
    }
    return item;
  }

  @Override
  public final Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = ebv(qc, info);
    return (item instanceof ANum ? item.dbl(info) == qc.focus.pos : item.bool(info)) ? item : null;
  }

  @Override
  public final SeqType seqType() {
    return exprType.seqType();
  }

  @Override
  public final long size() {
    return exprType.size();
  }

  @Override
  public Data data() {
    return exprType.data();
  }

  @Override
  public final void refineType(final Expr expr) {
    exprType.refine(expr);
  }

  @Override
  public InputInfo info() {
    return info;
  }

  // OPTIMIZATIONS ================================================================================

  /**
   * Assigns this expression's type to the specified expression.
   * @param <T> expression type
   * @param expr expression to be modified
   * @return specified expression
   */
  public final <T extends Expr> T copyType(final T expr) {
    if(expr instanceof ParseExpr) ((ParseExpr) expr).exprType.assign(this);
    return expr;
  }

  /**
   * Assigns the type from the specified expression.
   * @param expr expression
   * @return self reference
   */
  public final ParseExpr adoptType(final Expr expr) {
    exprType.assign(expr);
    return this;
  }

  // VALIDITY CHECKS ==============================================================================

  /**
   * Ensures that the specified function expression is (not) updating.
   * Otherwise, throws an exception.
   * @param <T> expression type
   * @param expr expression (may be {@code null})
   * @param updating indicates if expression is expected to be updating
   * @param sc static context
   * @return specified expression
   * @throws QueryException query exception
   */
  protected final <T extends XQFunctionExpr> T checkUp(final T expr, final boolean updating,
      final StaticContext sc) throws QueryException {

    if(!(sc.mixUpdates || updating == expr.annotations().contains(Annotation.UPDATING))) {
      if(!updating) throw FUNCUP_X.get(info, expr);
      if(!expr.vacuousBody()) throw FUNCNOTUP_X.get(info, expr);
    }
    return expr;
  }

  /**
   * Ensures that the specified expression performs no updates.
   * Otherwise, throws an exception.
   * @param expr expression (may be {@code null})
   * @throws QueryException query exception
   */
  protected final void checkNoUp(final Expr expr) throws QueryException {
    if(expr == null) return;
    expr.checkUp();
    if(expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
  }

  /**
   * Ensures that none of the specified expressions performs an update.
   * Otherwise, throws an exception.
   * @param exprs expressions (may be {@code null})
   * @throws QueryException query exception
   */
  protected final void checkNoneUp(final Expr... exprs) throws QueryException {
    if(exprs == null) return;
    checkAllUp(exprs);
    for(final Expr expr : exprs) {
      if(expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
    }
  }

  /**
   * Ensures that all specified expressions are vacuous or either updating or non-updating.
   * Otherwise, throws an exception.
   * @param exprs expressions to be checked
   * @throws QueryException query exception
   */
  protected final void checkAllUp(final Expr... exprs) throws QueryException {
    // updating state: 0 = initial state, 1 = updating, -1 = non-updating
    int state = 0;
    for(final Expr expr : exprs) {
      expr.checkUp();
      if(expr.vacuous()) continue;
      final boolean updating = expr.has(Flag.UPD);
      if(updating ? state == -1 : state == 1) throw UPALL.get(info);
      state = updating ? 1 : -1;
    }
  }

  /**
   * Returns the current context value or throws an exception if the context value is not set.
   * @param qc query context
   * @return context value
   * @throws QueryException query exception
   */
  protected final Value ctxValue(final QueryContext qc) throws QueryException {
    final Value value = qc.focus.value;
    if(value != null) return value;
    throw NOCTX_X.get(info, this);
  }

  // CONVERSIONS ==================================================================================

  /**
   * Evaluates an expression to a token.
   * @param expr expression
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    if(item == Empty.VALUE) throw EMPTYFOUND_X.get(info, STRING);
    return toToken(item);
  }

  /**
   * Evaluates an expression to a token.
   * @param expr expression
   * @param qc query context
   * @return token (zero-length if result is an empty sequence)
   * @throws QueryException query exception
   */
  protected final byte[] toZeroToken(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item == Empty.VALUE ? Token.EMPTY : toToken(item);
  }

  /**
   * Evaluates an expression to a token.
   * @param expr expression
   * @param qc query context
   * @return token, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final byte[] toTokenOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item != Empty.VALUE ? toToken(item) : null;
  }

  /**
   * Converts an item to a token.
   * @param item item to be converted
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Item item) throws QueryException {
    final Type type = item.type;
    if(type.isStringOrUntyped()) return item.string(info);
    throw item instanceof FItem ? FIATOMIZE_X.get(info, item) : typeError(item, STRING, info);
  }

  /**
   * Evaluates an expression to a string.
   * @param expr expression
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  protected final String toString(final Expr expr, final QueryContext qc) throws QueryException {
    return Token.string(toToken(expr, qc));
  }

  /**
   * Evaluates an expression to a string.
   * @param item item to be converted
   * @return string
   * @throws QueryException query exception
   */
  protected final String toString(final Item item) throws QueryException {
    return Token.string(toToken(item));
  }

  /**
   * Evaluates an expression to a string.
   * @param expr expression
   * @param qc query context
   * @return string, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final String toStringOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item != Empty.VALUE ? toString(item) : null;
  }

  /**
   * Evaluates an expression to a boolean.
   * @param expr expression
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Expr expr, final QueryContext qc) throws QueryException {
    return toBoolean(expr.atomItem(qc, info));
  }

  /**
   * Converts an item to a boolean.
   * @param item item to be converted
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Item item) throws QueryException {
    final Type type = checkNoEmpty(item, BOOLEAN).type;
    if(type == BOOLEAN) return item.bool(info);
    if(type.isUntyped()) return Bln.parse(item, info);
    throw typeError(item, BOOLEAN, info);
  }

  /**
   * Evaluates an expression to a double number.
   * @param expr expression
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Expr expr, final QueryContext qc) throws QueryException {
    return toDouble(expr.atomItem(qc, info));
  }

  /**
   * Converts an item to a double number.
   * @param item item
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Item item) throws QueryException {
    if(checkNoEmpty(item, DOUBLE).type.isNumberOrUntyped()) return item.dbl(info);
    throw numberError(this, item);
  }

  /**
   * Evaluates an expression to a number.
   * @param expr expression
   * @param qc query context
   * @return number, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final ANum toNumberOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item != Empty.VALUE ? toNumber(item) : null;
  }

  /**
   * Converts an item to a number.
   * @param item item to be converted
   * @return number
   * @throws QueryException query exception
   */
  protected final ANum toNumber(final Item item) throws QueryException {
    if(item.type.isUntyped()) return Dbl.get(item.dbl(info));
    if(item instanceof ANum) return (ANum) item;
    throw numberError(this, item);
  }

  /**
   * Evaluates an expression to a float number.
   * @param expr expression
   * @param qc query context
   * @return float
   * @throws QueryException query exception
   */
  protected final float toFloat(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    if(checkNoEmpty(item, FLOAT).type.isNumberOrUntyped()) return item.flt(info);
    throw numberError(this, item);
  }

  /**
   * Evaluates an expression to a long number.
   * @param expr expression
   * @param qc query context
   * @return integer value
   * @throws QueryException query exception
   */
  protected final long toLong(final Expr expr, final QueryContext qc) throws QueryException {
    return toLong(expr.atomItem(qc, info));
  }

  /**
   * Converts an item to an integer number.
   * @param item item to be converted
   * @return number
   * @throws QueryException query exception
   */
  protected final long toLong(final Item item) throws QueryException {
    final Type type = checkNoEmpty(item, INTEGER).type;
    if(type.instanceOf(INTEGER) || type.isUntyped()) return item.itr(info);
    throw typeError(item, INTEGER, info);
  }

  /**
   * Converts an item to a node.
   * @param expr expression
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  protected final ANode toNode(final Expr expr, final QueryContext qc) throws QueryException {
    return toNode(checkNoEmpty(expr.item(qc, info), NODE));
  }

  /**
   * Evaluates an expression to a node.
   * @param expr expression
   * @param qc query context
   * @return node, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final ANode toNodeOrNull(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.item(qc, info);
    return item == Empty.VALUE ? null : toNode(item);
  }

  /**
   * Converts an item to a node.
   * @param item item to be converted
   * @return node
   * @throws QueryException query exception
   */
  protected final ANode toNode(final Item item) throws QueryException {
    if(item instanceof ANode) return (ANode) item;
    throw typeError(item, NODE, info);
  }

  /**
   * Evaluates an expression to an item.
   * @param expr expression
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item toItem(final Expr expr, final QueryContext qc) throws QueryException {
    return checkNoEmpty(expr.item(qc, info));
  }

  /**
   * Evaluates an expression to an item of the given type.
   * @param expr expression
   * @param qc query context
   * @param type expected type
   * @return item
   * @throws QueryException query exception
   */
  protected final Item toItem(final Expr expr, final QueryContext qc, final Type type)
      throws QueryException {
    return checkNoEmpty(expr.item(qc, info), type);
  }

  /**
   * Evaluates an expression to an atomized item of the given type.
   * @param expr expression
   * @param qc query context
   * @return atomized item
   * @throws QueryException query exception
   */
  protected final Item toAtomItem(final Expr expr, final QueryContext qc) throws QueryException {
    return checkNoEmpty(expr.atomItem(qc, info));
  }

  /**
   * Evaluates an expression to an element.
   * @param expr expression
   * @param qc query context
   * @return element
   * @throws QueryException query exception
   */
  protected final ANode toElem(final Expr expr, final QueryContext qc) throws QueryException {
    return (ANode) checkType(expr.item(qc, info), ELEMENT);
  }

  /**
   * Evaluates an expression to a binary item.
   * @param expr expression
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Expr expr, final QueryContext qc) throws QueryException {
    return toBin(expr.atomItem(qc, info));
  }

  /**
   * Converts an item to a binary item.
   * @param item item to be converted
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Item item) throws QueryException {
    if(checkNoEmpty(item) instanceof Bin) return (Bin) item;
    throw BINARY_X.get(info, item.type);
  }

  /**
   * Evaluates an expression (token, binary item) to a byte array.
   * @param expr expression
   * @param qc query context
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBytes(final Expr expr, final QueryContext qc) throws QueryException {
    return toBytes(expr.atomItem(qc, info));
  }

  /**
   * Evaluates an expression to a Base64 item.
   * @param empty allow empty result
   * @param expr expression
   * @param qc query context
   * @return Base64 item
   * @throws QueryException query exception
   */
  protected final B64 toB64(final Expr expr, final QueryContext qc, final boolean empty)
      throws QueryException {
    return toB64(expr.atomItem(qc, info), empty);
  }

  /**
   * Converts an item to a base64 item.
   * @param empty return {@code null} if the given item is empty
   * @param item item
   * @return Base64 item or {@code null}
   * @throws QueryException query exception
   */
  protected final B64 toB64(final Item item, final boolean empty) throws QueryException {
    if(empty && item == Empty.VALUE) return null;
    return (B64) checkType(item, BASE64_BINARY);
  }

  /**
   * Converts an item (token, binary item) to a byte array.
   * @param item item to be converted
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBytes(final Item item) throws QueryException {
    if(checkNoEmpty(item).type.isStringOrUntyped()) return item.string(info);
    if(item instanceof Bin) return ((Bin) item).binary(info);
    throw STRBIN_X_X.get(info, item.type, item);
  }

  /**
   * Evaluates an expression to a QName.
   * @param expr expression
   * @param empty return {@code null} if the given item is empty
   * @param qc query context
   * @return QName or {@code null}
   * @throws QueryException query exception
   */
  protected final QNm toQNm(final Expr expr, final boolean empty, final QueryContext qc)
      throws QueryException {
    return toQNm(expr.atomItem(qc, info), empty);
  }

  /**
   * Converts an item to a QName.
   * @param item item
   * @param empty return {@code null} if the given item is empty
   * @return QName or {@code null}
   * @throws QueryException query exception
   */
  protected final QNm toQNm(final Item item, final boolean empty) throws QueryException {
    if(empty && item == Empty.VALUE) return null;
    final Type type = checkNoEmpty(item, QNAME).type;
    if(type == QNAME) return (QNm) item;
    if(type.isUntyped()) throw NSSENS_X_X.get(info, type, QNAME);
    throw typeError(item, QNAME, info);
  }

  /**
   * Evaluates an expression to a function item.
   * @param expr expression
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem toFunction(final Expr expr, final QueryContext qc) throws QueryException {
    return (FItem) checkType(toItem(expr, qc, SeqType.FUNCTION), SeqType.FUNCTION);
  }

  /**
   * Evaluates an expression to a map.
   * @param expr expression
   * @param qc query context
   * @return map
   * @throws QueryException query exception
   */
  protected final XQMap toMap(final Expr expr, final QueryContext qc) throws QueryException {
    return toMap(toItem(expr, qc, SeqType.MAP));
  }

  /**
   * Converts an item to a map.
   * @param item item to check
   * @return map
   * @throws QueryException query exception
   */
  protected final XQMap toMap(final Item item) throws QueryException {
    if(item instanceof XQMap) return (XQMap) item;
    throw typeError(item, SeqType.MAP, info);
  }

  /**
   * Evaluates an expression to an array.
   * @param expr expression
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  protected final XQArray toArray(final Expr expr, final QueryContext qc) throws QueryException {
    return toArray(toItem(expr, qc, SeqType.ARRAY));
  }

  /**
   * Converts an item to an array.
   * @param item item to check
   * @return the array
   * @throws QueryException if the item is not an array
   */
  protected final XQArray toArray(final Item item) throws QueryException {
    if(item instanceof XQArray) return (XQArray) item;
    throw typeError(item, SeqType.ARRAY, info);
  }

  /**
   * Evaluates an expression and returns an item if it has the specified type.
   * @param expr expression
   * @param type expected type
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item checkType(final Expr expr, final AtomType type, final QueryContext qc)
      throws QueryException {
    return checkType(expr.atomItem(qc, info), type);
  }

  /**
   * Returns an item if it has the specified type.
   * @param item item
   * @param type expected type
   * @return item
   * @throws QueryException query exception
   */
  protected final Item checkType(final Item item, final Type type) throws QueryException {
    if(checkNoEmpty(item, type).type.instanceOf(type)) return item;
    throw typeError(item, type, info);
  }

  /**
   * Returns an item if it is not empty.
   * @param item item
   * @return specified item
   * @throws QueryException query exception
   */
  protected final Item checkNoEmpty(final Item item) throws QueryException {
    if(item != Empty.VALUE) return item;
    throw EMPTYFOUND.get(info);
  }

  /**
   * Returns an item if it is not empty.
   * @param item item
   * @param type expected type
   * @return specified item
   * @throws QueryException query exception
   */
  protected final Item checkNoEmpty(final Item item, final Type type) throws QueryException {
    if(item != Empty.VALUE) return item;
    throw EMPTYFOUND_X.get(info, type);
  }
}
