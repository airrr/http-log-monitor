package gd.engineering.httplogmonitor.alerter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to build alerts from java properties.
 * Based upon reflection to instantiate the alerts using the default {@link gd.engineering.httplogmonitor.alerter.Alert} constructor
 */
public class AlertBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(AlertBuilder.class);
  private static final String ALERTS_PROPERTY = "alert.list";
  private static final String ALERTS_PROPERTY_SEPARATOR = ",";
  private static final String ALERT_PREFIX_PROPERTY = "alert.";
  private static final String ALERT_THRESHOLD_SUFFIX = ".threshold";
  private static final String ALERT_WINDOW_ALERT_SUFFIX = ".window.alert.ms";
  private static final String ALERT_WINDOW_RECOVERY_SUFFIX = ".window.recovery.ms";

  private static final Map<String, Class<? extends Alert>> SUPPORTED_ALERTS = new HashMap<String, Class<? extends Alert>>() {{
    put("traffic", HighTrafficAlert.class);
    put("nodata", NoTrafficAlert.class);
  }};

  /**
   * Instantiate a list of alerts from a Properties object.
   * This requires four properties:
   * alert.list defines the list of active alerts
   * alert.<alertname>.threshold, alert.<alertname>.window.alert.ms and alert.<alertname>.window.recovery ms are the required parameters
   * <p>
   * e.g:
   * <p>
   * alert.list=traffic
   * alert.traffic.threshold=10
   * alert.traffic.window.alert.ms=120000
   * alert.traffic.window.recovery.ms=120000
   * <p>
   * will instantiate a new HighTrafficAlert
   *
   * @param properties Application properties
   * @return List of alert instances
   */
  public static List<Alert> buildAlertsFromProperties(Properties properties) {
    List<Alert> alerts = new ArrayList<>();
    String alertListStr = properties.getProperty(ALERTS_PROPERTY);
    if (StringUtils.isEmpty(alertListStr)) {
      return alerts;
    }
    String[] alertList = alertListStr.split(ALERTS_PROPERTY_SEPARATOR);
    for (String alertName : alertList) {
      try {
        int threshold = Integer.parseInt(properties.getProperty(ALERT_PREFIX_PROPERTY + alertName + ALERT_THRESHOLD_SUFFIX));
        int alertWindowMs = Integer.parseInt(properties.getProperty(ALERT_PREFIX_PROPERTY + alertName + ALERT_WINDOW_ALERT_SUFFIX));
        int alertRecoveryMs = Integer.parseInt(properties.getProperty(ALERT_PREFIX_PROPERTY + alertName + ALERT_WINDOW_RECOVERY_SUFFIX));
        Alert alert = buildAlert(alertName, threshold, alertWindowMs, alertRecoveryMs);
        if (alert != null) {
          alerts.add(alert);
        }
      } catch (NumberFormatException ex) {
        LOG.warn("Invalid parameter value for alert {}", alertName);
      } catch (IllegalArgumentException iae) {
        LOG.warn("Invalid alert definition {}", alertName, iae);
      }
    }
    return alerts;
  }

  /**
   * Lookup the alert by name and instantiate it with the provided parameters
   *
   * @param name             Alert name (traffic/nodata)
   * @param threshold        Alert value threshold
   * @param alertWindowMs    Alert evaluation window in milliseconds
   * @param recoveryWindowMs Alert recovery window in milliseconds
   * @return An instance of the alert
   * @throws IllegalArgumentException If no alerts exist with the provided name
   */
  static Alert buildAlert(String name, int threshold, int alertWindowMs, int recoveryWindowMs) {
    if (!SUPPORTED_ALERTS.containsKey(name)) {
      throw new IllegalArgumentException("No alerts supported named " + name);
    }
    try {
      return SUPPORTED_ALERTS.get(name).getConstructor(int.class, int.class, int.class).newInstance(threshold, alertWindowMs, recoveryWindowMs);
    } catch (Exception e) {
      LOG.error("Cannot instantiate alert {}", name);
    }
    return null;
  }

  private AlertBuilder() {
  }
}
