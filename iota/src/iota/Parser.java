package iota;

import java.util.ArrayList;

import static iota.TokenKind.*;

/**
 * A recursive descent parser that, given a lexical analyzer (a LookaheadScanner), parses an iota compilation unit
 * (program file), taking tokens from the LookaheadScanner, and produces an abstract syntax tree (AST) for it.
 */
class Parser {
    // The lexical analyzer with which tokens are scanned.
    private final LookaheadScanner scanner;

    // Whether a parser error has been found.
    private boolean isInError;

    // Whether we have recovered from a parser error.
    private boolean isRecovered;

    /**
     * Constructs a parser from the given lexical analyzer.
     *
     * @param scanner the lexical analyzer with which tokens are scanned.
     */
    public Parser(LookaheadScanner scanner) {
        this.scanner = scanner;
        isInError = false;
        isRecovered = true;

        // Prime the pump.
        scanner.next();
    }

    /**
     * Returns true if a parser error has occurred up to now, and false otherwise.
     *
     * @return true if a parser error has occurred up to now, and false otherwise.
     */
    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * Parses a compilation unit (a program file) and returns an AST for it.
     *
     * <pre>
     *     compilationUnit ::= { methodDeclaration } EOF
     * </pre>
     *
     * @return an AST for a compilation unit.
     */
    public ICompilationUnit compilationUnit() {
        int line = scanner.token().line();
        String fileName = scanner.fileName();
        ArrayList<IMethodDeclaration> methodDeclarations = new ArrayList<>();
        while (!see(EOF)) {
            IMethodDeclaration methodDeclaration = methodDeclaration();
            methodDeclarations.add(methodDeclaration);
        }
        mustBe(EOF);
        return new ICompilationUnit(fileName, line, methodDeclarations);
    }

    /**
     * Parses a method declaration and returns an AST for it.
     *
     * <pre>
     *     methodDeclaration ::= ( VOID | type ) IDENTIFIER formalParameters block
     * </pre>
     *
     * @return an AST for a method declaration.
     */
    private IMethodDeclaration methodDeclaration() {
        int line = scanner.token().line();
        Type type = have(VOID) ? Type.VOID : type();
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        ArrayList<IFormalParameter> params = formalParameters();
        IBlock body = block();
        return new IMethodDeclaration(line, name, type, params, body);
    }

    /**
     * Parses and returns a list of formal parameters.
     *
     * <pre>
     *   formalParameters ::= LPAREN [ formalParameter { COMMA formalParameter } ] RPAREN
     * </pre>
     *
     * @return a list of formal parameters.
     */
    private ArrayList<IFormalParameter> formalParameters() {
        ArrayList<IFormalParameter> parameters = new ArrayList<>();
        mustBe(LPAREN);
        if (have(RPAREN)) {
            return parameters;
        }
        do {
            parameters.add(formalParameter());
        } while (have(COMMA));
        mustBe(RPAREN);
        return parameters;
    }

    /**
     * Parses a formal parameter and returns an AST for it.
     *
     * <pre>
     *   formalParameter ::= type IDENTIFIER
     * </pre>
     *
     * @return an AST for a formal parameter.
     */
    private IFormalParameter formalParameter() {
        int line = scanner.token().line();
        Type type = type();
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        return new IFormalParameter(line, name, type);
    }

    /**
     * Parses a block and returns an AST for it.
     *
     * <pre>
     *   block ::= LCURLY { statement } RCURLY
     * </pre>
     *
     * @return an AST for a block.
     */
    private IBlock block() {
        int line = scanner.token().line();
        ArrayList<IStatement> statements = new ArrayList<>();
        mustBe(LCURLY);
        while (!see(RCURLY) && !see(EOF)) {
            statements.add(statement());
        }
        mustBe(RCURLY);
        return new IBlock(line, statements);
    }

    /**
     * Parses a statement and returns an AST for it.
     *
     * <pre>
     *   statement ::= block
     *               | type IDENTIFIER [ ASSIGN expression ] SEMI
     *               | IF parExpression statement [ ELSE statement ]
     *               | RETURN [ expression ] SEMI
     *               | WHILE parExpression statement
     *               | statementExpression SEMI
     * </pre>
     *
     * @return an AST for a statement.
     */
    private IStatement statement() {
        int line = scanner.token().line();
        if (see(LCURLY)) {
            return block();
        } else if (see(BOOLEAN) || see(INT)) {
            Type type = type();
            mustBe(IDENTIFIER);
            String name = scanner.previousToken().image();
            IExpression initial = have(ASSIGN) ? expression() : null;
            mustBe(SEMI);
            return new IVariableDeclaration(line, name, type, initial);
        } else if (have(IF)) {
            IExpression test = parExpression();
            IStatement consequent = statement();
            IStatement alternate = have(ELSE) ? statement() : null;
            return new IIfStatement(line, test, consequent, alternate);
        } else if (have(RETURN)) {
            if (have(SEMI)) {
                return new IReturnStatement(line, null);
            } else {
                IExpression expr = expression();
                mustBe(SEMI);
                return new IReturnStatement(line, expr);
            }
        } else if (have(WHILE)) {
            IExpression test = parExpression();
            IStatement statement = statement();
            return new IWhileStatement(line, test, statement);
        } else {
            // Must be a statementExpression.
            IStatement statement = statementExpression();
            mustBe(SEMI);
            return statement;
        }
    }

    /**
     * Parses a parenthesized expression and returns an AST for it.
     *
     * <pre>
     *   parExpression ::= LPAREN expression RPAREN
     * </pre>
     *
     * @return an AST for a parenthesized expression.
     */
    private IExpression parExpression() {
        mustBe(LPAREN);
        IExpression expr = expression();
        mustBe(RPAREN);
        return expr;
    }

    /**
     * Parses and returns a type.
     *
     * <pre>
     *   type ::= BOOLEAN | INT
     * </pre>
     *
     * @return a basic type.
     */
    private Type type() {
        if (have(BOOLEAN)) {
            return Type.BOOLEAN;
        } else if (have(INT)) {
            return Type.INT;
        } else {
            reportParserError("type sought where %s found", scanner.token().image());
            return Type.ANY;
        }
    }

    /**
     * Parses a statement expression and returns an AST for it.
     *
     * <pre>
     *   statementExpression ::= expression
     * </pre>
     *
     * @return an AST for a statement expression.
     */
    private IStatement statementExpression() {
        int line = scanner.token().line();
        IExpression expr = expression();
        if (expr instanceof IAssignOp || expr instanceof IMessageExpression) {
            // So as not to save on stack.
            expr.isStatementExpression = true;
        } else {
            reportParserError("invalid statement expression; it does not have a side-effect");
        }
        return new IStatementExpression(line, expr);
    }

    /**
     * Parses an expression and returns an AST for it.
     *
     * <pre>
     *   expression ::= assignmentExpression
     * </pre>
     *
     * @return an AST for an expression.
     */
    private IExpression expression() {
        return assignmentExpression();
    }

    /**
     * Parses an assignment expression and returns an AST for it.
     *
     * <pre>
     *   assignmentExpression ::= conditionalOrExpression [ ASSIGN assignmentExpression ]
     * </pre>
     *
     * @return an AST for an assignment expression.
     */
    private IExpression assignmentExpression() {
        int line = scanner.token().line();
        IExpression lhs = conditionalOrExpression();
        if (have(ASSIGN)) {
            return new IAssignOp(line, lhs, assignmentExpression());
        } else {
            return lhs;
        }
    }

    /**
     * Parses a conditional-or expression and returns an AST for it.
     *
     * <pre>
     *   conditionalOrExpression ::= conditionalAndExpression { LOR conditionalAndExpression }
     * </pre>
     *
     * @return an AST for a conditional-and expression.
     */
    private IExpression conditionalOrExpression() {
        int line = scanner.token().line();
        boolean more = true;
        IExpression lhs = conditionalAndExpression();
        while (more) {
            if (have(LOR)) {
                lhs = new ILogicalOrOp(line, lhs, conditionalAndExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses a conditional-and expression and returns an AST for it.
     *
     * <pre>
     *   conditionalAndExpression ::= equalityExpression { LAND equalityExpression }
     * </pre>
     *
     * @return an AST for a conditional-and expression.
     */
    private IExpression conditionalAndExpression() {
        int line = scanner.token().line();
        boolean more = true;
        IExpression lhs = equalityExpression();
        while (more) {
            if (have(LAND)) {
                lhs = new ILogicalAndOp(line, lhs, equalityExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses an equality expression and returns an AST for it.
     *
     * <pre>
     *   equalityExpression ::= relationalExpression { ( EQUAL | NOT_EQUAL ) relationalExpression }
     * </pre>
     *
     * @return an AST for an equality expression.
     */
    private IExpression equalityExpression() {
        int line = scanner.token().line();
        boolean more = true;
        IExpression lhs = relationalExpression();
        while (more) {
            if (have(EQUAL)) {
                lhs = new IEqualOp(line, lhs, relationalExpression());
            } else if (have(NOT_EQUAL)) {
                lhs = new INotEqualOp(line, lhs, relationalExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses a relational expression and returns an AST for it.
     *
     * <pre>
     *   relationalExpression ::= additiveExpression [ ( GE | GT | LE | LT ) additiveExpression ]
     * </pre>
     *
     * @return an AST for a relational expression.
     */
    private IExpression relationalExpression() {
        int line = scanner.token().line();
        IExpression lhs = additiveExpression();
        if (have(GE)) {
            return new IGreaterEqualOp(line, lhs, additiveExpression());
        } else if (have(GT)) {
            return new IGreaterThanOp(line, lhs, additiveExpression());
        } else if (have(LE)) {
            return new ILessEqualOp(line, lhs, additiveExpression());
        } else if (have(LT)) {
            return new ILessThanOp(line, lhs, additiveExpression());
        } else {
            return lhs;
        }
    }

    /**
     * Parses an additive expression and returns an AST for it.
     *
     * <pre>
     *   additiveExpression ::= multiplicativeExpression { ( MINUS | PLUS ) multiplicativeExpression }
     * </pre>
     *
     * @return an AST for an additive expression.
     */
    private IExpression additiveExpression() {
        int line = scanner.token().line();
        boolean more = true;
        IExpression lhs = multiplicativeExpression();
        while (more) {
            if (have(MINUS)) {
                lhs = new ISubtractOp(line, lhs, multiplicativeExpression());
            } else if (have(PLUS)) {
                lhs = new IPlusOp(line, lhs, multiplicativeExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses a multiplicative expression and returns an AST for it.
     *
     * <pre>
     *   multiplicativeExpression ::= unaryExpression { ( DIV | REM | STAR ) unaryExpression }
     * </pre>
     *
     * @return an AST for a multiplicative expression.
     */
    private IExpression multiplicativeExpression() {
        int line = scanner.token().line();
        boolean more = true;
        IExpression lhs = unaryExpression();
        while (more) {
            if (have(DIV)) {
                lhs = new IDivideOp(line, lhs, unaryExpression());
            } else if (have(REM)) {
                lhs = new IRemainderOp(line, lhs, unaryExpression());
            } else if (have(STAR)) {
                lhs = new IMultiplyOp(line, lhs, unaryExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * /**
     * Parses a unary expression and returns an AST for it.
     *
     * <pre>
     *   unaryExpression ::= ( LNOT | MINUS ) unaryExpression
     *                   | parExpression
     *                   | IDENTIFIER [ arguments ]
     *                   | literal
     * </pre>
     *
     * @return an AST for a unary expression.
     */
    private IExpression unaryExpression() {
        int line = scanner.token().line();
        if (have(LNOT)) {
            return new ILogicalNotOp(line, unaryExpression());
        } else if (see(LPAREN)) {
            return parExpression();
        } else if (have(MINUS)) {
            return new INegateOp(line, unaryExpression());
        } else if (have(IDENTIFIER)) {
            String name = scanner.previousToken().image();
            if (see(LPAREN)) {
                ArrayList<IExpression> args = arguments();
                return new IMessageExpression(line, name, args);
            } else {
                return new IVariable(line, name);
            }
        } else {
            return literal();
        }
    }

    /**
     * Parses and returns a list of arguments.
     *
     * <pre>
     *   arguments ::= LPAREN [ expression { COMMA expression } ] RPAREN
     * </pre>
     *
     * @return a list of arguments.
     */
    private ArrayList<IExpression> arguments() {
        ArrayList<IExpression> args = new ArrayList<>();
        mustBe(LPAREN);
        if (have(RPAREN)) {
            return args;
        }
        do {
            args.add(expression());
        } while (have(COMMA));
        mustBe(RPAREN);
        return args;
    }

    /**
     * Parses a literal and returns an AST for it.
     *
     * <pre>
     *   literal ::= BOOLEAN_LITERAL | INT_LITERAL
     * </pre>
     *
     * @return an AST for a literal.
     */
    private IExpression literal() {
        int line = scanner.token().line();
        if (have(FALSE) || have(TRUE)) {
            return new ILiteralBoolean(line, scanner.previousToken().image());
        } else if (have(INT_LITERAL)) {
            return new ILiteralInt(line, scanner.previousToken().image());
        } else {
            reportParserError("literal sought where %s found", scanner.token().image());
            return new IWildExpression(line);
        }
    }

    //////////////////////////////////////////////////
    // Parsing Support
    // ////////////////////////////////////////////////

    // Returns true if the current token equals sought, and false otherwise.
    private boolean see(TokenKind sought) {
        return (sought == scanner.token().kind());
    }

    // If the current token equals sought, scans it and returns true. Otherwise, returns false without scanning the
    // token.
    private boolean have(TokenKind sought) {
        if (see(sought)) {
            scanner.next();
            return true;
        } else {
            return false;
        }
    }

    // Attempts to match a token we're looking for with the current input token. On success, scans the token and goes
    // into a "Recovered" state. On failure, what happens next depends on whether or not the parser is currently in a
    // "Recovered" state: if so, it reports the error and goes into an "Unrecovered" state; if not, it repeatedly
    // scans tokens until it finds the one it is looking for (or EOF) and then returns to a "Recovered" state. This
    // gives us a kind of poor man's syntactic error recovery, a strategy due to David Turner and Ron Morrison.
    private void mustBe(TokenKind sought) {
        if (scanner.token().kind() == sought) {
            scanner.next();
            isRecovered = true;
        } else if (isRecovered) {
            isRecovered = false;
            reportParserError("%s found where %s sought", scanner.token().image(), sought.image());
        } else {
            // Do not report the (possibly spurious) error, but rather attempt to recover by forcing a match.
            while (!see(sought) && !see(EOF)) {
                scanner.next();
            }
            if (see(sought)) {
                scanner.next();
                isRecovered = true;
            }
        }
    }

    // Reports a syntax error.
    private void reportParserError(String message, Object... args) {
        isInError = true;
        isRecovered = false;
        System.err.printf("%s:%d: error: ", scanner.fileName(), scanner.token().line());
        System.err.printf(message, args);
        System.err.println();
    }
}
