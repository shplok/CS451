package iota;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * A class for generating Marvin code.
 */
public class NEmitter {
    // Source program file name.
    private String sourceFile;

    // Destination directory for the Marvin code (ie, .marv file).
    private String destDir;

    // Whether an error occurred while creating/writing Marvin code.
    private boolean errorHasOccurred;

    // List of control flow graphs (one per method in the source program).
    private ArrayList<NControlFlowGraph> cfgs;

    /**
     * Maps method identifier (ie, combination of its name and descriptor) to its address (ie, where the method is
     * defined in the text segment of Marvin's main memory.
     */
    public static HashMap<String, Integer> methodAddresses;

    /**
     * Program counter for Marvin instructions.
     */
    public static int pc;

    /**
     * Constructs an NEmitter object.
     *
     * @param sourceFile the source iota program file name.
     * @param clFile     JVM bytecode representation of the program.
     * @param ra         register allocation scheme (naive or graph) to use.
     * @param verbose    whether to produce verbose output or not.
     */
    public NEmitter(String sourceFile, CLFile clFile, String ra, boolean verbose) {
        this.sourceFile = sourceFile;
        cfgs = new ArrayList<>();
        methodAddresses = new HashMap<>();
        CLConstantPool cp = clFile.constantPool;
        pc = 2; // 0 and 1 are for emitting instructions to call the entry point method main()V and halt.
        for (CLMethodInfo mInfo : clFile.methods) {
            String name = new String(((CLConstantUtf8Info) cp.cpItem(mInfo.nameIndex)).b);
            String desc = new String(((CLConstantUtf8Info) cp.cpItem(mInfo.descriptorIndex)).b);

            // Booleans are implicitly integers (1 for true and 0 for false).
            desc = desc.replace("Z", "I");

            // We ignore these IO methods: read()I, write(I)V, and write(Z)V.
            if (name.equals("read") && desc.equals("()I") || name.equals("write") && 
                desc.equals("(I)V") || name.equals("write") && desc.equals("(Z)V")) {
                continue;
            }

            // Build a control flow graph (cfg) for this method. Each block in the cfg, at the end of this step, has
            // the JVM bytecode translated into its tuple representation.
            NControlFlowGraph cfg = new NControlFlowGraph(cp, mInfo, name, desc);

            // Identify basic blocks that are loop headers (LHs) and loop tails (LTs).
            cfg.detectLoops(cfg.basicBlocks.get(0), null);

            // Remove the basic blocks that are not reachable from the source block (B0).
            cfg.removeUnreachableBlocks();

            // Convert the tuples to HIR instructions.
            cfg.tuplesToHir();

            // Remove the redundant Phi functions.
            cfg.cleanupPhiFunctions();

            // Convert HIR instructions to LIR instructions.
            cfg.hirToLir();

            // Resolve Phi functions by inserting appropriate "copy" instructions in the predecessor blocks.
            cfg.resolvePhiFunctions();

            // Renumber the LIR instructions.
            cfg.renumberLir();

            // Perform register allocation.
            NRegisterAllocator regAllocator;
            if (ra.equals("naive")) {
                regAllocator = new NNaiveRegisterAllocator(cfg);
            } else {
                regAllocator = new NGraphRegisterAllocator(cfg);
            }
            regAllocator.run();

            // If verbose output is requested, write the IRs (tuples, HIR, LIR), liveness sets, and liveness
            // intervals for the cfg to standard output.
            if (verbose) {
                PrettyPrinter p = new PrettyPrinter();
                p.printf(">>> %s%s\n\n", cfg.name, cfg.desc);
                cfg.writeTuplesToStdOut(p);
                cfg.writeHirToStdOut(p);
                cfg.writeLirToStdOut(p);
                cfg.writeLivenessSetsToStdOut(p);
                cfg.writeLivenessIntervalsToStdOut(p);
                p.println();
            }

            // Convert LIR instructions to Marvin instructions.
            cfg.lirToMarvin();

            // Generate Marvin code for creating a stack frame upon method entry and destroying the frame upon exit.
            cfg.prepareMethodEntryAndExit();

            // Resolve jumps within a method. This does not include method calls, which are handled within write() by
            // calling cfg.resolveCalls().
            cfg.resolveJumps();

            // Save the cfg in the list of cfgs.
            cfgs.add(cfg);
        }
    }

    /**
     * Sets the destination directory for the .marv file.
     *
     * @param destDir the destination directory.
     */
    public void destinationDir(String destDir) {
        this.destDir = destDir;
    }

    /**
     * Returns true if an emitter error has occurred up to now, and false otherwise.
     *
     * @return true if an emitter error has occurred up to now, and false otherwise.
     */
    public boolean errorHasOccurred() {
        return errorHasOccurred;
    }

    /**
     * Writes out .marv file to the file system. The destination directory for the files can be set using the
     * destinationDir() method.
     */
    public void write() {
        String s = sourceFile.replace(".iota", ".marv");
        String[] tokens = s.split("[\\\\|/]");
        String outFileName = tokens[tokens.length - 1];
        String outFile = destDir + File.separator + outFileName;
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outFile));
            out.printf("# %s (last modified: %s)\n\n", outFileName, new Date());

            // Emit instructions to jump to entry point method main()V and halt.
            int N = methodAddresses.get("main()V");
            out.printf("%-6s%-8s%-8s%-8s%-8s# %s\n", 0, "calln", "r12", N, "", "call method @" + N);
            out.printf("%-6s%-8s%-8s%-8s%-8s# %s\n\n", 1, "halt", "", "", "", "halt the machine");

            // Emit instructions for each method.
            for (NControlFlowGraph cfg : cfgs) {
                cfg.resolveCalls();
                cfg.write(out);
            }

            out.close();
        } catch (IOException e) {
            reportEmitterError("cannot write to file %s", outFile);
        }
    }

    // Reports any error that occurs while creating/writing the spim file, to standard error.
    private void reportEmitterError(String message, Object... args) {
        System.err.printf("Error: " + message, args);
        System.err.println();
        errorHasOccurred = true;
    }
}

/**
 * A utility class that allows pretty (indented) printing to standard output.
 */
class PrettyPrinter {
    // Width of an indentation.
    private int indentWidth;

    // Current indentation (number of blank spaces).
    private int indent;

    /**
     * Constructs a pretty printer with an indentation width of 2.
     */
    public PrettyPrinter() {
        this(2);
    }

    /**
     * Constructs a pretty printer.
     *
     * @param indentWidth number of blank spaces for an indent.
     */
    public PrettyPrinter(int indentWidth) {
        this.indentWidth = indentWidth;
        indent = 0;
    }

    /**
     * Indents right.
     */
    public void indentRight() {
        indent += indentWidth;
    }

    /**
     * Indents left.
     */
    public void indentLeft() {
        if (indent > 0) {
            indent -= indentWidth;
        }
    }

    /**
     * Prints an empty line to standard output.
     */
    public void println() {
        doIndent();
        System.out.println();
    }

    /**
     * Prints the specified string (followed by a newline) to standard output.
     *
     * @param s string to print.
     */

    public void println(String s) {
        doIndent();
        System.out.println(s);
    }

    /**
     * Prints the specified string to standard output.
     *
     * @param s string to print.
     */
    public void print(String s) {
        doIndent();
        System.out.print(s);
    }

    /**
     * Prints args to standard output according to the specified format.
     *
     * @param format format specifier.
     * @param args   values to print.
     */
    public void printf(String format, Object... args) {
        doIndent();
        System.out.printf(format, args);
    }

    // Indents by printing spaces to standard output.
    private void doIndent() {
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
    }
}
