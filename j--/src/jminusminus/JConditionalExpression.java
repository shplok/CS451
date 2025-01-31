package jminusminus;

import static jminusminus.CLConstants.GOTO;
import static jminusminus.CLConstants.IFEQ;
import static jminusminus.TokenKind.BOOLEAN;

/**
 * The AST node for a conditional expression.
 */
class JConditionalExpression extends JExpression {
    // Test expression.
    private JExpression condition;

    // Then part.
    private JExpression thenPart;

    // Else part.
    private JExpression elsePart;

    /**
     * Constructs an AST node for a conditional expression.
     *
     * @param line      line in which the conditional expression occurs in the source file.
     * @param condition test expression.
     * @param thenPart  then part.
     * @param elsePart  else part.
     */
    public JConditionalExpression(int line, JExpression condition, JExpression thenPart, JExpression elsePart) {
        super(line);
        this.condition = condition;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        condition = condition.analyze(context);
        thenPart = thenPart.analyze(context);
        elsePart = elsePart.analyze(context);
        
        // Using TokenKind.BOOLEAN here
        if (!condition.type().equals(Type.BOOLEAN)) {
            JAST.compilationUnit.reportSemanticError(line(), 
                "Condition must be of type " + BOOLEAN.image());
            type = Type.ANY;
        }

        if (!thenPart.type().equals(elsePart.type())) {
            JAST.compilationUnit.reportSemanticError(line(), "Types are Incompatable");
            type = Type.ANY;
        } else {
            type = thenPart.type();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String elseLabel = output.createLabel();
        String endLabel = output.createLabel();

        condition.codegen(output);
        output.addBranchInstruction(IFEQ, elseLabel);

        thenPart.codegen(output);
        output.addBranchInstruction(GOTO, endLabel);

        output.addLabel(elseLabel);
        elsePart.codegen(output);

        output.addLabel(endLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JConditionalExpression:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("ThenPart", e2);
        thenPart.toJSON(e2);
        JSONElement e3 = new JSONElement();
        e.addChild("ElsePart", e3);
        elsePart.toJSON(e3);
    }
}