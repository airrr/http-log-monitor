package gd.engineering.httplogmonitor.model.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * In memory implementation of the metrics store.
 * Each flushed metrics is stored in a TreeMap ordered by startTime.
 * Counters are aggregated in the counters map for global values
 * <p> Visual representation:
 * Values |1|1|3|2|
 * Time   1 2 3 4 5
 * Map:
 * 1-1
 * 2-1
 * 3-3
 * 4-2
 * </p>
 * To manage the alerts, each average and sum method looks for a full evaluation window by
 * looking if there is an entry in the map before the lower bound of the alert evaluation window
 */
public class InMemoryMetricsStore implements MetricsStore {

  private NavigableMap<Long, IntervalMetrics> historicalMetrics;
  private Map<String, Integer> counters;

  public InMemoryMetricsStore() {
    this.historicalMetrics = new TreeMap<>();
    this.counters = new HashMap<>();
  }

  @Override
  public boolean add(IntervalMetrics intervalMetrics) {
    this.historicalMetrics.put(intervalMetrics.getStartTime(), intervalMetrics);
    Map<String, Integer> intervalCounters = intervalMetrics.getCounters();
    intervalCounters.forEach((counter, value) ->
        this.counters.put(counter, value + this.counters.getOrDefault(counter, 0))
    );
    return true;
  }

  @Override
  public int getTotalSumCounterValue(String counterName) {
    return counters.getOrDefault(counterName, 0);
  }

  @Override
  public int getAverageRateValue(String rateName, long nowMs, long timeFromNowMs) {
    if (historicalMetrics.isEmpty()) {
      return 0;
    }
    long lowerBound = nowMs - timeFromNowMs;
    if (lowerBound < historicalMetrics.firstEntry().getKey()) {
      return 0;
    }
    OptionalDouble average = getIntervalStartTimeBetween(nowMs, lowerBound)
        .mapToInt(intervalStartTime -> historicalMetrics.get(intervalStartTime).getRates().getOrDefault(rateName, 0))
        .average();
    return (int) average.orElse(0.);
  }

  @Override
  public int getAverageCounterValue(String counterName, long nowMs, long timeFromNowMs) {
    if (historicalMetrics.isEmpty() || timeFromNowMs < historicalMetrics.firstEntry().getKey()) {
      return 0;
    }
    long lowerBound = nowMs - timeFromNowMs;
    if (lowerBound < historicalMetrics.firstEntry().getKey()) {
      return 0;
    }
    OptionalDouble average = getIntervalStartTimeBetween(nowMs, lowerBound)
        .mapToInt(intervalStartTime -> historicalMetrics.get(intervalStartTime).getCounters().getOrDefault(counterName, 0))
        .average();
    return (int) average.orElse(0.);
  }

  @Override
  public int getSumCounterValue(String counterName, long nowMs, long timeFromNowMs) {
    if (historicalMetrics.isEmpty() || timeFromNowMs < historicalMetrics.firstEntry().getKey()) {
      return 0;
    }
    long lowerBound = nowMs - timeFromNowMs;
    if (lowerBound < historicalMetrics.firstEntry().getKey()) {
      return 0;
    }
    return getIntervalStartTimeBetween(nowMs, lowerBound)
        .mapToInt(intervalStartTime -> historicalMetrics.get(intervalStartTime).getCounters().getOrDefault(counterName, 0))
        .sum();
  }

  @Override
  public IntervalMetrics getLatestMetrics() {
    return historicalMetrics.lastEntry().getValue();
  }

  /**
   * Look for all the metrics recorded between lowerBound inclusive and upperBound exclusive.
   * UpperBound is exclusive as the time stored are the metrics interval start time (the metrics are not recorded at that time)
   *
   * @param upperBoundMs Exclusive time upper bound
   * @param lowerBoundMs Inclusive time lower bound
   * @return Stream of interval start time found between the lower and upper bounds
   */
  private Stream<Long> getIntervalStartTimeBetween(long upperBoundMs, long lowerBoundMs) {
    return historicalMetrics.descendingKeySet().stream().filter(intervalStartTime -> intervalStartTime >= lowerBoundMs && intervalStartTime < upperBoundMs);
  }
}
