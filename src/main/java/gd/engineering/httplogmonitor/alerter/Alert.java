package gd.engineering.httplogmonitor.alerter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

/**
 * Metric Alert representation.
 * Each alert has a state (alerting),
 * an associated value (value) computed by the computeAlertValue method on a time window (either alertWindowInMs or recoveryWindowInMs) depending on the alert state,
 * an evaluation method on this value (evaluate) which can compare the value to the metric threshold
 * and a message stating the details of the alert.
 */
abstract class Alert {

  private static final DateTimeFormatter ALERT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm:ss");
  private boolean alerting;
  private long lastCheck;
  private int value;
  private int threshold;
  private int alertWindowInMs;
  private int recoveryWindowInMs;

  /**
   * Build a new alert from the provided threshold, evaluation window and recovery window
   *
   * @param threshold          Threshold value
   * @param alertWindowInMs    Evaluation window in milliseconds
   * @param recoveryWindowInMs Recovery window in milliseconds
   */
  Alert(int threshold, int alertWindowInMs, int recoveryWindowInMs) {
    this.threshold = threshold;
    this.alertWindowInMs = alertWindowInMs;
    this.recoveryWindowInMs = recoveryWindowInMs;
  }

  /**
   * Compute an alert value using the provided store between nowMs and nowMs-evaluationWindowMs
   *
   * @param metricsStore       Metrics store instance
   * @param nowMs              Upper bound of the evaluation window in milliseconds
   * @param evaluationWindowMs Duration of the evaluation window in milliseconds
   * @return The current value associated to metric monitored by this alert
   */
  abstract int computeAlertValue(MetricsStore metricsStore, long nowMs, long evaluationWindowMs);

  /**
   * Evaluate the provided value against the alert rule
   *
   * @param alertValue Alert value
   * @return True if the value triggers the alert, false otherwise
   */
  abstract boolean evaluate(int alertValue);

  /**
   * Retrieve the alert message detailing this alert
   *
   * @return Alert message
   */
  abstract String getMessage();

  /**
   * Retrieve the recovert message for this alert
   *
   * @return Recovery message
   */
  String getRecoveryMessage() {
    return "Recovery - " + getMessage();
  }

  /**
   * Utility method converting milliseconds to a more human readable format
   *
   * @return Time formatted to dd/MMM/yyyy HH:mm:ss
   */
  String getLastCheckHumanReadable() {
    return Instant.ofEpochMilli(getLastCheck()).atZone(ZoneId.systemDefault()).toLocalDateTime().format(ALERT_DATETIME_FORMAT);
  }

  public boolean isAlerting() {
    return alerting;
  }

  public void setAlerting(boolean alerting) {
    this.alerting = alerting;
  }

  public long getLastCheck() {
    return lastCheck;
  }

  public void setLastCheck(long lastCheck) {
    this.lastCheck = lastCheck;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public int getAlertWindowInMs() {
    return alertWindowInMs;
  }

  public int getRecoveryWindowInMs() {
    return recoveryWindowInMs;
  }

  public int getThreshold() {
    return threshold;
  }
}
