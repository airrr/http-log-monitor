package gd.engineering.httplogmonitor.model;

/**
 * Exception for invalid log lines thrown in {@link gd.engineering.httplogmonitor.tailer.ApacheAccessLogParser}
 */
public class InvalidLogLineException extends RuntimeException {

  private static final String MESSAGE_FORMAT = "Invalid log line, %s: %s";

  public InvalidLogLineException(String reason, String logLine) {
    super(String.format(MESSAGE_FORMAT, reason, logLine));
  }

  public InvalidLogLineException(String reason, String logLine, Throwable t) {
    super(String.format(MESSAGE_FORMAT, reason, logLine), t);
  }

}
