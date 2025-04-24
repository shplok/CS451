package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a break-statement.
 */
class JBreakStatement extends JStatement {

    private JStatement enclosingStatement;
    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */
    public JBreakStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        
        if (enclosingStatement == null) {
            JAST.compilationUnit.reportSemanticError(line(), "break statement must be inside a loop or switch");
        } else {
            // Mark the enclosing statement as having a break
            if (enclosingStatement instanceof JDoStatement) {
                ((JDoStatement) enclosingStatement).hasBreak = true;
            } else if (enclosingStatement instanceof JWhileStatement) {
                ((JWhileStatement) enclosingStatement).hasBreak = true;
            } else if (enclosingStatement instanceof JForStatement) {
                ((JForStatement) enclosingStatement).hasBreak = true;
            } else if (enclosingStatement instanceof JSwitchStatement) {
                ((JSwitchStatement) enclosingStatement).hasBreak = true;
            } else {
                JAST.compilationUnit.reportSemanticError(line(), "break statement must be inside a loop or switch");
            }
        }
        
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
         // Access the break label via the enclosing statement
         if (enclosingStatement != null) {
            String breakLabel = null;
            
            if (enclosingStatement instanceof JDoStatement) {
                breakLabel = ((JDoStatement) enclosingStatement).breakLabel;
            } else if (enclosingStatement instanceof JWhileStatement) {
                breakLabel = ((JWhileStatement) enclosingStatement).breakLabel;
            } else if (enclosingStatement instanceof JForStatement) {
                breakLabel = ((JForStatement) enclosingStatement).breakLabel;
            } else if (enclosingStatement instanceof JSwitchStatement) {
                breakLabel = ((JSwitchStatement) enclosingStatement).breakLabel;
            }
            
            // Generate an unconditional jump to the break label
            if (breakLabel != null) {
                output.addBranchInstruction(GOTO, breakLabel);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}
