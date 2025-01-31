package iota;

import java.io.FileNotFoundException;

/**
 * This is the main entry point for the iota compiler. The compiler proceeds as follows:
 * <ol>
 *   <li>It reads arguments that affects its behavior.</li>
 *
 *   <li>It builds a scanner.</li>
 *
 *   <li>It builds a parser (using the scanner) and parses the input for producing an abstact syntax tree (AST).</li>
 *
 *   <li>It sends the preAnalyze() message to that AST, which recursively descends the tree so far as the member
 *   headers for declaring types (just one in iota, which is the type induced by the program. For example, the type T
 *   for the program T.java) and their members in the symbol table within the compilation unit context.</li>
 *
 *   <li>It sends the analyze() message to that AST for declaring local variables, and checking and assigning types
 *   to expressions. Analysis also sometimes rewrites some of the abstract syntax tree for clarifying the semantics.
 *   Analysis does all of this by recursively descending the AST down to its leaves.</li>
 *
 *   <li>Finally, it sends a codegen() message to the AST for generating the target language code. Again, codegen()
 *   recursively descends the tree, down to its leaves, generating a .marv file.</li>
 * </ol>
 */
class Main {
    // Added to eliminate javadoc warning.
    private Main() {
    }

    /**
     * Entry point.
     *
     * @param args the command-line arguments.
     */
    public static void main(String[] args) {
        String caller = "java iota.Main";
        String sourceFile = "";
        String registerAllocation = "naive";
        String outputDir = ".";
        boolean verbose = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("iota")) {
                caller = "iota";
            } else if (args[i].endsWith(".iota")) {
                sourceFile = args[i];
            } else if (args[i].equals("-g")) {
                registerAllocation = "graph";
            } else if (args[i].endsWith("-d") && (i + 1) < args.length) {
                outputDir = args[++i];
            } else if (args[i].equals("-v")) {
                verbose = true;
            } else {
                printUsage(caller);
                return;
            }
        }
        if (sourceFile.isEmpty()) {
            printUsage(caller);
            return;
        }

        LookaheadScanner scanner;
        try {
            scanner = new LookaheadScanner(sourceFile);
        } catch (FileNotFoundException e) {
            System.err.println("Error: file " + sourceFile + " not found.");
            return;
        }

        // Scan/parse the source program.
        Parser parser = new Parser(scanner);
        ICompilationUnit ast = parser.compilationUnit();
        if (parser.errorHasOccurred()) {
            return;
        }

        // Pre-analyze the program.
        CLEmitter partial = new CLEmitter(false);
        CLEmitter.initializeByteClassLoader();
        ast.preAnalyze(null, partial);
        if (IAST.compilationUnit.errorHasOccurred()) {
            return;
        }

        // Analyze the program.
        ast.analyze(null);
        if (IAST.compilationUnit.errorHasOccurred()) {
            return;
        }

        // Generate in-memory JVM bytecode.
        CLEmitter jvmCode = new CLEmitter(false);
        CLEmitter.initializeByteClassLoader();
        ast.codegen(jvmCode);
        if (jvmCode.errorHasOccurred()) {
            return;
        }

        // Generate Marvin code.
        NEmitter marvCode = new NEmitter(sourceFile, jvmCode.clFile(), registerAllocation, verbose);
        marvCode.destinationDir(outputDir);
        marvCode.write();
        if (marvCode.errorHasOccurred()) {
            System.err.println("Error: compilation failed!");
        }
    }

    // Prints command usage to STDOUT.
    private static void printUsage(String caller) {
        String usage = "Usage: " + caller
                + " <options> <source file>\n"
                + "Where possible options include:\n"
                + "  -g  Allocate registers using graph coloring method; default = naive method\n"
                + "  -v  Display intermediate representations and liveness intervals\n"
                + "  -d  <dir> Specify where to place output (.marv) file; default = .";
        System.out.println(usage);
    }
}
