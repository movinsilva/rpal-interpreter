package scanner;

/*
 * The Token class represents a token produced by the scanner and provided to the parser during the compilation process.
 * Each token encapsulates two main attributes: its type and its value.
 * While the token's type is essential for all cases, the value may be inconsequential for certain kinds of tokens,
 * such as DELETE and L_PAREN tokens. 
 */
public class Token{
  private TokenType type;
  private String value;
  private int sourceLineNumber;
  
  public TokenType getType(){
    return type;
  }
  
  public void setType(TokenType type){
    this.type = type;
  }
  
  public String getValue(){
    return value;
  }
  
  public void setValue(String value){
    this.value = value;
  }

  public int getSourceLineNumber(){
    return sourceLineNumber;
  }

  public void setSourceLineNumber(int sourceLineNumber){
    this.sourceLineNumber = sourceLineNumber;
  }
}
