package gd.engineering.httplogmonitor.reporter;

import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsFlushListener;

/**
 * Report on newly flushed metrics
 */
public interface MetricsReporter extends MetricsFlushListener {

  /**
   * Report on the last (or existing) metrics
   *
   * @param latestMetrics Latest flushed metrics by the aggregator
   */
  void report(IntervalMetrics latestMetrics);
}
