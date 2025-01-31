package iota;

import java.util.ArrayList;

/**
 * The AST node for a block, which delimits a nested level of scope.
 */
class IBlock extends IStatement {
    // List of statements forming the block body.
    private final ArrayList<IStatement> statements;

    // The new context (built in analyze()) represented by this block.
    private LocalContext context;

    /**
     * Constructs an AST node for a block.
     *
     * @param line       line in which the block occurs in the source file.
     * @param statements list of statements forming the block body.
     */
    public IBlock(int line, ArrayList<IStatement> statements) {
        super(line);
        this.statements = statements;
    }

    /**
     * {@inheritDoc}
     */
    public IBlock analyze(Context context) {
        // { ... } defines a new level of scope.
        this.context = new LocalContext(context);

        for (int i = 0; i < statements.size(); i++) {
            statements.set(i, (IStatement) statements.get(i).analyze(this.context));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        for (IStatement statement : statements) {
            statement.codegen(output);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("IBlock:" + line, e);
        if (context != null) {
            context.toJSON(e);
        }
        for (IStatement statement : statements) {
            statement.toJSON(e);
        }
    }
}
