package scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/*
 * The Scanner module is an integration of a lexer and a screener, designed to adhere to the Lexicon of RPAL.
 * Its function is to process the input source code, applying lexical analysis and filtering operations
 * to ensure compliance with the language's specific rules and syntax.
 */

public class Scanner{
  private BufferedReader bufferReader;
  private String extraCharacters;
  private final List<String> reservedIdentifiers = Arrays.asList(new String[]{"let","in","within","fn","where","aug","or",
                                                                              "not","gr","ge","ls","le","eq","ne","true",
                                                                              "false","nil","dummy","rec","and"});
  private int sourceLineNumber;
  
  public Scanner(String inputFile) throws IOException{
    sourceLineNumber = 1;
    bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile))));
  }
  
  /*
   * This function retrieves the next token from the input file.
   * If the file has reached its end, the function returns null as an indication.
   */

  public Token readNextToken(){
    Token nxtToken = null;
    String nxtChar;
    if(extraCharacters!=null){
      nxtChar = extraCharacters;
      extraCharacters = null;
    } else
      nxtChar = readNextChar();
    if(nxtChar!=null)
      nxtToken = buildToken(nxtChar);
    return nxtToken;
  }

  private String readNextChar(){
    String nextChar = null;
    try{
      int c = bufferReader.read();
      if(c!=-1){
        nextChar = Character.toString((char)c);
        if(nextChar.equals("\n")) sourceLineNumber++;
      } else
          bufferReader.close();
    }catch(IOException e){
    }
    return nextChar;
  }

  /*
   * This function constructs the next token from the input stream.
   * It takes the currently processed character as input and assembles the token based on the language's rules and grammar.
   * The function then returns the built token as the output.
   */
  private Token buildToken(String currentChar){
    Token nxtToken = null;
    if(LexicalRegexPatterns.LetterPattern.matcher(currentChar).matches()){
      nxtToken = buildIdentifierToken(currentChar);
    }
    else if(LexicalRegexPatterns.DigitPattern.matcher(currentChar).matches()){
      nxtToken = buildIntegerToken(currentChar);
    }
    else if(LexicalRegexPatterns.OpSymbolPattern.matcher(currentChar).matches()){ 
      nxtToken = buildOperatorToken(currentChar);
    }
    else if(currentChar.equals("\'")){
      nxtToken = buildStringToken(currentChar);
    }
    else if(LexicalRegexPatterns.SpacePattern.matcher(currentChar).matches()){
      nxtToken = buildSpaceToken(currentChar);
    }
    else if(LexicalRegexPatterns.PunctuationPattern.matcher(currentChar).matches()){
      nxtToken = buildPunctuationPattern(currentChar);
    }
    return nxtToken;
  }

  /*
   * This function constructs an Identifier token based on the input stream's current character.
   * The Identifier is formed according to the language's grammar, which states that an Identifier starts with a Letter
   * and can be followed by a combination of Letters, Digits, or underscores. 
   * The function processes the current character and potentially subsequent characters to build the complete Identifier token.
   * Finally, it returns the constructed token as the output.
   */

  private Token buildIdentifierToken(String currentChar){
    Token idToken = new Token();
    idToken.setType(TokenType.IDENTIFIER);
    idToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = readNextChar();
    while(nextChar!=null){ //null indicates the file ended
      if(LexicalRegexPatterns.IdentifierPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
      else{
        extraCharacters = nextChar;
        break;
      }
    }
    
    String value = sBuilder.toString();
    if(reservedIdentifiers.contains(value))
      idToken.setType(TokenType.RESERVED);
    
    idToken.setValue(value);
    return idToken;
  }

  /*
   * This function constructs an Integer token based on the input stream's current character.
   * The Integer token is formed following the language's grammar, which specifies that it consists of one or more Digits.
   * The function processes the current character and, if applicable, subsequent Digits to build the complete Integer token.
   * Once constructed, the function returns the resulting token as the output.
   */
  private Token buildIntegerToken(String currentChar){
    Token intToken = new Token();
    intToken.setType(TokenType.INTEGER);
    intToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nxtChar = readNextChar();
    while(nxtChar!=null){ //null indicates the file ended
      if(LexicalRegexPatterns.DigitPattern.matcher(nxtChar).matches()){
        sBuilder.append(nxtChar);
        nxtChar = readNextChar();
      }
      else{
        extraCharacters = nxtChar;
        break;
      }
    }
    
    intToken.setValue(sBuilder.toString());
    return intToken;
  }

  /*
   * This function constructs an Operator token based on the input stream's current character.
   * The Operator token is formed according to the language's grammar, which specifies that it consists of one or more Operator symbols.
   * The function processes the current character and, if applicable, subsequent Operator symbols to build the complete Operator token.
   * Once constructed, the function returns the resulting token as the output.
   */

  private Token buildOperatorToken(String currentChar){
    Token opSymbolToken = new Token();
    opSymbolToken.setType(TokenType.OPERATOR);
    opSymbolToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nxtChar = readNextChar();
    
    if(currentChar.equals("/") && nxtChar.equals("/"))
      return buildCommentToken(currentChar+nxtChar);
    
    while(nxtChar!=null){ //null indicates the file ended
      if(LexicalRegexPatterns.OpSymbolPattern.matcher(nxtChar).matches()){
        sBuilder.append(nxtChar);
        nxtChar = readNextChar();
      }
      else{
        extraCharacters = nxtChar;
        break;
      }
    }
    
    opSymbolToken.setValue(sBuilder.toString());
    return opSymbolToken;
  }

  /*
   * This function constructs a String token based on the input stream's current character.
   * The String token is formed following the language's grammar,
   * which specifies that it starts and ends with four consecutive single quotes ('' '').
   * Between the opening and closing quotes, various characters are allowed, 
   * such as '''' ('\' 't' | '\' 'n' | '\' '\' | '\' '''' |'(' | ')' | ';' | ',' |'' |Letter | Digit | Operator_symbol )* ''''.
   * The function processes the current character and, if applicable, subsequent characters to build the complete String token.
   * Once constructed, the function returns the resulting token as the output.
   */

  private Token buildStringToken(String currentChar){
    Token stringToken = new Token();
    stringToken.setType(TokenType.STRING);
    stringToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder("");
    
    String nxtChar = readNextChar();
    while(nxtChar!=null){ //null --> end of file
      if(nxtChar.equals("\'")){ //since last char,no need to set extraCharacters
        stringToken.setValue(sBuilder.toString());
        return stringToken;
      }
      else if(LexicalRegexPatterns.StringPattern.matcher(nxtChar).matches()){ //match Letter | Digit | Operator_symbol
        sBuilder.append(nxtChar);
        nxtChar = readNextChar();
      }
    }
    
    return null;
  }
  
  /*
   * This function is responsible for building a Space token from the input stream, starting with the provided current character.
   * The Space token is formed by a sequence of consecutive space characters.
   * The function reads subsequent characters from the input stream until a non-space character is encountered or the file ends.
   * It then constructs the Space token with the collected space characters and returns it.
   * The variable 'extraCharacters' is used to keep track of any non-space character encountered after the space sequence,
   * which may be used for further processing in the scanning process. 
   */
  private Token buildSpaceToken(String currentChar){
    Token deleteToken = new Token();
    deleteToken.setType(TokenType.DELETE);
    deleteToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = readNextChar();
    while(nextChar!=null){ //null indicates the file ended
      if(LexicalRegexPatterns.SpacePattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
      else{
        extraCharacters = nextChar;
        break;
      }
    }
    
    deleteToken.setValue(sBuilder.toString());
    return deleteToken;
  }
  
  /*
   * This function constructs a Comment token starting from the provided current character.
   * It reads subsequent characters from the input stream and appends them to the token's value until 
   * the end of the file is reached (indicated by null) or a newline character ('\n') is encountered.
   * The Comment token is formed by matching consecutive characters with the Comment pattern defined by lexical grammar.
   * creates and returns the Comment token with the collected characters, including the newline character if encountered.
   */

  private Token buildCommentToken(String currentChar){
    Token commentToken = new Token();
    commentToken.setType(TokenType.DELETE);
    commentToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nxtChar = readNextChar();
    while(nxtChar!=null){ //null indicates the file ended
      if(LexicalRegexPatterns.CommentPattern.matcher(nxtChar).matches()){
        sBuilder.append(nxtChar);
        nxtChar = readNextChar();
      }
      else if(nxtChar.equals("\n"))
        break;
    }
    
    commentToken.setValue(sBuilder.toString());
    return commentToken;
  }

/*
 * This function constructs a Punctuation token using the provided current character.
 * It sets the token's source line number and value as the current character.
 * Then determines the specific type of punctuation token by comparing the current character to 
 * predefined characters ('(', ')', ';', ',') and assigns the corresponding token type accordingly. 
 * Returns the constructed Punctuation token with the relevant information.
 */

  private Token buildPunctuationPattern(String currentChar){
    Token punctuationToken = new Token();
    punctuationToken.setSourceLineNumber(sourceLineNumber);
    punctuationToken.setValue(currentChar);
    if(currentChar.equals("("))
      punctuationToken.setType(TokenType.L_PAREN);
    else if(currentChar.equals(")"))
      punctuationToken.setType(TokenType.R_PAREN);
    else if(currentChar.equals(";"))
      punctuationToken.setType(TokenType.SEMICOLON);
    else if(currentChar.equals(","))
      punctuationToken.setType(TokenType.COMMA);
    
    return punctuationToken;
  }
}

