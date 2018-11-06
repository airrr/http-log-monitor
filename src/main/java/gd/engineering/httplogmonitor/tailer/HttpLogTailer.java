package gd.engineering.httplogmonitor.tailer;

import java.io.File;

import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around the apache commons {@link org.apache.commons.io.input.Tailer} with a companion Thread
 */
public class HttpLogTailer {

  private static final Logger LOG = LoggerFactory.getLogger(HttpLogTailer.class);
  private Tailer logTailer;
  private Thread thread;

  /**
   * Create a new log tailer and its companion thread with the provided listener.
   * The tailer will pull new lines at the tailerDelay interval in milliseconds
   *
   * @param logFile       File to tail from
   * @param listener      Listener handling each line
   * @param tailerDelayMs Tail interval in milliseconds
   */
  public HttpLogTailer(String logFile, HttpLogTailerListener listener, long tailerDelayMs) {
    this.logTailer = new Tailer(new File(logFile), listener, tailerDelayMs, true);
    this.thread = new Thread(logTailer);
  }

  /**
   * Starts the tailer companion thread
   */
  public void start() {
    LOG.info("Starting to tail {}", this.logTailer.getFile().getName());
    thread.start();
  }

  /**
   * Stops the tailer which will stop the companion thread
   */
  public void stop() {
    LOG.info("Stopping to tail {}", this.logTailer.getFile().getName());
    logTailer.stop();
    try {
      this.thread.join();
    } catch (InterruptedException e) {
      LOG.error("Tailer interrupted while shutting down");
    }
  }
}
