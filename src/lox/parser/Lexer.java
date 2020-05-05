package lox.parser;

import lox.Lox;

import java.util.*;

import static lox.parser.TokenType.*;

/**
 *  Scanner/Lexer class
 *
 *  I thought it would be a good idea to make a (mostly) stateless lexer (unlike the one in the book) with just an iterator
 *  I think it turned out to be more messy than what it was worth
 */
public class Lexer {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }


    public static List<Token> parseTokens(String script) {
        CharacterIterator it = new CharacterIterator(script);
        List<Token> tokens = new ArrayList<>();
        while (it.hasNext()){
            parseNextToken(tokens, it);
        }
        tokens.add(new Token(EOF, "", null, it.line()));
        return tokens;
    }

    private static void parseNextToken(List<Token> tokens, CharacterIterator it) {
        int start = it.index();
        char c = it.next();
        switch (c) {
            case '(': addToken(tokens, LEFT_PAREN, start, it); break;
            case ')': addToken(tokens, RIGHT_PAREN,  start, it); break;
            case '{': addToken(tokens, LEFT_BRACE,  start, it); break;
            case '}': addToken(tokens, RIGHT_BRACE,  start, it); break;
            case ',': addToken(tokens, COMMA,  start, it); break;
            case '.': addToken(tokens, DOT,  start, it); break;
            case '-': addToken(tokens, MINUS,  start, it); break;
            case '+': addToken(tokens, PLUS,  start, it); break;
            case ';': addToken(tokens, SEMICOLON,  start, it); break;
            case '*': addToken(tokens, STAR,  start, it); break;
            case '!': addToken(tokens, matchNext(it, '=') ? BANG_EQUAL : BANG,  start, it); break;
            case '=': addToken(tokens, matchNext(it, '=') ? EQUAL_EQUAL : EQUAL,  start, it); break;
            case '<': addToken(tokens, matchNext(it, '=') ? LESS_EQUAL : LESS,  start, it); break;
            case '>': addToken(tokens, matchNext(it, '=') ? GREATER_EQUAL : GREATER,  start, it); break;
            case '/': // special handling because it comments out the entire line if we have a double slash
                if(matchNext(it,'/')) {
                    while (it.hasNext() && it.next() != '\n') ;
                    // no prev because we want to consume the newline char as well
                } else if(matchNext(it, '*')){
                    // C-style multiline comments
                    parseMultilineComment(it);
                }else{
                    addToken(tokens, SLASH, start, it);
                }
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                // Ignore whitespace.
                break;
            case '"': parseString(tokens, it); break;
            default:
                if(isDigit(c)) {
                    parseNumber(tokens, it);
                } else if (isAlpha(c)) {
                    parseIdentifier(tokens, it);
                } else {
                    Lox.error(it.line(), "Unexpected character: " + c);
                }
                break;
        }
    }

    private static void parseMultilineComment(CharacterIterator it) {
        while(it.hasNext()){
            if(it.next() == '*'){
                if(it.peek() == '/'){
                    it.next();
                    return;
                }
            }
        }
        Lox.error(it.line(), "Unterminated multiline comment");
        return;
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static void parseIdentifier(List<Token> tokens, CharacterIterator it) {
        int start = it.index() - 1; // -1 to get the first letter
        while(it.hasNext() && isAlphaNumeric(it.peek())) it.next();
        String identifier = it.getFrom(start);
        if(keywords.keySet().contains(identifier)){
            addToken(tokens, keywords.get(identifier), start, it);
        }else {
            addToken(tokens, IDENTIFIER, identifier, start, it);
        }
    }

    private static void parseNumber(List<Token> tokens, CharacterIterator it) {
        int start = it.index() - 1; // -1 to get the first digit
        while(it.hasNext() && isDigit(it.peek())) it.next();

        // See if it's a decimal
        if(it.hasNext() && it.peek() == '.'){
            it.next();
            if(it.hasNext() && isDigit(it.peek())){
                while(it.hasNext() && isDigit(it.peek())) it.next();
            }
        }

        addToken(tokens, NUMBER, Double.parseDouble(it.getFrom(start)), start, it);
    }

    private static boolean isDigit(char c) {
        return c>= '0' && c<='9';
    }

    private static void parseString(List<Token> tokens, CharacterIterator it) {
        int start = it.index() - 1; // -1 to get the first "
        int startLine = it.line();
        boolean ended = false;
        // Note: this means we support multi-line strings
        while(it.hasNext()){
            char c = it.next();
            if(c == '"') {
                ended = true;
                break;
            }
        }

        if(!ended){
            Lox.error(it.line(), "Unterminated string starting from line " + startLine);
            return;
        }

        String value = it.getFrom(start);
        addToken(tokens, STRING,
                value.substring(1, value.length() - 1), // trim the surrounding quotes
                start, it);
    }

    private static void addToken(List<Token> tokens, TokenType type, int start, CharacterIterator it){
        addToken(tokens, type, null, start, it);
    }

    private static void addToken(List<Token> tokens, TokenType type, Object lit, int start, CharacterIterator it){
        addToken(tokens, type, lit, it.getFrom(start), it.line());
    }

    private static void addToken(List<Token> tokens, TokenType type, Object lit, String text, int line){
        tokens.add(new Token(type, text, lit, line));
    }

    private static boolean matchNext(CharacterIterator it, char expected){
        if(!it.hasNext()) return false;
        if(it.next() != expected){
            it.prev(); // Unconsume if we were wrong
            return false;
        }
        return true;
    }
}

class CharacterIterator implements Iterator<Character> {

    private final String str;
    private int pos = 0;
    private int line = 1;

    public CharacterIterator(String str) {
        this.str = str;
    }

    public boolean hasNext() {
        return pos < str.length();
    }

    public Character next() {
        char c =  str.charAt(pos++);
        if(c == '\n') line++;
        return c;
    }

    public void prev(){
        pos--;
    }

    public int index(){
        return pos;
    }

    public int line(){
        return line;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public String getFrom(int start) {
        return str.substring(start, pos);
    }

    public char peek() {
        char c =  str.charAt(pos);
        return c;
    }
}
