/*
 * Copyright 2013 Babak Farhang 
 */
package com.gnahraf.io;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * 
 * @author Babak
 */
public class FileUtils {
  
  public final static long LOAD_AS_STRING_DEFAULT_MAX_FILE_SIZE = 512 * 1024;

  private FileUtils() {  }
  
  
  public static void assertDirArg(File dir) throws IllegalArgumentException {
    nonNullPath(dir);
    if (!dir.isDirectory())
      throw new IllegalArgumentException("not a directory: " + dir.getAbsolutePath());
  }
  
  public static void assertDir(File dir) throws FileNotFoundException {
    nonNullPath(dir);
    if (!dir.isDirectory())
      throw new FileNotFoundException("expected a directory: " + dir.getAbsolutePath());
  }
  
  private static void  nonNullPath(File dir) {
    if (dir == null)
      throw new IllegalArgumentException("null path");
  }
  
  public static void ensureDir(File dir) throws IllegalStateException {
    nonNullPath(dir);
    if (dir.isDirectory())
      return;
    if (dir.exists())
      throw new IllegalStateException("cannot overwrite ordinary file as dir: " + dir.getAbsolutePath());
    
    if (!dir.mkdirs() && !dir.isDirectory())
      throw new IllegalStateException("failed to create directory: " + dir.getAbsolutePath());
  }
  
  
  public static void moveToDir(File file, File dir) throws FileNotFoundException, IllegalStateException {
    assertDir(dir);
    assertFile(file);
    File target = new File(dir, file.getName());
    if (!file.renameTo(target))
      throw new IllegalStateException("failed to move " + file.getPath() + " to " + target.getPath());
  }
  
  
  public static void assertFile(File file) throws IllegalArgumentException, FileNotFoundException {
    if (file == null)
      throw new IllegalArgumentException("null file");
    if (!file.isFile()) {
      String message = file.isDirectory() ? "expected file but found directory: " : "expected file does not exist: ";
      message += file.getAbsolutePath();
      throw new FileNotFoundException(message);
    }
  }
  
  
  public static void assertDoesntExist(File file) throws IoStateException {
    if (file == null)
      throw new IllegalArgumentException("null file path");
    if (file.exists())
      throw new IoStateException("path already exists: " + file.getAbsolutePath());
  }
  
  public static String loadAsString(File file) throws IOException {
    return loadAsString(file, LOAD_AS_STRING_DEFAULT_MAX_FILE_SIZE);
  }
  
  public static String loadAsString(File file, long maxFileSize) throws IOException {
    assertFile(file);

    long size = file.length();
    if (size == 0)
      return "";
    
    // sanity + arg check..
    if (size < 0 || size > maxFileSize) {
      throw new IllegalArgumentException(
          "maxFileSize is " + maxFileSize + "; actual file size for " +
          file.getAbsolutePath() + " is " + size);
    }
    
    StringBuilder buffer = new StringBuilder();
    try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
      
      for (CharBuffer cbuf = CharBuffer.allocate(4096); reader.read(cbuf) != -1; cbuf.clear()) {
        cbuf.flip();
        buffer.append(cbuf);
      }
    }
    return buffer.toString();
  }


  
  public static void delete(File file) throws IllegalStateException {
    file.delete();
    if (file.exists())
      throw new IllegalStateException("failed to delete " + file.getAbsolutePath());
  }

  
  
  public static int copyRecurse(File source, File target) throws IOException {
    return copyRecurse(source, target, false);
  }
  
  
  /**
   * Recursively copies.
   * 
   * @param source       an existing path. If it's a directory, its files/subdirectories
   *                     are recursively copied/created
   * @param target       the target path
   * @param overwrite    if <tt>true</tt>, and <tt>target</tt> is an existing file, then
   *                     the file will be overwritten; o.w. an {@linkplain IllegalArgumentException}
   *                     is raised. If <tt>target</tt> is a directory (and <tt>source</tt> is too),
   *                     then this argument doesn't matter
   * @return             the number of <em>files</em> (not directories) copied
   * @throws IOException
   */
  public static int copyRecurse(File source, File target, boolean overwrite) throws IOException {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(target, "target");
    if (source.equals(target))
      return 0;
    if (target.getAbsoluteFile().toPath().startsWith(source.getAbsoluteFile().toPath()))
      throw new IllegalArgumentException("target " + target + " is a subpath of source " + source);
    
    return copyRecurseImpl(source, target, overwrite);
  }
  
  
  private static int copyRecurseImpl(File source, File target, boolean overwrite) throws IOException {
    copyImpl(source, target, overwrite);
    if (source.isFile())
      return 1;
    int count = 0;
    String[] subpaths = source.list();
    for (String subpath : subpaths)
      count += copyRecurseImpl(new File(source, subpath), new File(target, subpath), overwrite);
    return count;
  }
  

  
  
  public static void copy(File source, File target) throws IOException {
    copy(source, target, false);
  }
  
  
  public static void copy(File source, File target, boolean overwrite) throws IOException {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(target, "target");
    if (source.equals(target))
      return;
    copyImpl(source, target, overwrite);
  }
  
  private static void copyImpl(File source, File target, boolean overwrite) throws IOException {
    if (source.isFile()) {
      if (target.exists()) {
        if (target.isDirectory())
          throw new IllegalArgumentException(
              "target " + target + " is a dir while source " + source + " is not");
        if (!overwrite)
          throw new IllegalArgumentException("attempt to overwrite " + target);

        java.nio.file.Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } else
        java.nio.file.Files.copy(source.toPath(), target.toPath());
    
    } else if (source.isDirectory())
      ensureDir(target);
    else
      throw new IllegalArgumentException("source does not exist: " + source);
  }

}