package lox;

import lox.exception.LoxRuntimeException;
import lox.execution.InterpreterVisitor;
import lox.parser.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    private static final InterpreterVisitor interpreter = new InterpreterVisitor();

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
        if(hadError) return;

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();
        if(hadError) return;
        interpreter.interpret(expression);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(in);

        while(true){
            System.out.print("> ");
            run(reader.readLine());
            reset();
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
            report(token.getLine(), " at end of file", message);
        } else {
            report(token.getLine(), " at '" + token.getLexeme() + "'", message);
        }
    }

    public static void runtimeError(LoxRuntimeException error){
        System.err.println(error.getMessage() +
                "\n\tat [line " + error.getToken().getLine() + "]\n");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message){
        System.err.println("[line: " + line + "] | Error " + where + ": " + message + "\n");
        hadError = true;
    }
}
