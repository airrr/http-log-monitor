package gd.engineering.httplogmonitor.model.metrics;

/**
 * Listener abstraction called after any flush from the {@link gd.engineering.httplogmonitor.aggregator.BlockingStatsAggregator}
 * providing the newly flushed metrics
 */
public interface MetricsFlushListener {

  /**
   * Provides the latest metrics from the aggregator flush
   *
   * @param latestMetrics Latest recorded metrics
   */
  void onFlush(IntervalMetrics latestMetrics);
}
