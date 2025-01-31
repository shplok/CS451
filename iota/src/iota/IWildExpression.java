package iota;

/**
 * The AST node for a "wild" expression. A wild expression is a placeholder expression, used when there is a syntax
 * error.
 */
class IWildExpression extends IExpression {
    /**
     * Constructs an AST node for a "wild" expression.
     *
     * @param line line in which the "wild" expression occurs in the source file.
     */

    public IWildExpression(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        type = Type.ANY;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Nothing here.
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IWildExpression:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", "");
    }
}
