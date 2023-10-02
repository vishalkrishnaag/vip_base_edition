package org.vip;
import org.vip.Codegen.CodeGen;
import org.vip.Memmory.Symbol;
import org.vip.Lexer.Token;
import org.vip.Lexer.Tokenizer;
import org.vip.Parser.Parser;

import java.io.*;
import java.util.*;

public class Main {
     int add(int a, int b)
     {
         return  1;
     }
    int add(int a, int b,int c)
    {
        return  1;
    }
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String dir;
        boolean inCommand = false;
        List<Symbol> eventMap=new ArrayList<>();
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
                    File file = new File(dir + "/" + filename.replace(".vp", ".vpx"));
                    Scanner input = new Scanner(f);
                    System.out.println("compiling : "+f.getName());
                    ArrayList<String> jackcoms = new ArrayList<>();
                    while (input.hasNext()) {
                        String line = input.nextLine();
                        boolean ws = true;
                        if (line.strip().isEmpty())
                            ws = true;
                        else
                            ws = false;
                        if (ws)
                            continue;
                        else {

                            line = line.strip();
                            if (line.contains("//")) {
                                int hashIndex = line.indexOf("//");
                                if (hashIndex != -1) {
                                    String substringBeforeHash = line.substring(0, hashIndex);
                                    if (!substringBeforeHash.isEmpty()) {

                                        jackcoms.add(substringBeforeHash);
                                    }

                                }
                            } else if (line.contains("/*")) {

                                inCommand = true;
                                if (line.contains("*/")) {
                                    inCommand = false;
                                    int hashIndex = line.indexOf("*/");
                                    if (hashIndex != -1) {
                                        String substringBeforeHash = line.substring(hashIndex + 2);
                                        if(!substringBeforeHash.isEmpty())
                                        {
                                            jackcoms.add(substringBeforeHash);
                                        }
                                    }

                                    }

                                } else if (line.contains("*/")) {
                                    inCommand = false;
                                    int hashIndex = line.indexOf("*/");
                                    if (hashIndex != -1) {
                                        String substringBeforeHash = line.substring(hashIndex + 2);
                                        if(!substringBeforeHash.isEmpty())
                                        {
                                            jackcoms.add(substringBeforeHash);
                                        }
                                    }
                                } else {
                                    if (!inCommand && !line.isEmpty()) {
                                        jackcoms.add(line);
                                    }
                                }

                            }
                        }
                        List<Token> TokenizerOutput = new ArrayList<Token>();
                        TokenizerOutput = Tokenizer(jackcoms);
//                        for (int i = 0; i < TokenizerOutput.size(); i++) {
//                            System.out.println("lexme: " + TokenizerOutput.get(i).getLexme() + "  ,type : " + TokenizerOutput.get(i).getType());
//                        }
                    Parser parser = new Parser(TokenizerOutput);
                    eventMap.addAll(parser.getCompilationEngineOutput()) ;

//                        for (int i = 0; i < CompEngOutput.size(); i++)
//                            System.out.println(CompEngOutput.get(i));
                        input.close();
                    }
                }
//            Analyzer analyzer = new Analyzer(eventMap);
            CodeGen codeGen = new CodeGen(eventMap);
            } catch(Exception e){
                e.printStackTrace();
            }


        }

        public static List<Token> Tokenizer (ArrayList < String > jackcoms) {
            List<Token> TokenizerOutput = new ArrayList<Token>();
            Tokenizer tok = new Tokenizer(jackcoms);
            TokenizerOutput = tok.getTokens();
            return TokenizerOutput;
        }
    }