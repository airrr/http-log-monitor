package gd.engineering.httplogmonitor.alerter;

import java.time.Clock;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import gd.engineering.httplogmonitor.model.metrics.InMemoryMetricsStore;
import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

public class AlertTaskTest {

  @Test
  public void updateAlertHighTrafficTestNotAlerting() {
    HighTrafficAlert alert = Mockito.spy(new HighTrafficAlert(1, 2, 2));
    MetricsStore store = buildStore();
    AlertTask task = new AlertTask(alert, store.getLatestMetrics(), store, Clock.systemDefaultZone());
    task.updateAlert(alert, store, 2L, 2L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(0, alert.getValue());
    Assert.assertEquals(2L, alert.getLastCheck());
    task.updateAlert(alert, store, 3L, 3L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(0, alert.getValue());
    Assert.assertEquals(3L, alert.getLastCheck());
    task.updateAlert(alert, store, 4L, 4L);
    Assert.assertTrue(alert.isAlerting());
    Assert.assertEquals(2, alert.getValue());
    Assert.assertEquals(4L, alert.getLastCheck());
    Mockito.verify(alert).getMessage();
    task.updateAlert(alert, store, 5L, 5L);
    Assert.assertTrue(alert.isAlerting());
    Assert.assertEquals(2, alert.getValue());
    Assert.assertEquals(5L, alert.getLastCheck());
    task.updateAlert(alert, store, 6L, 6L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(1, alert.getValue());
    Assert.assertEquals(6L, alert.getLastCheck());
    Mockito.verify(alert).getRecoveryMessage();
    task.updateAlert(alert, store, 7L, 7L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(0, alert.getValue());
    Assert.assertEquals(7L, alert.getLastCheck());
  }

  @Test
  public void updateAlertHighTrafficTestAlreadyAlerting() {
    HighTrafficAlert alert = Mockito.spy(new HighTrafficAlert(1, 2, 2));
    alert.setAlerting(true);
    MetricsStore store = buildStore();
    AlertTask task = new AlertTask(alert, store.getLatestMetrics(), store, Clock.systemDefaultZone());
    task.updateAlert(alert, store, 6L, 6L);
    Mockito.verify(alert).getRecoveryMessage();
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(1, alert.getValue());
    Assert.assertEquals(6L, alert.getLastCheck());
    task.updateAlert(alert, store, 7L, 7L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(0, alert.getValue());
    Assert.assertEquals(7L, alert.getLastCheck());
  }

  @Test
  public void updateAlertHighTrafficTestNotAlertingDifferentWindow() {
    HighTrafficAlert alert = Mockito.spy(new HighTrafficAlert(1, 2, 1));
    MetricsStore store = buildStore();
    AlertTask task = new AlertTask(alert, store.getLatestMetrics(), store, Clock.systemDefaultZone());
    task.updateAlert(alert, store, 2L, 2L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(0, alert.getValue());
    Assert.assertEquals(2L, alert.getLastCheck());
    task.updateAlert(alert, store, 3L, 3L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(0, alert.getValue());
    Assert.assertEquals(3L, alert.getLastCheck());
    task.updateAlert(alert, store, 4L, 4L);
    Assert.assertTrue(alert.isAlerting());
    Assert.assertEquals(2, alert.getValue());
    Assert.assertEquals(4L, alert.getLastCheck());
    Mockito.verify(alert).getMessage();
    task.updateAlert(alert, store, 5L, 5L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(1, alert.getValue());
    Assert.assertEquals(5L, alert.getLastCheck());
    Mockito.verify(alert).getRecoveryMessage();
    task.updateAlert(alert, store, 6L, 6L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(1, alert.getValue());
    Assert.assertEquals(6L, alert.getLastCheck());
    task.updateAlert(alert, store, 7L, 7L);
    Assert.assertFalse(alert.isAlerting());
    Assert.assertEquals(0, alert.getValue());
    Assert.assertEquals(7L, alert.getLastCheck());
  }

  private InMemoryMetricsStore buildStore() {
    IntervalMetrics s1 = new IntervalMetrics();
    s1.putRate("hits", 2);
    s1.setStartTime(2L);
    IntervalMetrics s2 = new IntervalMetrics();
    s2.putRate("hits", 3);
    s2.setStartTime(3L);
    IntervalMetrics s3 = new IntervalMetrics();
    s3.putRate("hits", 1);
    s3.setStartTime(4L);
    IntervalMetrics s4 = new IntervalMetrics();
    s4.putRate("hits", 1);
    s4.setStartTime(5L);
    IntervalMetrics s5 = new IntervalMetrics();
    s5.putRate("hits", 0);
    s5.setStartTime(6L);
    InMemoryMetricsStore store = new InMemoryMetricsStore();
    store.add(s1);
    store.add(s2);
    store.add(s3);
    store.add(s4);
    store.add(s5);
    return store;
  }

}
