package src.redact;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Redaction_Main {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();


    public static void main(String[] args) throws IOException {

        Scanner s = new Scanner(System.in); //java scanner to allow for input BTW
        boolean continueLanguage = true;
        while(continueLanguage){
            System.out.print("Enter file path or type \"REDACT\" to enter scripting :: ");
            String filepath = s.next();
            if(filepath.equals("REDACT")) {
                System.out.println("Press ctrl+c to exit scripting. To use again, you will have to rerun the main file");
                runPrompt();
            } else
                runFile(filepath);

            System.out.print("Do you want to continue? Y or N :: ");
            String continuance = s.next();

            if(continuance.toUpperCase().equals("N"))
                continueLanguage = false;
        }


        /*
        if (args.length > 1) {
            System.out.println("Usage: redaction [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }*/
    }

    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);

    }

    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    public static void run(String s) {
        src.redact.Scanner scanner = new src.redact.Scanner(s);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;
        interpreter.interpret(expression);
        //System.out.println(new AstPrinter().print(expression));
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}




