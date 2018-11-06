package gd.engineering.httplogmonitor.alerter;

import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class AlertBuilderTest {

  @Test
  public void testBuildHighTrafficAlertFromValidName() {
    Alert alert = AlertBuilder.buildAlert("traffic", 1, 2, 3);
    Assert.assertEquals(alert.getClass(), HighTrafficAlert.class);
    Assert.assertEquals(1, alert.getThreshold());
    Assert.assertEquals(2, alert.getAlertWindowInMs());
    Assert.assertEquals(3, alert.getRecoveryWindowInMs());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuildHighTrafficAlertFromInvalidName() {
    AlertBuilder.buildAlert("lol", 1, 2, 3);
  }

  @Test
  public void testBuildHighTrafficFromProperties() {
    Properties properties = new Properties();
    properties.put("alert.list", "traffic");
    properties.put("alert.traffic.threshold", "1");
    properties.put("alert.traffic.window.alert.ms", "2");
    properties.put("alert.traffic.window.recovery.ms", "3");
    List<Alert> alerts = AlertBuilder.buildAlertsFromProperties(properties);
    Assert.assertEquals(1, alerts.size());
    Alert alert = alerts.get(0);
    Assert.assertEquals(alert.getClass(), HighTrafficAlert.class);
    Assert.assertEquals(1, alert.getThreshold());
    Assert.assertEquals(2, alert.getAlertWindowInMs());
    Assert.assertEquals(3, alert.getRecoveryWindowInMs());
  }

  @Test
  public void testBuildFromEmptyProperties() {
    Properties properties = new Properties();
    List<Alert> alerts = AlertBuilder.buildAlertsFromProperties(properties);
    Assert.assertEquals(0, alerts.size());
  }

  @Test
  public void testBuildHighTrafficFromUnknownAlert() {
    Properties properties = new Properties();
    properties.put("alert.list", "bob");
    properties.put("alert.bob.threshold", "1");
    properties.put("alert.bob.window.alert.ms", "2");
    properties.put("alert.bob.window.recovery.ms", "3");
    List<Alert> alerts = AlertBuilder.buildAlertsFromProperties(properties);
    Assert.assertEquals(0, alerts.size());
  }

  @Test
  public void testBuildHighTrafficFromInvalidParameter() {
    Properties properties = new Properties();
    properties.put("alert.list", "traffic");
    properties.put("alert.traffic.threshold", "bob");
    properties.put("alert.traffic.window.alert.ms", "2");
    properties.put("alert.traffic.window.recovery.ms", "3");
    List<Alert> alerts = AlertBuilder.buildAlertsFromProperties(properties);
    Assert.assertEquals(0, alerts.size());
  }

}
