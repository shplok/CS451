package iota;

import static iota.CLConstants.IFEQ;
import static iota.CLConstants.IFNE;
import static iota.CLConstants.ILOAD;

/**
 * The AST node for an identifier used as a primary expression.
 */
class IVariable extends IExpression {
    // The variable's name.
    private final String name;

    // The variable's definition.
    private Defn defn;

    /**
     * Constructs the AST node for a variable.
     *
     * @param line line in which the variable occurs in the source file.
     * @param name the name.
     */
    public IVariable(int line, String name) {
        super(line);
        this.name = name;
    }

    /**
     * Returns the identifier name.
     *
     * @return the identifier name.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the identifier's definition.
     *
     * @return the identifier's definition.
     */
    public Defn defn() {
        return defn;
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        defn = context.lookup(name);
        if (defn == null) {
            IAST.compilationUnit.reportSemanticError(line, "cannot find name: " + name);
            type = Type.ANY;
        } else {
            if (!((LocalVariableDefn) defn).isInitialized()) {
                IAST.compilationUnit.reportSemanticError(line, "variable " + name + " might not have been initialized");
            }
            type = defn.type();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        int offset = ((LocalVariableDefn) defn).offset();
        output.addOneArgInstruction(ILOAD, offset);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        if (defn instanceof LocalVariableDefn) {
            codegen(output);
            output.addBranchInstruction(onTrue ? IFNE : IFEQ, targetLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IVariable:" + line, e);
        e.addAttribute("name", name());
        e.addAttribute("type", type == null? "" : type.toString());
    }
}
