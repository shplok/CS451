package iota;

/**
 * An enum of token kinds. Each entry in this enum represents the kind of a token along with its image (string
 * representation).
 */
enum TokenKind {
    /**
     * Reserved word, "boolean".
     */
    BOOLEAN("boolean"),

    /**
     * Reserved word, "else".
     */
    ELSE("else"),

    /**
     * Reserved word, "false".
     */
    FALSE("false"),

    /**
     * Reserved word, "if".
     */
    IF("if"),

    /**
     * Reserved word, "int".
     */
    INT("int"),

    /**
     * Reserved word, "return".
     */
    RETURN("return"),

    /**
     * Reserved word, "true".
     */
    TRUE("true"),

    /**
     * Reserved word, "void".
     */
    VOID("void"),

    /**
     * Reserved word, "while".
     */
    WHILE("while"),

    /**
     * Separator, ",".
     **/
    COMMA(","),

    /**
     * Separator, "{".
     **/
    LCURLY("{"),

    /**
     * Separator, "(".
     **/
    LPAREN("("),

    /**
     * Separator, "}".
     **/
    RCURLY("}"),

    /**
     * Separator, ")".
     **/
    RPAREN(")"),

    /**
     * Separator, ";".
     **/
    SEMI(";"),

    /**
     * Operator, "=".
     */
    ASSIGN("="),

    /**
     * Operator, "/".
     */
    DIV("/"),

    /**
     * Operator, "==".
     */
    EQUAL("=="),

    /**
     * Operator, ">=".
     */
    GE("GE"),

    /**
     * Operator, ">".
     */
    GT(">"),

    /**
     * Operator, "&amp;&amp;".
     */
    LAND("&&"),

    /**
     * Operator, "&lt;=".
     */
    LE("<="),

    /**
     * Operator, "&lt;".
     */
    LT("<"),

    /**
     * Operator, "!".
     */
    LNOT("!"),

    /**
     * Operator, "||".
     */
    LOR("||"),

    /**
     * Operator, "-".
     */
    MINUS("-"),

    /**
     * Operator, "!=".
     */
    NOT_EQUAL("!="),

    /**
     * Operator, "+".
     */
    PLUS("+"),

    /**
     * Operator, "%".
     */
    REM("%"),

    /**
     * Operator, "*".
     */
    STAR("*"),

    /**
     * Identifier.
     **/
    IDENTIFIER("<IDENTIFIER>"),

    /**
     * Integer literal.
     **/
    INT_LITERAL("<INT_LITERAL>"),

    /**
     * End of file character.
     */
    EOF("<end of file>");

    // The token kind's string representation.
    private final String image;

    /**
     * Constructs an instance of TokenKind given its string representation.
     *
     * @param image string representation of the token kind.
     */
    TokenKind(String image) {
        this.image = image;
    }

    /**
     * Returns the token kind's string representation.
     *
     * @return the token kind's string representation.
     */
    public String tokenRep() {
        if (this == EOF) {
            return "<EOF>";
        }
        if (image.startsWith("<") && image.endsWith(">")) {
            return image;
        }
        return "\"" + image + "\"";
    }

    /**
     * Returns the token kind's image.
     *
     * @return the token kind's image.
     */
    public String image() {
        return image;
    }
}

/**
 * A representation of tokens returned by the Scanner method getNextToken(). A token has a kind identifying what kind
 * of token it is, an image for providing any semantic text, and the line in which it occurred in the source file.
 */
class TokenInfo {
    // Token kind.
    private final TokenKind kind;

    // Semantic text (if any). For example, the identifier name when the token kind is IDENTIFIER. For tokens without
    // a semantic text, it is simply its string representation. For example, "+=" when the token kind is PLUS_ASSIGN.
    private final String image;

    // Line in which the token occurs in the source file.
    private final int line;

    /**
     * Constructs a TokenInfo object given its kind, the semantic text forming the token, and its line number.
     *
     * @param kind  the token's kind.
     * @param image the semantic text forming the token.
     * @param line  the line in which the token occurs in the source file.
     */
    public TokenInfo(TokenKind kind, String image, int line) {
        this.kind = kind;
        this.image = image;
        this.line = line;
    }

    /**
     * Constructs a TokenInfo object given its kind and its line number. Its image is simply the token kind's string
     * representation.
     *
     * @param kind the token's identifying number.
     * @param line the line in which the token occurs in the source file.
     */
    public TokenInfo(TokenKind kind, int line) {
        this(kind, kind.image(), line);
    }

    /**
     * Returns the token's kind.
     *
     * @return the token's kind.
     */
    public TokenKind kind() {
        return kind;
    }

    /**
     * Returns the line number associated with the token.
     *
     * @return the line number associated with the token.
     */
    public int line() {
        return line;
    }

    /**
     * Returns the token's string representation.
     *
     * @return the token's string representation.
     */
    public String tokenRep() {
        return kind.tokenRep();
    }

    /**
     * Returns the token's image.
     *
     * @return the token's image.
     */
    public String image() {
        return image;
    }
}
