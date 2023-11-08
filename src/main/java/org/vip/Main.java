package org.vip;

import org.vip.Codegen.CodeGen;
import org.vip.Memmory.Symbol;
import org.vip.Lexer.Token;
import org.vip.Lexer.Tokenizer;
import org.vip.Parser.Parser;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String dir;
        boolean inCommand = false;
        List<Symbol> eventMap = new ArrayList<>();
        System.out.println("Enter the file directory: ");
//        dir = in.next();
        in.close();
        dir = "src/test/java";
        try {
            File Directory = new File(dir);
            for (File f : Directory.listFiles()) {
                String filename = f.getName();
                System.out.println(f);
                if (filename.endsWith(".vp")) {
                    Scanner input = new Scanner(f);
                    System.out.println("compiling : " + f.getName());
                    List TokenizerOutput = new ArrayList<>();
                    Tokenizer t = new Tokenizer(input);
                    TokenizerOutput = t.getTokens();
                    String file = dir.replace('/', '.') + "." + filename.replace(".vp", "");
                    Parser parser = new Parser(TokenizerOutput, file);
                    eventMap.addAll(parser.getCompilationEngineOutput());

                    input.close();
                }
            }
            CodeGen codeGen = new CodeGen(eventMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}