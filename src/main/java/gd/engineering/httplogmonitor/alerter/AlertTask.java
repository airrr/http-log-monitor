package gd.engineering.httplogmonitor.alerter;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

/**
 * Task executed on an alert.
 * Based upon the alert state and the evaluation of the new alert value,
 * displays either the alert or recovery message and update the value and alert state
 * Alerts messages are logged into a specific html logger (alerts-log)
 */
public class AlertTask implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger("alerts-log");
  private Alert alert;
  private MetricsStore metricsStore;
  private IntervalMetrics latestStats;
  private Clock clock;

  /**
   * Build a new alert task on the provided alert
   *
   * @param alert        Alert to be run
   * @param latestStats  Latest flused stats
   * @param metricsStore Metrics store
   * @param clock        System clock
   */
  AlertTask(Alert alert, IntervalMetrics latestStats, MetricsStore metricsStore, Clock clock) {
    this.alert = alert;
    this.metricsStore = metricsStore;
    this.latestStats = latestStats;
    this.clock = clock;
  }

  @Override
  public void run() {
    updateAlert(this.alert, this.metricsStore, latestStats.getEndTime(), clock.millis());
  }

  /**
   * Update the alert state based upon the value and the evaluation of this new value.
   * Depending of the existing state and new state, displays the alert or recovery message
   *
   * @param alert              Alert to run
   * @param store              Metrics store
   * @param latestCycleEndTime Latest metrics flush end time
   * @param currentTimeMs      Current system time
   */
  void updateAlert(Alert alert, MetricsStore store, long latestCycleEndTime, long currentTimeMs) {
    boolean isAlertCurrentlyActive = alert.isAlerting();
    int currentValue = alert.computeAlertValue(store, currentTimeMs, isAlertCurrentlyActive ? alert.getRecoveryWindowInMs() : alert.getAlertWindowInMs());
    alert.setValue(currentValue);
    alert.setLastCheck(latestCycleEndTime);
    boolean isAlerting = alert.evaluate(currentValue);
    if (isAlerting && !isAlertCurrentlyActive) {
      LOG.info(alert.getMessage());
    }
    if (!isAlerting && isAlertCurrentlyActive) {
      LOG.info(alert.getRecoveryMessage());
    }
    alert.setAlerting(isAlerting);
  }
}
