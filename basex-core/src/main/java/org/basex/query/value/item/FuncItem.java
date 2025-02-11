package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Function item.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public final class FuncItem extends FItem implements Scope {
  /** Static context. */
  public final StaticContext sc;
  /** Function expression. */
  private final Expr expr;
  /** Function name (may be {@code null}). */
  private final QNm name;
  /** Formal parameters. */
  private final Var[] params;
  /** Size of the stack frame needed for this function. */
  private final int stackSize;
  /** Input information. */
  private final InputInfo info;

  /** Query focus. */
  private QueryFocus focus;
  /** Indicates if the query focus is accessed or modified. */
  private Boolean fcs;

  /**
   * Constructor.
   * @param sc static context
   * @param anns function annotations
   * @param name function name (can be {@code null})
   * @param params formal parameters
   * @param type function type
   * @param expr function body
   * @param stackSize stack-frame size
   * @param info input info
   */
  public FuncItem(final StaticContext sc, final AnnList anns, final QNm name, final Var[] params,
      final FuncType type, final Expr expr, final int stackSize, final InputInfo info) {
    this(sc, anns, name, params, type, expr, stackSize, info, null);
  }

  /**
   * Constructor.
   * @param sc static context
   * @param anns function annotations
   * @param name function name (can be {@code null})
   * @param params formal parameters
   * @param type function type
   * @param expr function body
   * @param stackSize stack-frame size
   * @param info input info
   * @param focus query focus (can be {@code null})
   */
  public FuncItem(final StaticContext sc, final AnnList anns, final QNm name, final Var[] params,
      final FuncType type, final Expr expr, final int stackSize, final InputInfo info,
      final QueryFocus focus) {

    super(type, anns);
    this.name = name;
    this.params = params;
    this.expr = expr;
    this.stackSize = stackSize;
    this.sc = sc;
    this.info = info;
    this.focus = focus;
  }

  @Override
  public int arity() {
    return params.length;
  }

  @Override
  public QNm funcName() {
    return name;
  }

  @Override
  public QNm paramName(final int ps) {
    return params[ps].name;
  }

  @Override
  public Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {

    final int pl = params.length;
    for(int p = 0; p < pl; p++) qc.set(params[p], args[p]);

    // use shortcut if focus is not accessed
    if(fcs == null) fcs = expr.has(Flag.FCS, Flag.CTX, Flag.POS);
    if(!fcs) return expr.value(qc);

    final QueryFocus qf = qc.focus;
    qc.focus = focus != null ? focus : new QueryFocus();
    try {
      return expr.value(qc);
    } finally {
      qc.focus = qf;
    }
  }

  @Override
  public int stackFrameSize() {
    return stackSize;
  }

  @Override
  public FuncItem coerceTo(final FuncType ft, final QueryContext qc, final InputInfo ii,
      final boolean optimize) throws QueryException {

    final int pl = params.length, al = ft.argTypes.length;
    if(pl != ft.argTypes.length) throw FUNARITY_X_X.get(info, arguments(pl), al);

    // optimize: continue with coercion if current type is only an instance of new type
    FuncType tp = funcType();
    if(optimize ? tp.eq(ft) : tp.instanceOf(ft)) return this;

    // create new compilation context and variable scope
    final CompileContext cc = new CompileContext(qc, false);
    final VarScope vs = new VarScope(sc);
    final Var[] vars = new Var[pl];
    final Expr[] args = new Expr[pl];
    for(int p = pl; p-- > 0;) {
      vars[p] = vs.addNew(params[p].name, ft.argTypes[p], true, qc, ii);
      args[p] = new VarRef(ii, vars[p]).optimize(cc);
    }
    cc.pushScope(vs);

    // create new function call (will immediately be inlined/simplified when being optimized)
    final boolean updating = anns.contains(Annotation.UPDATING) || expr.has(Flag.UPD);
    Expr body = new DynFuncCall(ii, sc, updating, false, this, args);
    if(optimize) body = body.optimize(cc);

    // add type check if return types differ
    final SeqType dt = ft.declType;
    if(!tp.declType.instanceOf(dt)) {
      body = new TypeCheck(sc, ii, body, dt, true);
      if(optimize) body = body.optimize(cc);
    }

    // adopt type of optimized body if it is more specific than passed on type
    final SeqType bt = body.seqType();
    tp = optimize && !bt.eq(dt) && bt.instanceOf(dt) ? FuncType.get(bt, ft.argTypes) : ft;
    body.markTailCalls(null);
    return new FuncItem(sc, anns, name, vars, tp, body, vs.stackSize(), ii);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.funcItem(this);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var param : params) {
      if(!visitor.declared(param)) return false;
    }
    return expr.accept(visitor);
  }

  @Override
  public boolean compiled() {
    return true;
  }

  @Override
  public Object toJava() {
    return this;
  }

  @Override
  public Expr inline(final Expr[] exprs, final CompileContext cc) throws QueryException {
    if(!StaticFunc.inline(cc, anns, expr) || expr.has(Flag.CTX)) return null;
    cc.info(OPTINLINE_X, this);

    // create let bindings for all variables
    final LinkedList<Clause> clauses = new LinkedList<>();
    final IntObjMap<Var> vm = new IntObjMap<>();
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      clauses.add(new Let(cc.copy(params[p], vm), exprs[p]).optimize(cc));
    }

    // create the return clause
    final Expr rtrn = expr.copy(cc, vm).optimize(cc);
    rtrn.accept(new InlineVisitor());
    return clauses.isEmpty() ? rtrn : new GFLWOR(info, clauses, rtrn).optimize(cc);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(info, this);
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(info, this);
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(ii, this);
  }

  @Override
  public boolean deep(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    throw FICOMPARE_X.get(info, this);
  }

  @Override
  public boolean vacuousBody() {
    final SeqType st = expr.seqType();
    return st != null && st.zero() && !expr.has(Flag.UPD);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj;
  }

  @Override
  public String description() {
    return FUNCTION + ' ' + ITEM;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name == null ? null : name.prefixId()), params, expr);
  }

  @Override
  public String toErrorString() {
    final QueryString qs = new QueryString();
    if(name != null) {
      qs.concat(name.prefixId(), "#", arity());
    } else {
      final StringList list = new StringList(params.length);
      for(final Var param : params) list.add(param.toErrorString());
      qs.token(anns).token(FUNCTION).params(list.finish()).token(AS);
      qs.token(funcType().declType).brace(expr);
    }
    return qs.toString();
  }

  @Override
  public void toString(final QueryString qs) {
    if(name != null) qs.concat("(: ", name.prefixId(), "#", arity(), " :)");
    qs.token(anns).token(FUNCTION).params(params);
    qs.token(AS).token(funcType().declType).brace(expr);
  }

  /**
   * A visitor for checking inlined expressions.
   *
   * @author BaseX Team 2005-22, BSD License
   * @author Leo Woerteler
   */
  private class InlineVisitor extends ASTVisitor {
    @Override
    public boolean inlineFunc(final Scope scope) {
      return scope.visit(this);
    }

    @Override
    public boolean dynFuncCall(final DynFuncCall call) {
      call.markInlined(FuncItem.this);
      return true;
    }
  }
}
