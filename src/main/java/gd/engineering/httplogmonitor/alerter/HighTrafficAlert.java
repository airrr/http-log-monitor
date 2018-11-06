package gd.engineering.httplogmonitor.alerter;

import gd.engineering.httplogmonitor.model.HttpLogMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

/**
 * Monitors the average traffic described as requests per seconds
 */
public class HighTrafficAlert extends Alert {

  public HighTrafficAlert(int threshold, int alertWindowInMs, int recoveryWindowInMs) {
    super(threshold, alertWindowInMs, recoveryWindowInMs);
  }

  @Override
  public int computeAlertValue(MetricsStore metricsStore, long nowMs, long evaluationWindowMs) {
    return metricsStore.getAverageRateValue(HttpLogMetrics.COUNTER_HITS, nowMs, evaluationWindowMs);
  }

  @Override
  public boolean evaluate(int alertValue) {
    return alertValue > getThreshold();
  }

  @Override
  public String getMessage() {
    return String.format("High traffic generated an alert - hits = %d RPS, triggered at %s", getValue(), getLastCheckHumanReadable());
  }

}
