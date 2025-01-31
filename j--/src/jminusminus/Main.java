package jminusminus;

import java.io.FileNotFoundException;

import static jminusminus.TokenKind.EOF;

/**
 * Driver class for j-- compiler using hand-written front-end. This is the main entry point for the compiler. The
 * compiler proceeds as follows:
 * <ol>
 *   <li>It reads arguments that affects its behavior.</li>
 *
 *   <li>It builds a scanner.</li>
 *
 *   <li>It builds a parser (using the scanner) and parses the input for producing an abstact syntax tree (AST).</li>
 *
 *   <li>It sends the preAnalyze() message to that AST, which recursively descends the tree so far as the member
 *   headers for declaring types and members in the symbol table (represented as a string of contexts).</li>
 *
 *   <li>It sends the analyze() message to that AST for declaring local variables, and checking and assigning types
 *   to expressions. Analysis also sometimes rewrites some of the abstract syntax tree for clarifying the semantics.
 *   Analysis does all of this by recursively descending the AST down to its leaves.</li>
 *
 *   <li>Finally, it sends a codegen() message to the AST for generating code. Again, codegen() recursively descends
 *   the tree, down to its leaves, generating JVM code for producing a .class for each defined type (class).</li>
 * </ol>
 */
public class Main {
    /**
     * Constructs a Main object.
     */
    public Main() {
        // Nothing here.
    }

    /**
     * Entry point.
     *
     * @param args the command-line arguments.
     */
    public static void main(String[] args) {
        String caller = "java jminusminus.Main";
        String sourceFile = "";
        String debugOption = "";
        String outputDir = ".";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("j--")) {
                caller = "j--";
            } else if (args[i].endsWith(".java")) {
                sourceFile = args[i];
            } else if (args[i].equals("-t") || args[i].equals("-p") || args[i].equals("-pa") || args[i].equals("-a")) {
                debugOption = args[i];
            } else if (args[i].endsWith("-d") && (i + 1) < args.length) {
                outputDir = args[++i];
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

        if (debugOption.equals("-t")) {
            // Just tokenize input and print the tokens to STDOUT.
            TokenInfo token;
            do {
                scanner.next();
                token = scanner.token();
                System.out.printf("%d\t : %s = %s\n", token.line(), token.tokenRep(), token.image());
            } while (token.kind() != EOF);
            return;
        }

        Parser parser = new Parser(scanner);
        JCompilationUnit ast = parser.compilationUnit();
        if (debugOption.equals("-p")) {
            // Just parse input and print AST to STDOUT.
            JSONElement json = new JSONElement();
            ast.toJSON(json);
            System.out.println(json);
            return;
        }
        if (parser.errorHasOccurred()) {
            return;
        }

        ast.preAnalyze();
        if (debugOption.equals("-pa")) {
            // Just pre-analyze input and print AST to STDOUT.
            JSONElement json = new JSONElement();
            ast.toJSON(json);
            System.out.println(json);
            return;
        }
        if (JAST.compilationUnit.errorHasOccurred()) {
            return;
        }

        ast.analyze(null);
        if (debugOption.equals("-a")) {
            // Just analyze input and print AST to STDOUT.
            JSONElement json = new JSONElement();
            ast.toJSON(json);
            System.out.println(json);
            return;
        }
        if (JAST.compilationUnit.errorHasOccurred()) {
            return;
        }

        // Generate JVM code.
        CLEmitter jvmCode = new CLEmitter(true);
        jvmCode.destinationDir(outputDir);
        ast.codegen(jvmCode);
        if (jvmCode.errorHasOccurred()) {
            System.err.println("Error: compilation failed!");
        }
    }

    // Prints command usage to STDOUT.
    private static void printUsage(String caller) {
        String usage = "Usage: " + caller
                + " <options> <source file>\n"
                + "Where possible options include:\n"
                + "  -t  Tokenize input and print tokens to STDOUT\n"
                + "  -p  Parse input and print AST to STDOUT\n"
                + "  -pa Pre-analyze input and print AST to STDOUT\n"
                + "  -a  Analyze input and print AST to STDOUT\n"
                + "  -d  <dir> Specify where to place output (.class) files; default = .";
        System.out.println(usage);
    }
}
