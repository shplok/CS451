package jminusminus;


/**
 * The AST node for a do-statement.
 */
class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;

    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    /**
    * Analyzes the do-statement. Checks that the test expression
    * is boolean and analyzes both the test and the body.

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        condition = condition.analyze(context);

        // Ensure boolean type
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Create labels for body and test
        String bodyLabel = output.createLabel();
        String testLabel = output.createLabel();
        
        // Mark the start of the loop body
        output.addLabel(bodyLabel);
        
        // Generate code for the body
        body.codegen(output);
        
        // Mark where we test the condition
        output.addLabel(testLabel);
        
        // Generate code for the condition
        condition.codegen(output);
        
        // If condition is true , goto: start
        output.addBranchInstruction(CLConstants.IFNE, bodyLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
