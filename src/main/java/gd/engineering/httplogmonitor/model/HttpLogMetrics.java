package gd.engineering.httplogmonitor.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Metrics recorded by the http log monitor and helpful methods for section metrics
 */
public class HttpLogMetrics {
  public static final String COUNTER_HITS = "hits";
  public static final String COUNTER_ERRORS = "errors";
  public static final String COUNTER_SUCCESSES = "successes";
  public static final String COUNTER_SECTION = "section";
  public static final String DELIMITER = ":";

  /**
   * Build the a section counter prefix.
   * e.g: section:<sectionname>
   *
   * @param sectionName Section name
   * @return Section counter prefix
   */
  public static String getSectionCounterPrefix(String sectionName) {
    return COUNTER_SECTION + DELIMITER + sectionName;
  }

  /**
   * Based upon a counter, extracts the section name
   * e.g: section:GET/api:hits will return GET/API
   *
   * @param counter Any counter name
   * @return The section name
   * @throws IllegalArgumentException If the counter is not a specific section counter
   */
  public static String getSectionNameFromCounter(String counter) {
    if (StringUtils.isEmpty(counter) || !counter.startsWith(COUNTER_SECTION)) {
      throw new IllegalArgumentException("GetSectionName: Invalid counter name " + counter);
    }
    String[] counterParts = counter.split(DELIMITER);
    if (counterParts.length < 2) {
      throw new IllegalArgumentException("GetSectionName:  Invalid counter name" + counter);
    }
    return counterParts[1];
  }

  private HttpLogMetrics() {
  }
}
