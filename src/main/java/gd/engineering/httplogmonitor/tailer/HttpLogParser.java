package gd.engineering.httplogmonitor.tailer;

import gd.engineering.httplogmonitor.model.HttpLogLine;

/**
 * Interface for parsing log lines to {@link gd.engineering.httplogmonitor.model.HttpLogLine}
 */
public interface HttpLogParser {
  HttpLogLine parse(String logLine);
}
