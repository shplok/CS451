package iota;

import java.util.ArrayList;

import static iota.CLConstants.ICONST_0;
import static iota.CLConstants.IRETURN;
import static iota.CLConstants.RETURN;

/**
 * The AST node for a method declaration.
 */
class IMethodDeclaration extends IAST {
    /**
     * Method modifiers.
     */
    protected ArrayList<String> mods;

    /**
     * Method name.
     */
    protected String name;

    /**
     * Return type.
     */
    protected Type returnType;

    /**
     * The formal parameters.
     */
    protected ArrayList<IFormalParameter> params;

    /**
     * Method body.
     */
    protected IBlock body;

    /**
     * Method context (built in analyze()).
     */
    protected MethodContext context;

    /**
     * Method descriptor (computed in preAnalyze()).
     */
    protected String descriptor;

    /**
     * Method signature (also computed in preAnalyze()).
     */
    protected String signature;

    /**
     * Constructs an AST node for a method declaration.
     *
     * @param line       line in which the method declaration occurs in the source file.
     * @param name       method name.
     * @param returnType return type.
     * @param params     the formal parameters.
     * @param body       method body.
     */
    public IMethodDeclaration(int line, String name, Type returnType, ArrayList<IFormalParameter> params, IBlock body) {
        super(line);
        this.name = name;
        this.returnType = returnType;
        this.params = params;
        this.body = body;

        // The method is implicitly public and static.
        mods = new ArrayList<>();
        mods.add("public");
        mods.add("static");
    }

    /**
     * {@inheritDoc}
     */
    public void preAnalyze(Context context, CLEmitter partial) {
        // Resolve types of the formal parameters, the return type, and compute method descriptor/signature.
        descriptor = "(";
        Type[] argTypes = new Type[params.size()];
        for (int i = 0; i < params.size(); i++) {
            IFormalParameter param = params.get(i);
            param.setType(param.type().resolve(context));
            descriptor += param.type().toDescriptor();
            argTypes[i] = param.type();
        }
        returnType = returnType.resolve(context);
        descriptor += ")" + returnType.toDescriptor();
        signature = Type.signatureFor(name, argTypes);

        // If this is the entry point method, record it in the compilation unit.
        if (name.equals("main") && descriptor.equals("()V")) {
            IAST.compilationUnit.hasEntryPoint();
        }

        // Add the method with an empty body (for now) to the partial class, if it doesn't exist already.
        if (partial.containsMethodSignature(signature)) {
            IAST.compilationUnit.reportSemanticError(line(), "redefining method " + signature);
            return;
        }
        partial.addMethod(mods, name, descriptor, null, false);
        partial.addMethodSignature(signature);
        if (returnType == Type.VOID) {
            partial.addNoArgInstruction(RETURN);
        } else {
            partial.addNoArgInstruction(ICONST_0);
            partial.addNoArgInstruction(IRETURN);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IAST analyze(Context context) {
        MethodContext methodContext = new MethodContext(context, returnType);
        this.context = methodContext;

        // Declare the parameters. We consider a formal parameter to be always initialized, via a method call.
        for (IFormalParameter param : params) {
            LocalVariableDefn defn = new LocalVariableDefn(param.type(), this.context.nextOffset());
            defn.initialize();
            this.context.addEntry(param.line(), param.name(), defn);
        }

        body = body.analyze(this.context);
        if (returnType != Type.VOID && !methodContext.methodHasReturn()) {
            IAST.compilationUnit.reportSemanticError(line(), "non-void method must have a return statement");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        output.addMethod(mods, name, descriptor, null, false);
        if (body != null) {
            body.codegen(output);
        }
        if (returnType == Type.VOID) {
            output.addNoArgInstruction(RETURN);
        }
    }

    /**
     * Adds information pertaining to this context to the given JSON element.
     *
     * @param json JSON element.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IMethodDeclaration:" + line, e);
        e.addAttribute("returnType", returnType == null ? "" : returnType.toString());
        e.addAttribute("name", name);
        if (params != null) {
            ArrayList<String> value = new ArrayList<>();
            for (IFormalParameter param : params) {
                value.add(String.format("[\"%s\", \"%s\"]", param.name(), param.type() == null ? "" :
                        param.type().toString()));
            }
            e.addAttribute("parameters", value);
        }
        if (context != null) {
            context.toJSON(e);
        }
        if (body != null) {
            body.toJSON(e);
        }
    }
}
