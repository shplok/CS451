package jminusminus;

import static jminusminus.CLConstants.LOOKUPSWITCH;
import static jminusminus.CLConstants.TABLESWITCH;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * The AST node for a switch-statement.
 */
class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    protected boolean hasBreak;
    
    protected String breakLabel;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> switchStmtGroups;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line             line in which the switch-statement occurs in the source file.
     * @param condition        test expression.
     * @param switchStmtGroups list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition, ArrayList<SwitchStatementGroup> switchStmtGroups) {
        super(line);
        this.condition = condition;
        this.switchStmtGroups = switchStmtGroups;
        this.hasBreak = false;
        this.breakLabel = null;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // Push this onto JMember.enclosingStatement upon entry
        JMember.enclosingStatement.push(this);
        
        // Analyze the condition
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.INT);
        
        // Create a new LocalContext with context as the parent
        LocalContext localContext = new LocalContext(context);
        
        // Analyze each case group
        for (SwitchStatementGroup group : switchStmtGroups) {
            // Analyze case labels
            for (int i = 0; i < group.getSwitchLabels().size(); i++) {
                JExpression label = group.getSwitchLabels().get(i);
                if (label != null) {
                    // Null represents default case
                    label = label.analyze(context);
                    label.type().mustMatchExpected(line(), Type.INT);
                    group.getSwitchLabels().set(i, label);
                }
            }
            
            // Analyze statements in the block
            for (int i = 0; i < group.block().size(); i++) {
                JStatement statement = group.block().get(i);
                group.block().set(i, (JStatement) statement.analyze(localContext));
            }
        }
        
        // Pop this from JMember.enclosingStatement upon exit
        JMember.enclosingStatement.pop();
        
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        condition.codegen(output);
        
        // Label for the default case
        String defaultLabel = output.createLabel();
        
        // Set breakLabel to an appropriate label if hasBreak is true
        if (hasBreak) {
            breakLabel = output.createLabel();
        } else {
            breakLabel = output.createLabel();
        }
        
        // Collect case values and corresponding labels
        TreeMap<Integer, String> switchCasePairs = new TreeMap<>();
        ArrayList<String> caseLabels = new ArrayList<>();
        
        // Find the lowest and highest case values
        int lo = Integer.MAX_VALUE;
        int hi = Integer.MIN_VALUE;
        
        // First pass: create labels and collect information
        for (SwitchStatementGroup group : switchStmtGroups) {
            for (JExpression label : group.getSwitchLabels()) {
                if (label != null) {
                    // Create a label for this case
                    String caseLabel = output.createLabel();
                    
                    // Assuming label is a JLiteralInt
                    int value = ((JLiteralInt) label).toInt();
                    
                    caseLabels.add(caseLabel);
                    switchCasePairs.put(value, caseLabel);
                    
                    // Update lo and hi
                    if (value < lo) lo = value;
                    if (value > hi) hi = value;
                }
            }
        }
        
        // Calculate total number of labels
        int nLabels = switchCasePairs.size();
        
        // Decide which instruction to emit based on heuristic
        long tableSpaceCost = 5 + hi - lo;
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * nLabels;
        long lookupTimeCost = nLabels;
        int opcode = nLabels > 0 && (tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost) ? 
                     TABLESWITCH : LOOKUPSWITCH;
        
        if (opcode == TABLESWITCH) {
            // For tableswitch, we need an ArrayList of labels ordered by case value
            ArrayList<String> tableLabels = new ArrayList<>();
            for (int i = lo; i <= hi; i++) {
                String label = switchCasePairs.get(i);
                if (label != null) {
                    tableLabels.add(label);
                } else {
                    tableLabels.add(defaultLabel);
                }
            }
            
            // Emit the tableswitch instruction with correct parameter order
            output.addTABLESWITCHInstruction(defaultLabel, lo, hi, tableLabels);
        } else {
            // For lookupswitch, we need the count and the map
            // Emit the lookupswitch instruction with correct parameters
            output.addLOOKUPSWITCHInstruction(defaultLabel, nLabels, switchCasePairs);
        }
        
        
        // Keep track of label usage to avoid defining the same label twice
        boolean defaultUsed = false;
        
        // Second pass: Generate code for each case group
        int labelIndex = 0;
        for (SwitchStatementGroup group : switchStmtGroups) {
            boolean groupHasDefault = false;
            
            // Add labels for this group
            for (JExpression label : group.getSwitchLabels()) {
                if (label == null) {
                    groupHasDefault = true;
                    if (!defaultUsed) {
                        output.addLabel(defaultLabel);
                        defaultUsed = true;
                    }
                } else if (labelIndex < caseLabels.size()) {
                    output.addLabel(caseLabels.get(labelIndex++));
                }
            }
            
            // Generate code for all statements in this group
            for (JStatement stmt : group.block()) {
                stmt.codegen(output);
            }
        }
        
        // Make sure default case is handled if not used yet
        if (!defaultUsed) {
            output.addLabel(defaultLabel);
        }
        
        // Add the break label at the end
        output.addLabel(breakLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : switchStmtGroups) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch-statement group consists of a list of switch labels and a block of statements.
 */
class SwitchStatementGroup {
    // Switch labels.
    private ArrayList<JExpression> switchLabels;

    // Block of statements.
    private ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels switch labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    /**
     * Returns the switch labels associated with this switch-statement group.
     *
     * @return the switch labels associated with this switch-statement group.
     */
    public ArrayList<JExpression> getSwitchLabels() {
        return switchLabels;
    }

    /**
     * Returns the block of statements associated with this switch-statement group.
     *
     * @return the block of statements associated with this switch-statement group.
     */
    public ArrayList<JStatement> block() {
        return block;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}
