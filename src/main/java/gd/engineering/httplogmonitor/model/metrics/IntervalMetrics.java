package gd.engineering.httplogmonitor.model.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO for metrics recorded during a time interval.
 * The metrics supported are counters and rates and stored as string separated with colons.
 * E.g:
 * section.GET/api.hits = 1
 * errors = 2
 * hits = 4
 * <p>
 * The time interval is represented by two long as milliseconds for the start and end recording time. Those timings are managed by the {@link gd.engineering.httplogmonitor.aggregator.BlockingStatsAggregator}
 */
public class IntervalMetrics {
  private long startTime;
  private long endTime;
  private Map<String, Integer> counters;
  private Map<String, Integer> rates;

  public IntervalMetrics() {
    counters = new HashMap<>();
    rates = new HashMap<>();
  }

  /**
   * Increment the provided counter by one
   *
   * @param counter Counter to be incremented
   */
  public void incr(String counter) {
    incrBy(counter, 1);
  }

  /**
   * Increment the provided counter by value
   *
   * @param counter Counter to be incremented
   * @param value   Value to increment the counter
   */
  public void incrBy(String counter, int value) {
    counters.put(counter, counters.getOrDefault(counter, 0) + value);
  }

  /**
   * Store the rate value
   *
   * @param rate Rate name
   * @param l    Rate value
   */
  public void putRate(String rate, int l) {
    rates.put(rate, l);
  }

  /**
   * Retrieve the provided counter value or 0 if not found
   *
   * @param counterName Counter name
   * @return Counter value or 0 if the counter name does not exist
   */
  public int getCounterValue(String counterName) {
    return counters.getOrDefault(counterName, 0);
  }

  /**
   * Retrieve the provided rate value or 0 if not found
   *
   * @param rateName Rate name
   * @return Rate value or 0 if the rate name does not exist
   */
  public int getRateValue(String rateName) {
    return rates.getOrDefault(rateName, 0);
  }

  public Map<String, Integer> getCounters() {
    return counters;
  }

  public Map<String, Integer> getRates() {
    return rates;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }


}
