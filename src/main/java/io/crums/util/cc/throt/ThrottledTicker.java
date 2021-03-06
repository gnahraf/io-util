/*
 * Copyright 2014 Babak Farhang 
 */
package io.crums.util.cc.throt;


import java.util.logging.Logger;

import io.crums.util.cc.ThreadUtils;
import io.crums.util.ticker.Ticker;

/**
 * A throttled ticker.
 * 
 * <h3>Concurrent Access OK</h3>
 * 
 * This is designed to be well-behaved under concurrent access.
 * 
 * @author Babak
 */
public class ThrottledTicker extends Ticker {
  
  
  private volatile long throttleNanos;


  /**
   * Returns the minimum number of nanoseconds each invocation of {@linkplain #tick()}
   * takes to complete.
   */
  public long getThrottleNanos() {
    return throttleNanos;
  }


  /**
   * Sets the minimum number of nanoseconds each invocation of {@linkplain #tick()}
   * takes to complete. Setting the value to less than one effectively clears the
   * throttle.
   */
  public void setThrottleNanos(long throttleNanos) {
    this.throttleNanos = throttleNanos;
  }
  
  /**
   * Possibly throttled ticker.
   * 
   * @see #getThrottleNanos()
   */
  @Override
  public void tick() {
    if (throttleNanos < 1)
      return;
    try {
      ThreadUtils.ensureSleepNanos(throttleNanos);
    } catch (InterruptedException ix) {
      Logger.getLogger(ThrottledTicker.class.getName()).warning("Interrupted while throttling tick()");
      Thread.currentThread().interrupt();   // reset the interrupt flag
    }
  }
  
  
  

}
