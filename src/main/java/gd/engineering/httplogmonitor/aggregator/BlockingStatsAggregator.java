package gd.engineering.httplogmonitor.aggregator;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import gd.engineering.httplogmonitor.model.HttpLogLine;
import gd.engineering.httplogmonitor.model.HttpLogMetrics;
import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsFlushListener;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

import static gd.engineering.httplogmonitor.model.HttpLogMetrics.COUNTER_ERRORS;
import static gd.engineering.httplogmonitor.model.HttpLogMetrics.COUNTER_HITS;
import static gd.engineering.httplogmonitor.model.HttpLogMetrics.COUNTER_SUCCESSES;
import static gd.engineering.httplogmonitor.model.HttpLogMetrics.DELIMITER;

/**
 * Log to metric aggregator.
 * The aggregator regularly polls the log queue for any newly parsed line and increments its current metrics.
 * It periodically flushes those metrics to the metric store and send an event to all its listeners.
 * While flushing, rates are computed based upon the counter values. Those rates are per second.
 */
public class BlockingStatsAggregator implements Runnable {

  private boolean running = true;
  private BlockingQueue<HttpLogLine> logQueue;
  private long pollTimeoutMs;
  private long flushIntervalMs;
  private MetricsStore metricsStore;
  private List<MetricsFlushListener> flushListeners;
  private Clock clock;

  /**
   * Build a new aggregator listening to the provided log queue and flushing in the metrics store.
   * The log queue poll frequency is provided by pollTimeoutMs and the flush frequency is provided by flushIntervalMs.
   * The poll frequency has to be lower than the flush interval
   *
   * @param logQueue        Blocking log queue
   * @param metricsStore    Metrics store
   * @param clock           System clock
   * @param pollTimeoutMs   Log queue poll frequency in milliseconds
   * @param flushIntervalMs Aggregator flush frequency in milliseconds
   * @throws IllegalArgumentException if poll timeout is greater than flush interval
   */
  public BlockingStatsAggregator(BlockingQueue<HttpLogLine> logQueue, MetricsStore metricsStore, Clock clock, long pollTimeoutMs, long flushIntervalMs) {
    if (pollTimeoutMs > flushIntervalMs) {
      throw new IllegalArgumentException("Poll timeout has to be lower than the flush interval otherwise metrics will not be accurate");
    }
    this.logQueue = logQueue;
    this.pollTimeoutMs = pollTimeoutMs;
    this.flushIntervalMs = flushIntervalMs;
    this.metricsStore = metricsStore;
    this.flushListeners = new ArrayList<>();
    this.clock = clock;
  }

  @Override
  public void run() {
    long lastFlushTime = clock.millis();
    IntervalMetrics stats = new IntervalMetrics();
    while (running) {
      try {
        long currentTime = clock.millis();
        if (currentTime - lastFlushTime > flushIntervalMs) {
          flush(stats, lastFlushTime, currentTime, TimeUnit.SECONDS);
          stats = new IntervalMetrics();
          lastFlushTime = clock.millis();
        }
        HttpLogLine logLine = logQueue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
        if (logLine != null) {
          incrementMetrics(stats, logLine);
        }
      } catch (InterruptedException e) {
        running = false;
      }
    }
  }

  /**
   * Flush the aggregated stats to the metrics store.
   * The method computes rates and notifies the listeners of the new metrics being available
   *
   * @param metrics           Metrics to be flushed
   * @param intervalStartTime Start of metrics being recorded in milliseconds
   * @param intervalEndTime   End of metric being recorded in milliseconds
   * @param rateUnits         Unit used to compute rates (default second)
   */
  void flush(IntervalMetrics metrics, long intervalStartTime, long intervalEndTime, TimeUnit rateUnits) {
    metrics.setStartTime(intervalStartTime);
    metrics.setEndTime(intervalEndTime);
    computeRates(metrics, rateUnits);
    if (metricsStore.add(metrics)) {
      flushListeners.forEach(fl -> fl.onFlush(metrics));
    }
  }

  /**
   * Add a new listener to flush events
   *
   * @param listener Flush listener
   */
  public void addStatsFlushListener(MetricsFlushListener listener) {
    flushListeners.add(listener);
  }

  /**
   * Compute the rates on the provided metrics counters
   *
   * @param metrics  Metrics to compute rates from
   * @param rateUnit Rates time unit
   */
  private void computeRates(IntervalMetrics metrics, TimeUnit rateUnit) {
    metrics.getCounters().forEach((counter, value) -> metrics.putRate(counter, value / (int) (rateUnit.convert(flushIntervalMs, TimeUnit.MILLISECONDS))));
  }

  /**
   * Increment the currently recorded metrics based upon the current log line
   * Hits, errors, success overall and per section are recorded
   *
   * @param metrics Current interval metric
   * @param logLine Log line to be processed
   */
  private void incrementMetrics(IntervalMetrics metrics, HttpLogLine logLine) {
    String sectionPrefix = HttpLogMetrics.getSectionCounterPrefix(logLine.getHttpMethod() + logLine.getSection());
    metrics.incr(COUNTER_HITS);
    int statusCode = logLine.getStatusCode();
    if (statusCode >= 400) {
      metrics.incr(sectionPrefix + DELIMITER + COUNTER_ERRORS);
      metrics.incr(COUNTER_ERRORS);
    } else {
      metrics.incr(sectionPrefix + DELIMITER + COUNTER_SUCCESSES);
      metrics.incr(COUNTER_SUCCESSES);
    }
    metrics.incr(sectionPrefix + DELIMITER + COUNTER_HITS);
  }
}
