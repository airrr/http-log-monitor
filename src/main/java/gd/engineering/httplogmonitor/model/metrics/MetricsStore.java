package gd.engineering.httplogmonitor.model.metrics;

/**
 * Abstraction of a metrics store managing counters and rates.
 * <p>
 * The store is able to add newly flushed metrics and compute sum and averages on counters and rates.
 */
public interface MetricsStore {
  /**
   * Add newly flushed metrics to the store
   *
   * @param stats New metrics to be added
   * @return True if the metrics have been correctly stored, False otherwise
   */
  boolean add(IntervalMetrics stats);

  /**
   * Return the sum of all values of the given counter
   *
   * @param counterName Counter name
   * @return Counter aggregated value since the counter has emitted data for the first time
   */
  int getTotalSumCounterValue(String counterName);

  /**
   * Return the average value of the given counter from nowMs to nowMs-timeFromNowMs
   *
   * @param counterName   Counter name
   * @param nowMs         Upper time bound for the counter values in milliseconds
   * @param timeFromNowMs Time delta from now in milliseconds
   * @return The average value of the given counter in the provided time window
   */
  int getAverageCounterValue(String counterName, long nowMs, long timeFromNowMs);

  /**
   * Return the sum of the given counter from nowMs to nowMs-timeFromNowMs
   *
   * @param counterName   Counter name
   * @param nowMs         Upper time bound for the counter values in milliseconds
   * @param timeFromNowMs Time delta from now in milliseconds
   * @return The sum of the given counter in the provided time window
   */
  int getSumCounterValue(String counterName, long nowMs, long timeFromNowMs);

  /**
   * Return the average value of the given rate from nowMs to nowMs-timeFromNowMs
   *
   * @param rateName      Rate name
   * @param nowMs         Upper time bound for the rate values in milliseconds
   * @param timeFromNowMs Time delta from now in milliseconds
   * @return The average value of the given rate in the provided time window
   */
  int getAverageRateValue(String rateName, long nowMs, long timeFromNowMs);

  /**
   * Return the latest flushed metrics
   *
   * @return Latest flushed metrics
   */
  IntervalMetrics getLatestMetrics();
}
