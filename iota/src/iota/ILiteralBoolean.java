package iota;

import static iota.CLConstants.GOTO;
import static iota.CLConstants.ICONST_0;
import static iota.CLConstants.ICONST_1;

/**
 * The AST node for a boolean literal.
 */
class ILiteralBoolean extends IExpression {
    // String representation of the literal.
    private final String text;

    /**
     * Constructs an AST node for a boolean literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public ILiteralBoolean(int line, String text) {
        super(line);
        this.text = text;
    }

    /**
     * Returns the literal as a boolean.
     *
     * @return the literal as a boolean.
     */
    public boolean toBoolean() {
        return text.equals("true");
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        output.addNoArgInstruction(toBoolean() ? ICONST_1 : ICONST_0);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        boolean b = toBoolean();
        if (b && onTrue || !b && !onTrue) {
            output.addBranchInstruction(GOTO, targetLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("ILiteralBoolean:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", text);
    }
}
