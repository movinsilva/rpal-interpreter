package scanner;

/*
 * Type of token constructed by the scanner.
 */
public enum TokenType{
  IDENTIFIER,
  INTEGER,
  STRING,
  OPERATOR,
  DELETE,
  L_PAREN,
  R_PAREN,
  SEMICOLON,
  COMMA,
  RESERVED; //this is used to distinguish reserved RPAL keywords
}
