package iota;

import java.util.ArrayList;

import static iota.CLConstants.IFEQ;
import static iota.CLConstants.IFNE;
import static iota.CLConstants.INVOKESTATIC;
import static iota.CLConstants.POP;

/**
 * The AST node for a message expression that has a target (the type corresponding to the compilation unit in which
 * the message is invoked), a message name, and zero or more actual arguments.
 */
class IMessageExpression extends IExpression {
    // The target expression.
    private Type targetType;

    // The message name.
    private final String messageName;

    // Message arguments.
    private final ArrayList<IExpression> arguments;

    // The Method representing this message.
    private Method method;

    /**
     * Constructs an AST node for a message expression.
     *
     * @param line        line in which the expression occurs in the source file.
     * @param messageName the message name.
     * @param arguments   the arguments.
     */
    public IMessageExpression(int line, String messageName, ArrayList<IExpression> arguments) {
        super(line);
        this.messageName = messageName;
        this.arguments = arguments;
    }

    /**
     * {@inheritDoc}
     */
    public IExpression analyze(Context context) {
        targetType = context.definingType();

        // Analyze the arguments, collecting their types (in Class form) as argTypes.
        Type[] argTypes = new Type[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            arguments.set(i, arguments.get(i).analyze(context));
            argTypes[i] = arguments.get(i).type();
        }

        method = targetType.methodFor(messageName, argTypes);
        if (method == null) {
            IAST.compilationUnit.reportSemanticError(line(),
                    "cannot find method for: " + Type.signatureFor(messageName, argTypes));
            type = Type.ANY;
        } else {
            type = method.returnType();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        for (IExpression argument : arguments) {
            argument.codegen(output);
        }
        output.addMemberAccessInstruction(INVOKESTATIC, targetType.jvmName(), messageName, method.toDescriptor());
        if (isStatementExpression && type != Type.VOID) {
            // Pop any value left on the stack.
            output.addNoArgInstruction(POP);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        codegen(output);
        output.addBranchInstruction(onTrue ? IFNE : IFEQ, targetLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IMessageExpression:" + line, e);
        e.addAttribute("returnType", type == null ? "" : type.toString());
        e.addAttribute("targetType", targetType == null ? "" : targetType.toString());
        e.addAttribute("name", messageName);
        if (arguments != null) {
            for (IExpression argument : arguments) {
                JSONElement e1 = new JSONElement();
                e.addChild("Argument", e1);
                argument.toJSON(e1);
            }
        }
    }
}
