package jminusminus;

import static jminusminus.CLConstants.IFNE;

/**
 * The AST node for a do-statement.
 */
class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;


    protected boolean hasBreak;
    

    protected String breakLabel;

    protected boolean hasContinue;

    protected String continueLabel;

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
        this.hasBreak = false;
        this.breakLabel = null;
        this.hasContinue = false;
        this.continueLabel = null;
    }

    /**
    * Analyzes the do-statement. Checks that the test expression
    * is boolean and analyzes both the test and the body.

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {

        JMember.enclosingStatement.push(this);

        condition = condition.analyze(context);

        // Ensure boolean type
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);

        JMember.enclosingStatement.pop();

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String bodyLabel = output.createLabel();
        String testLabel = output.createLabel();
        
        // Set continueLabel to an appropriate label if hasContinue is true
        if (hasContinue) {
            continueLabel = testLabel; // In do-while loops, continue goes to the test
        } else {
            continueLabel = testLabel; // Create it anyway, same as test label
        }
        
        // Set breakLabel to an appropriate label if hasBreak is true
        if (hasBreak) {
            breakLabel = output.createLabel();
        } else {
            breakLabel = output.createLabel(); // Create it anyway for consistency
        }
        
        // Mark the start of the loop body
        output.addLabel(bodyLabel);
        
        // Generate code for the body
        body.codegen(output);
        
        // Mark where we test the condition (this is also where continue jumps to)
        output.addLabel(testLabel);
        
        // Generate code for the condition
        condition.codegen(output);
        
        // If condition is true, goto: start
        output.addBranchInstruction(CLConstants.IFNE, bodyLabel);
        
        // Add the break label at the appropriate place
        if (hasBreak && breakLabel != null) {
            output.addLabel(breakLabel);
        }
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
