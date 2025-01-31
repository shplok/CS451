package iota;

import static iota.CLConstants.IRETURN;
import static iota.CLConstants.RETURN;

/**
 * The AST node for a return-statement. If the enclosing method is non-void, then there is a value to return, so we
 * keep track of the expression denoting that value and its type.
 */
class IReturnStatement extends IStatement {
    // The returned expression.
    private IExpression expr;

    /**
     * Constructs an AST node for a return-statement.
     *
     * @param line line in which the return-statement appears in the source file.
     * @param expr the returned expression.
     */
    public IReturnStatement(int line, IExpression expr) {
        super(line);
        this.expr = expr;
    }

    /**
     * {@inheritDoc}
     */
    public IStatement analyze(Context context) {
        MethodContext methodContext = context.methodContext();
        Type returnType = methodContext.methodReturnType();
        methodContext.confirmMethodHasReturn();
        if (expr != null) {
            if (returnType == Type.VOID) {
                IAST.compilationUnit.reportSemanticError(line(), "cannot return a value from a void method");
            } else {
                expr = expr.analyze(context);
                expr.type().mustMatchExpected(line(), returnType);
            }
        } else {
            if (returnType != Type.VOID) {
                IAST.compilationUnit.reportSemanticError(line(), "missing return value");
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (expr == null) {
            output.addNoArgInstruction(RETURN);
        } else {
            expr.codegen(output);
            output.addNoArgInstruction(IRETURN);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IReturnStatement:" + line, e);
        if (expr != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Expression", e1);
            expr.toJSON(e1);
        }
    }
}
