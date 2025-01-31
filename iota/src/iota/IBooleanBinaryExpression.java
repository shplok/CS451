package iota;

import static iota.CLConstants.GOTO;
import static iota.CLConstants.ICONST_0;
import static iota.CLConstants.ICONST_1;
import static iota.CLConstants.IF_ICMPEQ;
import static iota.CLConstants.IF_ICMPNE;

/**
 * This abstract base class is the AST node for binary expressions that return booleans.
 */
abstract class IBooleanBinaryExpression extends IBinaryExpression {
    /**
     * Constructs an AST node for a boolean binary expression.
     *
     * @param line     line in which the boolean binary expression occurs in the source file.
     * @param operator the boolean binary operator.
     * @param lhs      lhs operand.
     * @param rhs      rhs operand.
     */
    protected IBooleanBinaryExpression(int line, String operator, IExpression lhs, IExpression rhs) {
        super(line, operator, lhs, rhs);
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
}

/**
 * The AST node for an equality (==) expression.
 */
class IEqualOp extends IBooleanBinaryExpression {
    /**
     * Constructs an AST node for an equality expression.
     *
     * @param line line number in which the equality expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */

    public IEqualOp(int line, IExpression lhs, IExpression rhs) {
        super(line, "==", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), rhs.type());
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addBranchInstruction(onTrue ? IF_ICMPEQ : IF_ICMPNE, targetLabel);
    }
}

/**
 * The AST node for a logical-and (&amp;&amp;) expression.
 */
class ILogicalAndOp extends IBooleanBinaryExpression {
    /**
     * Constructs an AST node for a logical-and expression.
     *
     * @param line line in which the logical-and expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public ILogicalAndOp(int line, IExpression lhs, IExpression rhs) {
        super(line, "&&", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        rhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        if (onTrue) {
            String falseLabel = output.createLabel();
            lhs.codegen(output, falseLabel, false);
            rhs.codegen(output, targetLabel, true);
            output.addLabel(falseLabel);
        } else {
            lhs.codegen(output, targetLabel, false);
            rhs.codegen(output, targetLabel, false);
        }
    }
}

/**
 * The AST node for a logical-or (||) expression.
 */
class ILogicalOrOp extends IBooleanBinaryExpression {
    /**
     * Constructs an AST node for a logical-or expression.
     *
     * @param line line in which the logical-or expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public ILogicalOrOp(int line, IExpression lhs, IExpression rhs) {
        super(line, "||", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        rhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        if (onTrue) {
            lhs.codegen(output, targetLabel, true);
            rhs.codegen(output, targetLabel, true);
        } else {
            String trueLabel = output.createLabel();
            lhs.codegen(output, trueLabel, true);
            rhs.codegen(output, targetLabel, false);
            output.addLabel(trueLabel);
        }
    }
}

/**
 * The AST node for a not-equal-to (!=) expression.
 */
class INotEqualOp extends IBooleanBinaryExpression {
    /**
     * Constructs an AST node for not-equal-to (!=) expression.
     *
     * @param line line number in which the not-equal-to (!=) expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */

    public INotEqualOp(int line, IExpression lhs, IExpression rhs) {
        super(line, "!=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), rhs.type());
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addBranchInstruction(onTrue ? IF_ICMPNE : IF_ICMPEQ, targetLabel);
    }
}
