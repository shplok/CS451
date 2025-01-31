package iota;

/**
 * The AST node for an int literal.
 */
class ILiteralInt extends IExpression {
    // String representation of the literal.
    private final String text;

    /**
     * Constructs an AST node for an int literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public ILiteralInt(int line, String text) {
        super(line);
        this.text = text;
    }

    /**
     * Returns the literal as an int.
     *
     * @return the literal as an int.
     */
    public int toInt() {
        return Integer.parseInt(text);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        int i = toInt();
        output.addLDCInstruction(i);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("ILiteralInt:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", text);
    }
}
