package iota;

/**
 * This abstract base class is the AST node for a statement (includes expressions).
 */
abstract class IStatement extends IAST {
    /**
     * Constructs an AST node for a statement.
     *
     * @param line line in which the statement occurs in the source file.
     */
    protected IStatement(int line) {
        super(line);
    }
}
