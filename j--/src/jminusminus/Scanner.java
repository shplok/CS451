package jminusminus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Hashtable;

// import org.javacc.jjtree.Token;

// import org.javacc.jjtree.Token;

import static jminusminus.TokenKind.*;

/**
 * A lexical analyzer for j--, that has no backtracking mechanism.
 */
class Scanner {
    /**
     * End of file character.
     */
    public final static char EOFCH = CharReader.EOFCH;

    // Keywords in j--.
    private final Hashtable<String, TokenKind> reserved;

    // Source characters.
    private final CharReader input;

    // Next unscanned character.
    private char ch;

    // Whether a scanner error has been found.
    private boolean isInError;

    // Source file name.
    private final String fileName;

    // Line number of current token.
    private int line;

    /**
     * Constructs a Scanner from a file name.
     *
     * @param fileName name of the source file.
     * @throws FileNotFoundException when the named file cannot be found.
     */
    public Scanner(String fileName) throws FileNotFoundException {
        this.input = new CharReader(fileName);
        this.fileName = fileName;
        isInError = false;

        // Keywords in j--
        reserved = new Hashtable<>();
        reserved.put(ABSTRACT.image(), ABSTRACT);
        reserved.put(BOOLEAN.image(), BOOLEAN);
        reserved.put(CHAR.image(), CHAR);
        reserved.put(CLASS.image(), CLASS);
        reserved.put(ELSE.image(), ELSE);
        reserved.put(EXTENDS.image(), EXTENDS);
        reserved.put(FALSE.image(), FALSE);
        reserved.put(IF.image(), IF);
        reserved.put(IMPORT.image(), IMPORT);
        reserved.put(INSTANCEOF.image(), INSTANCEOF);
        reserved.put(INT.image(), INT);
        reserved.put(NEW.image(), NEW);
        reserved.put(NULL.image(), NULL);
        reserved.put(PACKAGE.image(), PACKAGE);
        reserved.put(PRIVATE.image(), PRIVATE);
        reserved.put(PROTECTED.image(), PROTECTED);
        reserved.put(PUBLIC.image(), PUBLIC);
        reserved.put(RETURN.image(), RETURN);
        reserved.put(STATIC.image(), STATIC);
        reserved.put(SUPER.image(), SUPER);
        reserved.put(THIS.image(), THIS);
        reserved.put(TRUE.image(), TRUE);
        reserved.put(VOID.image(), VOID);
        reserved.put(WHILE.image(), WHILE);
        reserved.put(DO.image(), DO);               // include do in scanner
        reserved.put(BREAK.image(), BREAK);         // include break in scanner
        reserved.put(CASE.image(), CASE);           // include case in scanner
        reserved.put(CONTINUE.image(), CONTINUE);   // include continue in scanner
        reserved.put(DEFAULT.image(), DEFAULT);     // include default in scanner
        reserved.put(DOUBLE.image(), DOUBLE);       // include double in scanner
        reserved.put(FOR.image(), FOR);             // include for in scanner
        reserved.put(LONG.image(), LONG);           // include long in scanner
        reserved.put(SWITCH.image(), SWITCH);       // include switch in scanner

        // Prime the pump.
        nextCh();
    }

    /**
     * Scans and returns the next token from input.
     *
     * @return the next scanned token.
     */
    public TokenInfo getNextToken() {
        StringBuilder buffer;
        boolean moreWhiteSpace = true;
        while (moreWhiteSpace) {
            while (isWhitespace(ch)) {
                nextCh();
            }
            if (ch == '/') {
                nextCh();
                if (ch == '/') {
                    // CharReader maps all new lines to '\n'.
                    while (ch != '\n' && ch != EOFCH) {
                        nextCh();
                    }
                } else if (ch == '*') {
                    // skip the values within the comment.
                    nextCh();
                    while(true) {
                        if (ch == EOFCH) {
                            reportScannerError("Comment is incomplete.");
                            break;
                        }
                        if (ch == '*') {
                            nextCh();
                            if (ch == '/') {
                                // comment has been finished.
                                nextCh();
                                break;
                            } 
                        } else {
                            nextCh();
                        }
                    }
                } else if (ch == '=') {
                    // support for /=
                    nextCh();
                    return new TokenInfo(DIV_ASSIGN, line);
                } else {
                    return new TokenInfo(DIV, line);
                }
            } else {
                moreWhiteSpace = false;
            }
        }
        switch (ch) {
            case EOFCH:
                return new TokenInfo(EOF, line);

            // ------------------- NEW CASES ------------------------ //
        
            case '%':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(REM_ASSIGN, line);
                } else {
                    return new TokenInfo(REM, line);
                }
            case '?':
                nextCh();
                return new TokenInfo(QUESTION, line);
            case ':':
                nextCh();
                return new TokenInfo(COLON, line);

            // ----------------------------------------------------- //
            case ',':
                nextCh();
                return new TokenInfo(COMMA, line);
            case '.':
                buffer = new StringBuilder();
                buffer.append(ch);
                nextCh();
                
                // If followed by digits, it's a double literal (part 2)
                if (isDigit(ch)) {
                    buffer.append(digits());
                    
                    // Handle exponent
                    if (ch == 'e' || ch == 'E') {
                        buffer.append(exponent());
                    }
                    
                    // Handle d/D suffix
                    if (ch == 'd' || ch == 'D') {
                        buffer.append(ch);
                        nextCh();
                    }
                    
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                } else {
                    // It's just a dot operator
                    return new TokenInfo(DOT, line);
                }
            case '[':
                nextCh();
                return new TokenInfo(LBRACK, line);
            case '{':
                nextCh();
                return new TokenInfo(LCURLY, line);
            case '(':
                nextCh();
                return new TokenInfo(LPAREN, line);
            case ']':
                nextCh();
                return new TokenInfo(RBRACK, line);
            case '}':
                nextCh();
                return new TokenInfo(RCURLY, line);
            case ')':
                nextCh();
                return new TokenInfo(RPAREN, line);
            case ';':
                nextCh();
                return new TokenInfo(SEMI, line);
            case '-':
                nextCh();
                if (ch == '-') {
                    nextCh();
                    return new TokenInfo(DEC, line);
                } else if (ch == '=') {
                    // support for -=:
                    nextCh();
                    return new TokenInfo(MINUS_ASSIGN, line);
                } else {
                    return new TokenInfo(MINUS, line);
                }
            case '+':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(PLUS_ASSIGN, line);
                } else if (ch == '+') {
                    nextCh();
                    return new TokenInfo(INC, line);
                } else {
                    return new TokenInfo(PLUS, line);
                }
            case '*':
                nextCh();
                if (ch == '=') {
                    // support for *=
                    nextCh();
                    return new TokenInfo(STAR_ASSIGN, line);
                } else {
                    return new TokenInfo(STAR, line);
                }  
            case '=':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(EQUAL, line);
                } else {
                    return new TokenInfo(ASSIGN, line);
                }
            case '>':
                nextCh();
                if (ch == '=') {
                    // support for >=
                    nextCh();
                    return new TokenInfo(GE, line);
                } else {
                    return new TokenInfo(GT, line);
                }                
            case '<':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(LE, line);
                } else {
                    // support for <
                    return new TokenInfo(LT, line);
                }
            case '!':
                nextCh();
                if (ch == '=') {
                    // support for !=
                    nextCh();
                    return new TokenInfo(NOT_EQUAL, line);
                } else {
                    return new TokenInfo(LNOT, line);
                }       
            case '&':
                nextCh();
                if (ch == '&') {
                    nextCh();
                    return new TokenInfo(LAND, line);
                } else {
                    reportScannerError("operator & is not supported in j--");
                    return getNextToken();
                }
            case '|': // support for || and not |
                nextCh();
                if (ch == '|') {
                    nextCh();
                    return new TokenInfo(LOR, line);
                } else {
                    reportScannerError("operator | is not supported in our version of j-- :)");
                    return getNextToken();
                }
            case '\'':
                buffer = new StringBuilder();
                buffer.append('\'');
                nextCh();
                if (ch == '\\') {
                    nextCh();
                    buffer.append(escape());
                } else {
                    buffer.append(ch);
                    nextCh();
                }
                if (ch == '\'') {
                    buffer.append('\'');
                    nextCh();
                    return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
                } else {
                    // Expected a '; report error and try to recover.
                    reportScannerError(ch + " found by scanner where closing ' was expected");
                    while (ch != '\'' && ch != ';' && ch != '\n') {
                        nextCh();
                    }
                    return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
                }
            case '"':
                buffer = new StringBuilder();
                buffer.append("\"");
                nextCh();
                while (ch != '"' && ch != '\n' && ch != EOFCH) {
                    if (ch == '\\') {
                        nextCh();
                        buffer.append(escape());
                    } else {
                        buffer.append(ch);
                        nextCh();
                    }
                }
                if (ch == '\n') {
                    reportScannerError("unexpected end of line found in string");
                } else if (ch == EOFCH) {
                    reportScannerError("unexpected end of file found in string");
                } else {
                    // Scan the closing ".
                    nextCh();
                    buffer.append("\"");
                }
                return new TokenInfo(STRING_LITERAL, buffer.toString(), line);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                buffer = new StringBuilder();
                
                // Get all digits before potential decimal point
                buffer.append(digits());
                
                // Handle long literal with L/l suffix
                if (ch == 'l' || ch == 'L') {
                    buffer.append(ch);
                    nextCh();
                    return new TokenInfo(LONG_LITERAL, buffer.toString(), line);
                }
                
                // Handle decimal point (double literal part 1)
                if (ch == '.') {
                    buffer.append(ch);
                    nextCh();
                    
                    // Get digits after decimal point (if any)
                    if (isDigit(ch)) {
                        buffer.append(digits());
                    }
                    
                    // Handle exponent
                    if (ch == 'e' || ch == 'E') {
                        buffer.append(exponent());
                    }
                    
                    // Handle d/D suffix
                    if (ch == 'd' || ch == 'D') {
                        buffer.append(ch);
                        nextCh();
                    }
                    
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                }
                
                // Handle exponent (double literal part 3)
                if (ch == 'e' || ch == 'E') {
                    buffer.append(exponent());
                    
                    // Optional d/D suffix
                    if (ch == 'd' || ch == 'D') {
                        buffer.append(ch);
                        nextCh();
                    }
                    
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                }
                
                // Handle d/D suffix (double literal part 4)
                if (ch == 'd' || ch == 'D') {
                    buffer.append(ch);
                    nextCh();
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                }
                
                // If none of the above, it's a regular int literal
                return new TokenInfo(INT_LITERAL, buffer.toString(), line);
            default:
                if (isIdentifierStart(ch)) {
                    buffer = new StringBuilder();
                    while (isIdentifierPart(ch)) {
                        buffer.append(ch);
                        nextCh();
                    }
                    String identifier = buffer.toString();
                    if (reserved.containsKey(identifier)) {
                        return new TokenInfo(reserved.get(identifier), line);
                    } else {
                        return new TokenInfo(IDENTIFIER, identifier, line);
                    }
                } else {
                    reportScannerError("unidentified input token '%c'", ch);
                    nextCh();
                    return getNextToken();
                }
        }
    }

    /**
     * Scans and returns a string of digits starting at the current character,
     * which must be a digit.
     *
     * @return a string of digits.
     */
    private String digits() {
        StringBuilder buffer = new StringBuilder();
        while (isDigit(ch)) {
            buffer.append(ch);
            nextCh();
        }
        return buffer.toString();
    }

    /**
     * Scans and returns an exponent (e.g., "e+10", "E-5") starting at the current 
     * character, which must be 'e' or 'E'.
     *
     * @return a string representation of the exponent.
     */
    private String exponent() {
        StringBuilder buffer = new StringBuilder();
        
        // Append 'e' or 'E'
        buffer.append(ch);
        nextCh();
        
        // Optional sign
        if (ch == '+' || ch == '-') {
            buffer.append(ch);
            nextCh();
        }
        
        // Must have at least one digit after e/E
        if (!isDigit(ch)) {
            reportScannerError("Invalid exponent - must have at least one digit");
            return buffer.toString();
        }
        
        // Read all digits
        buffer.append(digits());
        
        return buffer.toString();
    }

    /**
     * Returns true if an error has occurred, and false otherwise.
     *
     * @return true if an error has occurred, and false otherwise.
     */
    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * Returns the name of the source file.
     *
     * @return the name of the source file.
     */
    public String fileName() {
        return fileName;
    }

    // Scans and returns an escaped character.
    private String escape() {
        switch (ch) {
            case 'b':
                nextCh();
                return "\\b";
            case 't':
                nextCh();
                return "\\t";
            case 'n':
                nextCh();
                return "\\n";
            case 'f':
                nextCh();
                return "\\f";
            case 'r':
                nextCh();
                return "\\r";
            case '"':
                nextCh();
                return "\\\"";
            case '\'':
                nextCh();
                return "\\'";
            case '\\':
                nextCh();
                return "\\\\";
            default:
                reportScannerError("Badly formed escape: \\%c", ch);
                nextCh();
                return "";
        }
    }

    // Advances ch to the next character from input, and updates the line number.
    private void nextCh() {
        line = input.line();
        try {
            ch = input.nextChar();
        } catch (Exception e) {
            reportScannerError("unable to read characters from input");
        }
    }

    // Reports a lexical error and records the fact that an error has occurred. This fact can be ascertained from the
    // Scanner by sending it an errorHasOccurred message.
    private void reportScannerError(String message, Object... args) {
        isInError = true;
        System.err.printf("%s:%d: error: ", fileName, line);
        System.err.printf(message, args);
        System.err.println();
    }

    // Returns true if the specified character is a digit (0-9), and false otherwise.
    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    // Returns true if the specified character is a whitespace, and false otherwise.
    private boolean isWhitespace(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\f');
    }

    // Returns true if the specified character can start an identifier name, and false otherwise.
    private boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '$');
    }

    // Returns true if the specified character can be part of an identifier name, and false otherwise.
    private boolean isIdentifierPart(char c) {
        return (isIdentifierStart(c) || isDigit(c));
    }
}

/**
 * A buffered character reader, which abstracts out differences between platforms, mapping all new lines to '\n', and
 * also keeps track of line numbers.
 */
class CharReader {
    /**
     * Representation of the end of file as a character.
     */
    public final static char EOFCH = (char) -1;

    // The underlying reader records line numbers.
    private final LineNumberReader lineNumberReader;

    // Name of the file that is being read.
    private final String fileName;

    /**
     * Constructs a CharReader from a file name.
     *
     * @param fileName the name of the input file.
     * @throws FileNotFoundException if the file is not found.
     */
    public CharReader(String fileName) throws FileNotFoundException {
        lineNumberReader = new LineNumberReader(new FileReader(fileName));
        this.fileName = fileName;
    }

    /**
     * Scans and returns the next character.
     *
     * @return the character scanned.
     * @throws IOException if an I/O error occurs.
     */
    public char nextChar() throws IOException {
        return (char) lineNumberReader.read();
    }

    /**
     * Returns the current line number in the source file.
     *
     * @return the current line number in the source file.
     */
    public int line() {
        return lineNumberReader.getLineNumber() + 1; // LineNumberReader counts lines from 0
    }

    /**
     * Returns the file name.
     *
     * @return the file name.
     */
    public String fileName() {
        return fileName;
    }

    /**
     * Closes the file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        lineNumberReader.close();
    }
}