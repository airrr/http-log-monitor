package gd.engineering.httplogmonitor.tailer;

import java.util.Queue;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gd.engineering.httplogmonitor.model.HttpLogLine;
import gd.engineering.httplogmonitor.model.InvalidLogLineException;

/**
 * Tailer listener parsing each line to {@link gd.engineering.httplogmonitor.model.HttpLogLine} and putting them in a blocking queue for processing.
 * It handles file rotation and truncation
 * The invalid lines are logged and skipped
 */
public class HttpLogTailerListener implements TailerListener {

  private static final Logger LOG = LoggerFactory.getLogger(HttpLogTailerListener.class);
  private Queue<HttpLogLine> logQueue;
  private HttpLogParser parser;

  public HttpLogTailerListener(HttpLogParser parser, Queue<HttpLogLine> logQueue) {
    this.parser = parser;
    this.logQueue = logQueue;
  }

  @Override
  public void init(Tailer tailer) {
  }

  @Override
  public void fileNotFound() {
  }

  @Override
  public void fileRotated() {
  }

  @Override
  public void handle(String line) {
    try {
      logQueue.offer(parser.parse(line));
    } catch (InvalidLogLineException ex) {
      LOG.error("", ex);
    }
  }

  @Override
  public void handle(Exception e) {
    LOG.error("", e);
  }
}
