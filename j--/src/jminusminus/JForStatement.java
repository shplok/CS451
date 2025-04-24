package jminusminus;

import static jminusminus.CLConstants.GOTO;

import java.util.ArrayList;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;

    // Update.
    private ArrayList<JStatement> update;

    // The body.
    private JStatement body;

    private Context LocalContext;

    protected boolean hasBreak;
  
    protected String breakLabel;

    protected boolean hasContinue;

    protected String continueLabel;
    /**
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition, ArrayList<JStatement> update,
                         JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
        this.hasBreak = false;
        this.breakLabel = null;
        this.hasContinue = false;
        this.continueLabel = null;
        
    }

    /**
     * {@inheritDoc}
     */
    public JForStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);
    
        this.LocalContext = new LocalContext(context);
        
        // Handle initialization statements if they exist
        if (init != null) {
            ArrayList<JStatement> initStatements = new ArrayList<>();
            for (JStatement initStmt : init) {
                initStmt = (JStatement) initStmt.analyze(LocalContext);
                initStatements.add(initStmt);
            }
            this.init = initStatements;
        }

        // Handle condition if it exists
        if (condition != null) {
            condition = condition.analyze(LocalContext);
        }

        // Handle update statements if they exist
        if (update != null) {
            ArrayList<JStatement> updateStatements = new ArrayList<>();
            for (JStatement updateStmt : update) {
                updateStmt = (JStatement) updateStmt.analyze(LocalContext);
                updateStatements.add(updateStmt);
            }
            this.update = updateStatements;
        }

        body = (JStatement) body.analyze(LocalContext);
        
        // Pop this from JMember.enclosingStatement upon exit
        JMember.enclosingStatement.pop();
        
        return this;

    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
 
        String conditionLabel = output.createLabel();
    
        // Set continueLabel to an appropriate label if hasContinue is true
        if (hasContinue) {
            continueLabel = output.createLabel();
        } else {
            continueLabel = output.createLabel(); // Create it anyway for consistency
        }
        
        // Set breakLabel to an appropriate label if hasBreak is true
        if (hasBreak) {
            breakLabel = output.createLabel();
        } else {
            breakLabel = output.createLabel(); // Still need a label for loop exit
        }

        // Generate code for initialization if they exist
        if (init != null) {
            for (JStatement initStmt : init) {
                initStmt.codegen(output);
            }
        }
        
        // Mark the condition check point
        output.addLabel(conditionLabel);
        
        // Generate condition code if it exists, otherwise it's an infinite loop
        if (condition != null) {
            condition.codegen(output, breakLabel, false);
        }

        // Generate code for the body
        body.codegen(output);
        
        // Add the continue label at the appropriate place
        output.addLabel(continueLabel);
        
        // Generate code for update statements if they exist
        if (update != null) {
            for (JStatement updateStmt : update) {
                updateStmt.codegen(output);
            }
        }

        // Jump back to condition check
        output.addBranchInstruction(GOTO, conditionLabel);
        
        // Add the break label
        output.addLabel(breakLabel);

    }
    

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
