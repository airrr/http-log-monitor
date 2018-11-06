package gd.engineering.httplogmonitor.alerter;

import org.junit.Assert;
import org.junit.Test;

import gd.engineering.httplogmonitor.model.metrics.InMemoryMetricsStore;
import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

public class HighTrafficAlertTest {

  @Test
  public void testAlertEvaluate() {
    HighTrafficAlert alert = new HighTrafficAlert(1, 1, 1);
    Assert.assertTrue(alert.evaluate(2));
    Assert.assertFalse(alert.evaluate(1));
    Assert.assertFalse(alert.evaluate(0));
    Assert.assertFalse(alert.evaluate(-1));
  }

  @Test
  public void testAlertComputeMetric() {
    HighTrafficAlert alert = new HighTrafficAlert(1, 1, 1);
    MetricsStore store = buildStore();
    int requestsPerSecond = alert.computeAlertValue(store, 5L, 5L);
    Assert.assertEquals(0, requestsPerSecond);
    requestsPerSecond = alert.computeAlertValue(store, 5L, 1L);
    Assert.assertEquals(0, requestsPerSecond);
    requestsPerSecond = alert.computeAlertValue(store, 5L, 3L);
    Assert.assertEquals(1, requestsPerSecond);
  }

  private InMemoryMetricsStore buildStore() {
    IntervalMetrics s1 = new IntervalMetrics();
    s1.putRate("hits", 2);
    s1.setStartTime(1L);
    IntervalMetrics s2 = new IntervalMetrics();
    s2.putRate("hits", 3);
    s2.setStartTime(2L);
    IntervalMetrics s3 = new IntervalMetrics();
    s3.putRate("hits", 0);
    s3.setStartTime(3L);
    InMemoryMetricsStore store = new InMemoryMetricsStore();
    store.add(s1);
    store.add(s2);
    store.add(s3);
    return store;
  }
}
