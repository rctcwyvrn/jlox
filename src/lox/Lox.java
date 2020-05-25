package lox;

import lox.exception.LoxRuntimeException;
import lox.exception.ParseException;
import lox.execution.InterpreterVisitor;
import lox.parser.*;
import lox.semantic.Resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Lox {
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    private static boolean ignoreErrors = false;

    public static void main(String[] args) throws IOException {
        if(args.length >= 2){
            System.out.println("Usage: jlox [file]");
            System.exit(60);
        } else if(args.length == 1){
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void reset(){
        hadError = false;
        hadRuntimeError = false;
    }

    private static void run(String script) {
        List<Token> tokens = Lexer.parseTokens(script);
        if(hadError) return; // Stop if we have lexing errors

        Parser parser = new Parser(tokens);
        List<Stmt> program = parser.parse();
        if(hadError) return; // Stop if we have parsing errors

        Resolver resolver = new Resolver();
        Map<Expr, Integer> resolutions = resolver.performResolve(program);
        if(hadError) return; // Stop if we have resolution errors

        InterpreterVisitor interpreter = new InterpreterVisitor(resolutions);
        interpreter.interpret(program);
    }

    private static void runPrompt() throws IOException {
        ignoreErrors = true;

        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(in);
        InterpreterVisitor interpreter = new InterpreterVisitor();
        while(true){
            try {
                System.out.print("> ");
                String input = reader.readLine();

                List<Token> tokens = Lexer.parseTokens(input);
                if (hadError) continue;

                Parser parser = new Parser(tokens);
                ASTNode node = parser.parseREPL();
                if (node instanceof Expr) {
                    Object res = interpreter.evaluate((Expr) node);
                    System.out.println(res);
                } else {
                    interpreter.execute((Stmt) node);
                }
            } catch (Exception e) {
                if(e instanceof LoxRuntimeException){
                    Lox.runtimeError((LoxRuntimeException) e);
                } else if(e instanceof ParseException){
                    System.out.println("Exception while parsing statement, try again");
                } else {
                    e.printStackTrace();
                }
            } finally {
                reset();
            }
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] script = Files.readAllBytes(Paths.get(path));
        run(new String(script, Charset.defaultCharset()));

        if(hadError){
            System.exit(65);
        }

        if(hadRuntimeError){
            System.exit(70);
        }
    }

    public static void error(int line, String message){
        report(line, "", message);
    }

    public static void error(Token token, String message){
        if (token.getType() == TokenType.EOF) {
            report(token.getLine(), "at end of file", message);
        } else {
            report(token.getLine(), "at '" + token.getLexeme() + "'", message);
        }
    }

    private static void report(int line, String where, String message){
        if(!ignoreErrors) System.err.println("[line: " + line + "] | Error " + where + ": " + message + "\n");
        hadError = true;
    }

    public static void runtimeError(LoxRuntimeException error){
        System.err.println("Lox runtime exception: " + error.getMessage() +
                "\n\tat [line " + error.getToken().getLine() + "]\n");
        hadRuntimeError = true;
    }
}
