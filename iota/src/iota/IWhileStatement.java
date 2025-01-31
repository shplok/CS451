package iota;

import static iota.CLConstants.GOTO;

/**
 * The AST node for a while-statement.
 */
class IWhileStatement extends IStatement {
    // Test expression.
    private IExpression condition;

    // Body.
    private IStatement body;

    /**
     * Constructs an AST node for a while-statement.
     *
     * @param line      line in which the while-statement occurs in the source file.
     * @param condition test expression.
     * @param body      the body.
     */
    public IWhileStatement(int line, IExpression condition, IStatement body) {
        super(line);
        this.condition = condition;
        this.body = body;
    }

    /**
     * {@inheritDoc}
     */
    public IWhileStatement analyze(Context context) {
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (IStatement) body.analyze(context);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String testLabel = output.createLabel();
        String endLabel = output.createLabel();
        output.addLabel(testLabel);
        condition.codegen(output, endLabel, false);
        body.codegen(output);
        output.addBranchInstruction(GOTO, testLabel);
        output.addLabel(endLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IWhileStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Body", e2);
        body.toJSON(e2);
    }
}
