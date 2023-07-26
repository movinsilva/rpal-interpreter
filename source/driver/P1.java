package driver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import ast.AST;
import parser.ParseException;
import parser.Parser;
import scanner.Scanner;

/**
 * Main driver class.
 * @author Raj
 */
public class P1{

  public static void main(String[] args){
    boolean listFlag = false;
    boolean astFlag = false;
    boolean noOutFlag = false;
    String fileName = "";
    AST ast = null;
    
    for(String cmdOption: args){
      if(cmdOption.equals("-l"))
        listFlag = true;
      else if(cmdOption.equals("-ast"))
        astFlag = true;
      else if(cmdOption.equals("-noout"))
        noOutFlag = true;
      else
        fileName = cmdOption;
    }
    
    //calling P1 without any switches produces no output
    if(!listFlag && !astFlag && !noOutFlag)
      return;
    
    if(listFlag){
      if(fileName.isEmpty())
        throw new ParseException("Please specify a file. Call P1 with -help to see examples");
      printInputListing(fileName);
    }
    
    if(astFlag){
      if(fileName.isEmpty())
        throw new ParseException("Please specify a file. Call P1 with -help to see examples");
      ast = buildAST(fileName, true);
      printAST(ast);
      //if(!noOutFlag)
      //  throw new ParseException("Interpreting has not been implemented as yet. Please provide -noout with -ast.");
    }
    
    //-noout without -ast produces no output
    if(noOutFlag && !astFlag){
      if(fileName.isEmpty())
        throw new ParseException("Please specify a file. Call P1 with -help to see examples");
    }
  }

  private static void printInputListing(String fileName){
    BufferedReader buffer = null;
    try{
      buffer = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
      String s = "";
      while((s = buffer.readLine())!=null){
        System.out.println(s);
      }
    }catch(FileNotFoundException e){
      throw new ParseException("File "+fileName+" not found.");
    }catch(IOException e){
      throw new ParseException("Error reading from file "+fileName);
    }finally{
      try{
        if(buffer!=null) buffer.close();
      }catch(IOException e){
      }
    }
  }

  private static AST buildAST(String fileName, boolean printOutput){
    AST ast = null;
    try{
      Scanner scanner = new Scanner(fileName);
      Parser parser = new Parser(scanner);
      ast = parser.buildAST();
    }catch(IOException e){
      throw new ParseException("ERROR: Could not read from file: " + fileName);
    }
    return ast;
  }

  private static void printAST(AST ast){
    ast.print();
  }

}
