package scanner;

import java.util.regex.Pattern;

/**
 * The RPAL scanner utilizes these regular expression matchers to comply with the lexical grammar of RPAL.
 * These matchers are responsible for tokenizing the input, breaking it down into meaningful elements.
 */

public class LexicalRegexPatterns{
  private static final String letterRegex = "a-zA-Z";
  private static final String digitRegex = "\\d";
  private static final String spaceRegex = "[\\s\\t\\n]";
  private static final String punctuationRegex = "();,";
  private static final String opSymbolRegexS = "+-/~:=|!#%_{}\"*<>.&$^\\[\\]?@";
  private static final String opSymbolToEscape = "([*<>.&$^?])";
  
  public static final Pattern LetterPattern = Pattern.compile("["+letterRegex+"]");
  
  public static final Pattern IdentifierPattern = Pattern.compile("["+letterRegex+digitRegex+"_]");

  public static final Pattern DigitPattern = Pattern.compile(digitRegex);

  public static final Pattern PunctuationPattern = Pattern.compile("["+punctuationRegex+"]");

  public static final String opSymbolRegex = "[" + escapeMetaChars(opSymbolRegexS, opSymbolToEscape) + "]";
  public static final Pattern OpSymbolPattern = Pattern.compile(opSymbolRegex);
  
  public static final Pattern StringPattern = Pattern.compile("[ \\t\\n\\\\"+punctuationRegex+letterRegex+digitRegex+escapeMetaChars(opSymbolRegexS, opSymbolToEscape) +"]");
  
  public static final Pattern SpacePattern = Pattern.compile(spaceRegex);
  
  public static final Pattern CommentPattern = Pattern.compile("[ \\t\\'\\\\ \\r"+punctuationRegex+letterRegex+digitRegex+escapeMetaChars(opSymbolRegexS, opSymbolToEscape)+"]"); //the \\r is for Windows LF; not really required since we're targeting *nix systems
  
  private static String escapeMetaChars(String inputString, String charsToEscape){
    return inputString.replaceAll(charsToEscape,"\\\\\\\\$1");
  }
}