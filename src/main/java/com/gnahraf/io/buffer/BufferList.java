/*
 * Copyright 2020 Babak Farhang
 */
package com.gnahraf.io.buffer;


import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * <tt>List</tt> view of an underlying block of memory. This is suitable if only a
 * small subset of the elements will actually be accessed. The reaon why, is that
 * this uses what I call the <em>create-element-on-demand</em> pattern.
 * 
 * <h3>Concurrency Note</h3>
 * <p>
 * <em>Instances are safe under concurrent access.</em>
 * </p>
 */
public class BufferList extends AbstractList<ByteBuffer> implements RandomAccess {
  
  /**
   * Invariants: the positional state of is never changed.
   */
  private final ByteBuffer block;
  private final int elementWidth;

  
  
  /**
   * Creates a new instance. Equivalent to
   * {@linkplain #BufferList(ByteBuffer, int, boolean) BufferList(block, elementWidth, true)}.
   * 
   * @param block         the source block (whose remaining bytes are used to determine the size of this
   *                      list). If its read-only, then the elements of this list will
   *                      be read-only. If the underlying buffer's contents are externally modified, then
   *                      those modifications will be visible in this instance (and viceversa if
   *                      <tt>block</tt> is not read-only).
   * @param elementWidth  the number of bytes for each element in the block
   */
  public BufferList(ByteBuffer block, int elementWidth) {
    this(block, elementWidth, true);
  }

  
  
  /**
   * Creates a new instance.
   * 
   * @param block         the source block. If its read-only, then the elements of this list will
   *                      be read-only. If the underlying buffer's contents are externally modified, then
   *                      those modifications will be visible in this instance (and viceversa if
   *                      <tt>block</tt> is not read-only).
   * @param elementWidth  the number of bytes for each element in the block
   * @param slice         if <tt>true</tt>, then <tt>block</tt> is sliced (and the
   *                      caller can later freely modify the argument's position, limit, etc.);
   *                      if <tt>false</tt>, then it is understood the caller will not modify
   *                      positional fields in <tt>block</tt> and that the entire block (ignoring
   *                      position and limit) belongs to this list
   */
  public BufferList(ByteBuffer block, int elementWidth, boolean slice) {
    Objects.requireNonNull(block, "null block");
    this.block = slice ? block.slice() : block.clear();
    this.elementWidth = elementWidth;
    
    if (elementWidth < 2)
      throw new IllegalArgumentException("elementWidth " + elementWidth + " < 2");
  }
  
  
  
  public final int elementWidth() {
    return elementWidth;
  }

  
  /**
   * 
   */
  @Override
  public ByteBuffer get(int index) {
    Objects.checkIndex(index, size());
    int position = index * elementWidth;
    return block.duplicate().clear().limit(position + elementWidth).position(position).slice();
  }

  
  
  @Override
  public int size() {
    return block.capacity() / elementWidth;
  }

}
