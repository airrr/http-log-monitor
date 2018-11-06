package gd.engineering.httplogmonitor.alerter;

import org.junit.Assert;
import org.junit.Test;

import gd.engineering.httplogmonitor.model.metrics.InMemoryMetricsStore;
import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

public class NoTrafficAlertTest {

  @Test
  public void testAlertEvaluate() {
    NoTrafficAlert alert = new NoTrafficAlert(0, 1, 1);
    Assert.assertTrue(alert.evaluate(2));
    Assert.assertTrue(alert.evaluate(1));
    Assert.assertFalse(alert.evaluate(0));
    Assert.assertFalse(alert.evaluate(-1));
  }

  @Test
  public void testAlertComputeMetric() {
    NoTrafficAlert alert = new NoTrafficAlert(0, 1, 1);
    MetricsStore store = buildStore();
    int hits = alert.computeAlertValue(store, 2L, 1L);
    Assert.assertEquals(1, hits);
    hits = alert.computeAlertValue(store, 5L, 3L);
    Assert.assertEquals(0, hits);
  }

  private InMemoryMetricsStore buildStore() {
    IntervalMetrics s1 = new IntervalMetrics();
    s1.incr("hits");
    s1.setStartTime(1L);
    IntervalMetrics s2 = new IntervalMetrics();
    s2.setStartTime(2L);
    InMemoryMetricsStore store = new InMemoryMetricsStore();
    store.add(s1);
    store.add(s2);
    return store;
  }
}
