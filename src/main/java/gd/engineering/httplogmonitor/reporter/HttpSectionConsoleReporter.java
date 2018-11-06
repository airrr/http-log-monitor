package gd.engineering.httplogmonitor.reporter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gd.engineering.httplogmonitor.model.HttpLogMetrics;
import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

import static gd.engineering.httplogmonitor.model.HttpLogMetrics.COUNTER_ERRORS;
import static gd.engineering.httplogmonitor.model.HttpLogMetrics.COUNTER_HITS;
import static gd.engineering.httplogmonitor.model.HttpLogMetrics.DELIMITER;

/**
 * Reports on the console various traffic metrics (see example below), global and per section.
 * The number of sections displayed is controlled by the maxSections argument.
 * The sections displayed are the most hit sections during the last flush
 * The metrics come from the metrics store.
 * <p>
 * Each report is triggered when a new flush arrives.
 * <p>
 * Example of report:
 * <p>
 * ---HTTP monitor report between 06/Nov/2018:00:42:42 and 06/Nov/2018:00:42:43---
 * Total hits since start: 24 | Error rate: 0%
 * Interval hits: 2 | Error rate: 0%
 * *** Top 5 sections by traffic
 * section: traffic part | hit count | error rate (4XX, 5XX)
 * POST/api5: 100% | 2 | 0%
 */
public class HttpSectionConsoleReporter implements MetricsReporter {

  private static final Logger LOG = LoggerFactory.getLogger("reporter-log");
  private static final DateTimeFormatter REPORT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss");
  private MetricsStore metricsStore;
  private int maxSections;

  /**
   * Creates a new reporter on the provided store, displaying the top maxSections sections
   *
   * @param store       Metrics store
   * @param maxSections Number of top sections to display
   */
  public HttpSectionConsoleReporter(MetricsStore store, int maxSections) {
    this.maxSections = maxSections;
    this.metricsStore = store;
  }

  @Override
  public void report(IntervalMetrics latestMetrics) {
    List<String> sortedSectionStatsByHits = getSortedSectionMetricsByHits(latestMetrics.getCounters(), this.maxSections);
    int historicalHits = metricsStore.getTotalSumCounterValue(HttpLogMetrics.COUNTER_HITS);
    int historicalErrors = metricsStore.getTotalSumCounterValue(COUNTER_ERRORS);
    int historicalErrorRate = historicalHits == 0 ? 0 : 100 * historicalErrors / historicalHits;
    int totalHits = latestMetrics.getCounterValue(HttpLogMetrics.COUNTER_HITS);
    int requestsPerSecond = latestMetrics.getRateValue(HttpLogMetrics.COUNTER_HITS);
    int totalErrorRate = totalHits == 0 ? 0 : 100 * latestMetrics.getCounterValue(COUNTER_ERRORS) / totalHits;
    String startTime = Instant.ofEpochMilli(latestMetrics.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDateTime().format(REPORT_DATETIME_FORMAT);
    String endTime = Instant.ofEpochMilli(latestMetrics.getEndTime()).atZone(ZoneId.systemDefault()).toLocalDateTime().format(REPORT_DATETIME_FORMAT);
    LOG.info("---HTTP monitor report between {} and {}---", startTime, endTime);
    LOG.info("Total hits since start: {} | Total error rate:    {}%", historicalHits, historicalErrorRate);
    LOG.info("Interval hits:          {} | Interval requests/s: {} | Interval error rate: {}%", totalHits, requestsPerSecond, totalErrorRate);
    if (!sortedSectionStatsByHits.isEmpty()) {
      LOG.info("*** Top {} sections by traffic", this.maxSections);
      LOG.info("Method/Section: Traffic part | Hit count | Error rate (4XX, 5XX)");
      sortedSectionStatsByHits.forEach(section -> {
        String sectionPrefix = HttpLogMetrics.getSectionCounterPrefix(section);
        int sectionHits = latestMetrics.getCounterValue(sectionPrefix + DELIMITER + COUNTER_HITS);
        int sectionErrors = latestMetrics.getCounterValue(sectionPrefix + DELIMITER + COUNTER_ERRORS);
        int errorRate = 100 * sectionErrors / sectionHits;
        int sectionSize = 100 * sectionHits / totalHits;
        LOG.info("{}: {}% | {} | {}%", section, sectionSize, sectionHits, errorRate);
      });
    }
    LOG.info("-------------------------------------------------------------------------------");
  }

  @Override
  public void onFlush(IntervalMetrics lastStats) {
    report(lastStats);
  }

  /**
   * Retrieve the top section names by hits.
   *
   * @param counters    Metric counters
   * @param maxSections Number of sections to keep
   * @return List of maxSections section names ranked by traffic
   */
  List<String> getSortedSectionMetricsByHits(Map<String, Integer> counters, int maxSections) {
    if (counters == null || counters.isEmpty()) {
      return new ArrayList<>();
    }
    return counters.entrySet().stream().filter(entry -> entry.getKey().startsWith(HttpLogMetrics.COUNTER_SECTION) && entry.getKey().endsWith(HttpLogMetrics.COUNTER_HITS)).sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .limit(maxSections)
        .map(entry -> HttpLogMetrics.getSectionNameFromCounter(entry.getKey()))
        .collect(Collectors.toList());
  }

}
