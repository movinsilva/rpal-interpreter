package csem;

import driver.P2;

public class EvaluationError{
  
  public static void printError(int sourceLineNumber, String message){
    System.out.println(P2.fileName+":"+sourceLineNumber+": "+message);
    System.exit(1);
  }

}
