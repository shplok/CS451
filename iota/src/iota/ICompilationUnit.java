package iota;

import java.util.ArrayList;

import static iota.CLConstants.ICONST_0;
import static iota.CLConstants.IRETURN;
import static iota.CLConstants.RETURN;

/**
 * The abstract syntax tree (AST) node representing a compilation unit, and so the root of the AST.
 * <p>
 * The AST is produced by the Parser. Once the AST has been built, three successive methods are invoked:
 * <ol>
 *   <li>Method preAnalyze() is invoked for making a first pass at type analysis, recursively reaching down to the
 *   method headers. preAnalyze() creates a partial class file (in memory) with this information.</li>
 *
 *   <li>Method analyze() is invoked for type-checking method bodies and determining the types of all expressions. A
 *   certain amount of tree surgery is also done here. And stack frame offsets are computed for method parameters and
 *   local variables.</li>
 *
 *   <li>Method codegen() is invoked for generating intermediate, in-memory JVM bytecode for the compilation unit. It
 *   calls methods on the CLEmitter object output this purpose. Of course, codegen(), starting at the root of the
 *   AST, makes recursive calls down the tree, to the codegen() method at each node, for generating the appropriate
 *   instructions.</li>
 * </ol>
 */
class ICompilationUnit extends IAST {
    /**
     * Modifiers of the class (ie, type) induced by this compilation unit.
     */
    protected ArrayList<String> mods;

    // Name of the source file.
    private final String fileName;

    // List of method declarations.
    private final ArrayList<IMethodDeclaration> methodDeclarations;

    // Whether the entry point method exists?
    private boolean hasEntryPoint;

    // For method declarations.
    private CompilationUnitContext context;

    // Type induced by this compilation unit.
    private Type thisType;

    // Whether a semantic error has been found.
    private boolean isInError;

    /**
     * Constructs an AST node for a compilation unit.
     *
     * @param fileName           the name of the source file.
     * @param line               line in which the compilation unit occurs in the source file.
     * @param methodDeclarations method declarations.
     */
    public ICompilationUnit(String fileName, int line, ArrayList<IMethodDeclaration> methodDeclarations) {
        super(line);
        this.fileName = fileName;
        this.methodDeclarations = methodDeclarations;
        isInError = false;
        compilationUnit = this;

        // The induced type is implicitly public.
        mods = new ArrayList<>();
        mods.add("public");
    }

    /**
     * Records the fact that the entry point method (void main()) exists.
     */
    public void hasEntryPoint() {
        hasEntryPoint = true;
    }

    /**
     * Returns the type induced by this compilation unit.
     *
     * @return the type induced by this compilation unit.
     */
    public Type thisType() {
        return thisType;
    }

    /**
     * Returns true if a semantic error has occurred up to now, and false otherwise.
     *
     * @return true if a semantic error has occurred up to now, and false otherwise..
     */
    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * Reports a semantic error.
     *
     * @param line      line in which the error occurred in the source file.
     * @param message   message identifying the error.
     * @param arguments related values.
     */
    public void reportSemanticError(int line, String message, Object... arguments) {
        isInError = true;
        System.err.printf("%s:%d: error: ", fileName, line);
        System.err.printf(message, arguments);
        System.err.println();
    }

    /**
     * {@inheritDoc}
     */
    public void preAnalyze(Context context, CLEmitter partial) {
        this.context = new CompilationUnitContext(this);
        initializeInducedType(partial);

        // Add the method headers to the induced (partial) type.
        for (IMethodDeclaration methodDeclaration : methodDeclarations) {
            methodDeclaration.preAnalyze(this.context, partial);
        }

        // Add an implicit entry point method (void main()) if one does not exist.
        if (!hasEntryPoint) {
            ArrayList<IFormalParameter> params = new ArrayList<>();
            ArrayList<IStatement> stmts = new ArrayList<>();
            IBlock body = new IBlock(0, stmts);
            IMethodDeclaration entryPoint = new IMethodDeclaration(0, "main", Type.VOID, params, body);
            entryPoint.preAnalyze(this.context, partial);
            methodDeclarations.add(entryPoint);
        }

        thisType = Type.typeFor(partial.toClass());

        // Declare the induced type.
        this.context.addType(line, thisType);
    }

    /**
     * {@inheritDoc}
     */
    public IAST analyze(Context context) {
        for (IMethodDeclaration methodDeclaration : methodDeclarations) {
            methodDeclaration.analyze(this.context);
        }



        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        initializeInducedType(output);
        for (IMethodDeclaration methodDeclaration : methodDeclarations) {
            methodDeclaration.codegen(output);
        }
        output.write();
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("ICompilationUnit:" + line, e);
        e.addAttribute("source", fileName);
        if (context != null) {
            context.toJSON(e);
        }
        if (methodDeclarations != null) {
            for (IMethodDeclaration methodDeclaration : methodDeclarations) {
                methodDeclaration.toJSON(e);
            }
        }
    }

    // Adds the following to the class represented by output:
    //   1. Class header.
    //   2. Implicit IO method, public static int read()
    //   3. Implicit IO method, public static void write(int)
    //   4. Implicit IO method, public static void write(boolean)
    private void initializeInducedType(CLEmitter output) {
        String name = fileName.replace(".iota", "");
        String[] tokens = name.split("[\\\\|/]");
        output.addClass(mods, tokens[tokens.length - 1], "java/lang/Object", null, false);
        ArrayList<String> mods2 = new ArrayList<>();
        mods2.add("public");
        mods2.add("static");
        output.addMethod(mods2, "read", "()I", null, false);
        output.addNoArgInstruction(ICONST_0);
        output.addNoArgInstruction(IRETURN);
        output.addMethod(mods2, "write", "(I)V", null, false);
        output.addNoArgInstruction(RETURN);
        output.addMethod(mods2, "write", "(Z)V", null, false);
        output.addNoArgInstruction(RETURN);
    }
}
