/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.TreeSet;

/**
 * String and CharSequence static utilities.
 */
public class Strings {
  
  // nobody calls
  private Strings() {  }
  
  
  public final static Charset UTF_8 = Charset.forName("UTF-8");
  
  
  /**
   * Returns the given character sequence containing no duplicate characters
   * as a string composed of its characters in lexicographic order. Introduced
   * for cannonicalizing command line options input when the order of the characters
   * doesn't matter.
   * 
   * @param charSet a sequence of characters with unique occurance
   * 
   * @return a string with the input characters in lexicographic order
   */
  public static String orderCharSet(CharSequence charSet) {
    TreeSet<Character> lexOrdererdChars = new TreeSet<>();
    for (int index = 0; index < charSet.length(); ++index) {
      char c = charSet.charAt(index);
      if (!lexOrdererdChars.add(c))
        throw new IllegalArgumentException(
            "character '" + c + "' occurs multiple times in " + charSet);
    }
    StringBuilder s = new StringBuilder(lexOrdererdChars.size());
    for (char c : lexOrdererdChars)
      s.append(c);
    return s.toString();
  }
  
  
  
  public static String nTh(int count) {
    switch (count) {
    case 1:
      return "1st";
    case 2:
      return "2nd";
    case 3:
      return "3rd";
    default:
      return count + "th";
    }
  }
  

  
  
  public static String pluralize(String single, long count) {
    if (count == 1)
      return single;
    if (single.endsWith("y"))
      return single.substring(0, single.length() - 1) + "ies";
    else
      return single + "s";
  }
  
  
  /**
   * 
   * @param thirdPpp 3rd person present tense of a verb (e.g. "divide")
   * @param count &ge; 0
   * 
   * @return
   */
  public static String singularVerb(String thirdPpp, long count) {
    return count == 1 ? thirdPpp + "s" : thirdPpp;
  }
  
  
  public static boolean isWhitespace(char c) {
    switch (c) {
    case '\t':
    case '\n':
    case '\f':
    case '\r':
    case ' ':
      return true;
    default:
      return false;
    }
  }
  
  
  
  
  public static byte[] utf8Bytes(String string) {
    return string.getBytes(UTF_8);
  }
  
  
  public static ByteBuffer utf8Buffer(String string) {
    return ByteBuffer.wrap(utf8Bytes(string)).asReadOnlyBuffer();
  }
  
  
  public static String utf8String(byte[] bytes) {
    return new String(bytes, UTF_8);
  }
  
  
  public static String utf8String(ByteBuffer bytes) {
    byte[] b = new byte[bytes.remaining()];
    bytes.get(b);
    return new String(b, UTF_8);
  }
  
  
  
  /**
   * Un-escapes the given escaped sequence of characters. Escaping
   * here just means c-style chars such as '\t', '\n', and so on.
   * See {@linkplain CharEscape} for the list of escaped characters.
   * 
   * @param escaped a possibly escaped char sequence (null ok)
   */
  public static String unescape(String escaped) {
    
    return CharEscape.unescape(escaped);
  }
  
  
  
  public enum CharEscape {
    /**
     * Tab '\t'.
     */
    TB('\t', "t"),
    /**
     * New line '\n'.
     */
    NL('\n', "n"),
    /**
     * Form feed '\f'.
     */
    FF('\f', "f"),
    /**
     * Carriage return '\r'.
     */
    CR('\r', "r"),
    /**
     * Back-slash '\\'.
     */
    BS('\\', "\\");
    
    
    
    public final static char BACK_SLASH = '\\';

    private final static CharEscape[] vals = values();
    
    
    
    /**
     * Escape character.
     */
    public final char eChar;
    /**
     * Escaped representation of eChar is of length 2.
     */
    public final String escaped;
    
    private CharEscape(char c, String esc) {
      this.eChar = c;
      this.escaped = '\\' + esc;
    }
    
    
    /**
     * Returns the next escape position beyond {@code fromIndex}, or null
     * if it doesn't occur.
     * 
     * @param escaped the string in escaped representation (null ok)
     */
    public static EscapePos nextEscapePos(String escaped, int fromIndex) {
      if (escaped == null || escaped.length() - 2 < fromIndex)
        return null;
      
      {
        int bsIndex = escaped.indexOf(BACK_SLASH, fromIndex);
        if (bsIndex == -1)
          return null;
        else
          fromIndex = bsIndex;
      }
      
      int min = escaped.length();
      CharEscape c = null;
      for (int ri = vals.length; ri-- > 0; ) {
        var e = vals[ri];
        int index = escaped.indexOf(e.escaped, fromIndex);
        if (index >= 0 && index < min) {
          min = index;
          c = e;
        }
      }
      
      return c == null ? null : new EscapePos(min, c);
    }
    

    /**
     * Un-escapes the given escaped sequence of characters. Escaping
     * here just means c-style chars such as '\t', '\n', and so on.
     * See {@linkplain CharEscape} for the list of escaped characters.
     * 
     * @param escaped a possibly escaped char sequence (null ok)
     */
    public static String unescape(String escaped) {
      EscapePos pos = nextEscapePos(escaped, 0);
      if (pos == null)
        return escaped;
      
      StringBuilder string = new StringBuilder(escaped.length());
      string.append(escaped.substring(0, pos.index)).append(pos.echar());
      
      while (true) {
        var nextPos = nextEscapePos(escaped, pos.index + 2);
        if (nextPos == null)
          break;
        string.append(escaped.substring(pos.index + 2, nextPos.index)).append(nextPos.echar());
        pos = nextPos;
      }
      
      if (pos.index + 2 < escaped.length())
        string.append(escaped.substring(pos.index + 2));
      
      return string.toString();
    }
  }
  
  
  public static class EscapePos {
    
    public final int index;
    public final CharEscape e;
    
    public EscapePos(int index, CharEscape e) {
      this.index = index;
      this.e = e;
      if (index < 0) throw new IllegalArgumentException("index " + index);
      Objects.requireNonNull(e, "null CharEscape");
    }
    
    public char echar() {
      return e.eChar;
    }
  }

}











