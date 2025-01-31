package iota;

import static iota.CLConstants.IF_ICMPGE;
import static iota.CLConstants.IF_ICMPGT;
import static iota.CLConstants.IF_ICMPLE;
import static iota.CLConstants.IF_ICMPLT;

/**
 * This abstract base class is the AST node for a comparison expression.
 */
abstract class IComparisonExpression extends IBooleanBinaryExpression {
    /**
     * Constructs an AST node for a comparison expression.
     *
     * @param line     line in which the expression occurs in the source file.
     * @param operator the comparison operator.
     * @param lhs      the lhs operand.
     * @param rhs      the rhs operand.
     */
    protected IComparisonExpression(int line, String operator, IExpression lhs, IExpression rhs) {
        super(line, operator, lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.BOOLEAN;
        return this;
    }
}

/**
 * The AST node for a greater-than-or-equal-to (&gt;=) expression.
 */
class IGreaterEqualOp extends IComparisonExpression {

    /**
     * Constructs an AST node for a greater-than-or-equal-to expression.
     *
     * @param line line in which the greater-than-or-equal-to expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public IGreaterEqualOp(int line, IExpression lhs, IExpression rhs) {
        super(line, ">=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addBranchInstruction(onTrue ? IF_ICMPGE : IF_ICMPLT, targetLabel);
    }
}

/**
 * The AST node for a greater-than (&gt;) expression.
 */
class IGreaterThanOp extends IComparisonExpression {
    /**
     * Constructs an AST node for a greater-than expression.
     *
     * @param line line in which the greater-than expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public IGreaterThanOp(int line, IExpression lhs, IExpression rhs) {
        super(line, ">", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addBranchInstruction(onTrue ? IF_ICMPGT : IF_ICMPLE, targetLabel);
    }
}

/**
 * The AST node for a less-than-or-equal-to (&lt;=) expression.
 */
class ILessEqualOp extends IComparisonExpression {

    /**
     * Constructs an AST node for a less-than-or-equal-to expression.
     *
     * @param line line in which the less-than-or-equal-to expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public ILessEqualOp(int line, IExpression lhs, IExpression rhs) {
        super(line, "<=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addBranchInstruction(onTrue ? IF_ICMPLE : IF_ICMPGT, targetLabel);
    }
}

/**
 * The AST node for a less-than (&lt;) expression.
 */
class ILessThanOp extends IComparisonExpression {
    /**
     * Constructs an AST node for a less-than expression.
     *
     * @param line line in which the less-than expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public ILessThanOp(int line, IExpression lhs, IExpression rhs) {
        super(line, "<", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addBranchInstruction(onTrue ? IF_ICMPLT : IF_ICMPGE, targetLabel);
    }
}
