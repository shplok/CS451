package iota;

import static iota.CLConstants.GOTO;

/**
 * The AST node for an if-statement.
 */
class IIfStatement extends IStatement {
    // Test expression.
    private IExpression condition;

    // Then part.
    private IStatement thenPart;

    // Else part.
    private IStatement elsePart;

    /**
     * Constructs an AST node for an if-statement.
     *
     * @param line      line in which the if-statement occurs in the source file.
     * @param condition test expression.
     * @param thenPart  then part.
     * @param elsePart  else part.
     */
    public IIfStatement(int line, IExpression condition, IStatement thenPart, IStatement elsePart) {
        super(line);
        this.condition = condition;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /**
     * {@inheritDoc}
     */
    public IStatement analyze(Context context) {
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        thenPart = (IStatement) thenPart.analyze(context);
        if (elsePart != null) {
            elsePart = (IStatement) elsePart.analyze(context);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String elseLabel = output.createLabel();
        String endLabel = output.createLabel();
        condition.codegen(output, elseLabel, false);
        thenPart.codegen(output);
        if (elsePart != null) {
            output.addBranchInstruction(GOTO, endLabel);
        }
        output.addLabel(elseLabel);
        if (elsePart != null) {
            elsePart.codegen(output);
            output.addLabel(endLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IIfStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("ThenPart", e2);
        thenPart.toJSON(e2);
        if (elsePart != null) {
            JSONElement e3 = new JSONElement();
            e.addChild("ElsePart", e3);
            elsePart.toJSON(e3);
        }
    }
}
