package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * The AST node for a while-statement.
 */
class JWhileStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // Body.
    private JStatement body;

    protected boolean hasBreak;

    protected String breakLabel;

    protected boolean hasContinue;

    protected String continueLabel;



    /**
     * Constructs an AST node for a while-statement.
     *
     * @param line      line in which the while-statement occurs in the source file.
     * @param condition test expression.
     * @param body      the body.
     */
    public JWhileStatement(int line, JExpression condition, JStatement body) {
        super(line);
        this.condition = condition;
        this.body = body;
        this.hasBreak = false;
        this.breakLabel = null;
    }

    /**
     * {@inheritDoc}
     */
    public JWhileStatement analyze(Context context) {

        JMember.enclosingStatement.push(this);

        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);

        JMember.enclosingStatement.pop();

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String testLabel = output.createLabel();
    
        // Set continueLabel to an appropriate label if hasContinue is true
        if (hasContinue) {
            continueLabel = testLabel; // In while loops, continue goes back to the test
        } else {
            continueLabel = testLabel; // Create it anyway, same as test label
        }
        
        // Set breakLabel to an appropriate label if hasBreak is true
        if (hasBreak) {
            breakLabel = output.createLabel();
        } else {
            breakLabel = output.createLabel(); // Still need a label for loop exit
        }
        
        // Mark where we test the condition
        output.addLabel(testLabel);
        condition.codegen(output, breakLabel, false);
        
        // Generate code for the body
        body.codegen(output);
        
        // Jump back to test condition
        output.addBranchInstruction(GOTO, testLabel);
        
        // Add the break label
        output.addLabel(breakLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JWhileStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Body", e2);
        body.toJSON(e2);
    }
}
