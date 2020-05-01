package lox;

import lox.parser.Lexer;
import lox.parser.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static boolean hadError = false;

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

    private static void run(String script) {
        List<Token> tokens = Lexer.parseTokens(script);
        for(Token token: tokens){
            System.out.println(token);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(in);

        while(true){
            System.out.print("> ");
            run(reader.readLine());
            hadError = false;
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] script = Files.readAllBytes(Paths.get(path));
        run(new String(script, Charset.defaultCharset()));

        if(hadError){
            System.exit(1);
        }
    }

    public static void error(int line, String message){
        report(line, "", message);
    }

    private static void report(int line, String where, String message){
        System.err.println("[line: " + line + "] | Error " + where + ": " + message);
        hadError = true;
    }
}
