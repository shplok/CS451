package iota;

import static iota.CLConstants.GOTO;
import static iota.CLConstants.ICONST_0;
import static iota.CLConstants.ICONST_1;
import static iota.CLConstants.INEG;

/**
 * This abstract base class is the AST node for a unary expression --- an expression with a single operand.
 */
abstract class IUnaryExpression extends IExpression {
    /**
     * The unary operator.
     */
    protected String operator;

    /**
     * The operand.
     */
    protected IExpression operand;

    /**
     * Constructs an AST node for a unary expression.
     *
     * @param line     line in which the unary expression occurs in the source file.
     * @param operator the unary operator.
     * @param operand  the operand.
     */
    protected IUnaryExpression(int line, String operator, IExpression operand) {
        super(line);
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IUnaryExpression:" + line, e);
        e.addAttribute("operator", operator);
        e.addAttribute("type", type == null ? "" : type.toString());
        JSONElement e1 = new JSONElement();
        e.addChild("Operand", e1);
        operand.toJSON(e1);
    }
}

/**
 * The AST node for a logical NOT (!) expression.
 */
class ILogicalNotOp extends IUnaryExpression {
    /**
     * Constructs an AST for a logical NOT expression.
     *
     * @param line line in which the logical NOT expression occurs in the source file.
     * @param arg  the operand.
     */
    public ILogicalNotOp(int line, IExpression arg) {
        super(line, "!", arg);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        operand = operand.analyze(context);
        operand.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String falseLabel = output.createLabel();
        String trueLabel = output.createLabel();
        this.codegen(output, falseLabel, false);
        output.addNoArgInstruction(ICONST_1); // true
        output.addBranchInstruction(GOTO, trueLabel);
        output.addLabel(falseLabel);
        output.addNoArgInstruction(ICONST_0); // false
        output.addLabel(trueLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        operand.codegen(output, targetLabel, !onTrue);
    }
}

/**
 * The AST node for a unary negation (-) expression.
 */
class INegateOp extends IUnaryExpression {
    /**
     * Constructs an AST node for a negation expression.
     *
     * @param line    line in which the negation expression occurs in the source file.
     * @param operand the operand.
     */
    public INegateOp(int line, IExpression operand) {
        super(line, "-", operand);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        operand = operand.analyze(context);
        operand.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        operand.codegen(output);
        output.addNoArgInstruction(INEG);
    }
}
