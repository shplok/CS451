package iota;

/**
 * The AST node for an expression that appears as a statement. Only the expressions that have a side effect are valid
 * statement expressions.
 */
class IStatementExpression extends IStatement {
    /**
     * The expression.
     */
    protected IExpression expr;

    /**
     * Constructs an AST node for a statement expression.
     *
     * @param line line in which the expression occurs in the source file.
     * @param expr the expression.
     */
    public IStatementExpression(int line, IExpression expr) {
        super(line);
        this.expr = expr;
    }

    /**
     * {@inheritDoc}
     */
    public IStatement analyze(Context context) {
        if (expr.isStatementExpression) {
            expr = expr.analyze(context);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        expr.codegen(output);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IStatementExpression:" + line, e);
        expr.toJSON(e);
    }
}
