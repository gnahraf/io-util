/*
 * Copyright 2013 Babak Farhang 
 */
package com.gnahraf.io.store.table;


import java.io.IOException;
import java.nio.ByteBuffer;

import com.gnahraf.io.store.table.del.DeleteCodec;
import com.gnahraf.io.store.table.iter.TableSetDIterator;
import com.gnahraf.io.store.table.iter.TableSetIterator;
import com.gnahraf.io.store.table.order.RowOrder;

/**
 * A {@linkplain TableSet} supporting delete overrides.
 * 
 * @author Babak
 */
public class TableSetD extends TableSet {


  private final DeleteCodec deleteCodec;
  
  private boolean hasDc() {
    return deleteCodec != null;
  }

  

  /**
   * Creates an empty instance.
   * 
   * @param deleteCodec <em>optional</em>
   */
  public TableSetD(RowOrder order, int rowWidth, DeleteCodec deleteCodec) {
    super(order, rowWidth);
    this.deleteCodec = deleteCodec;
  }

  public TableSetD(SortedTable table, DeleteCodec deleteCodec) throws IOException {
    this(new SortedTable[]{ table }, deleteCodec, false);
    checkTable(table);
  }


  public TableSetD(SortedTable[] tables, DeleteCodec deleteCodec) throws IOException {
    this(tables, deleteCodec, true);
  }


  protected TableSetD(SortedTable[] tables, DeleteCodec deleteCodec, boolean checkAndClone) throws IOException {
    super(tables, checkAndClone);
    this.deleteCodec = deleteCodec;
  }


  @Override
  public ByteBuffer getRow(ByteBuffer key) throws IOException {
    ByteBuffer row = super.getRow(key);
    if (row != null && hasDc() && deleteCodec.isDeleted(row))
      row = null;
    return row;
  }
  

  @Override
  public TableSetIterator iterator() throws IOException {
    return hasDc() ? new TableSetDIterator(this) : new TableSetIterator(this);
  }
  

  @Override
  public TableSetD append(SortedTable table) throws IOException {
    return new TableSetD(appendImpl(table), deleteCodec, false);
  }


  @Override
  public TableSetD append(SortedTable... table) throws IOException {
    return new TableSetD(appendImpl(table), deleteCodec, false);
  }


  /**
   * May be <tt>null</tt>
   */
  public final DeleteCodec getDeleteCodec() {
    return deleteCodec;
  }
  
  

}
