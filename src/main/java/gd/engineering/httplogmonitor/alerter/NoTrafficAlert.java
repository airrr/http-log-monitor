package gd.engineering.httplogmonitor.alerter;

import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

import static gd.engineering.httplogmonitor.model.HttpLogMetrics.COUNTER_HITS;

/**
 * Monitors windows of no traffic described as number of hits
 */
public class NoTrafficAlert extends Alert {

  public NoTrafficAlert(int threshold, int alertWindowInMs, int recoveryWindowInMs) {
    super(threshold, alertWindowInMs, recoveryWindowInMs);
  }

  @Override
  public int computeAlertValue(MetricsStore metricsStore, long nowMs, long evaluationWindowMs) {
    return metricsStore.getSumCounterValue(COUNTER_HITS, nowMs, evaluationWindowMs);
  }

  @Override
  public boolean evaluate(int alertValue) {
    return alertValue > getThreshold();
  }

  @Override
  public String getMessage() {
    return String.format("No traffic recorded for the past minute, triggered at %s", getLastCheckHumanReadable());
  }

}
