package gd.engineering.httplogmonitor.model.metrics;

import org.junit.Assert;
import org.junit.Test;

public class InMemoryMetricsStoreTest {

  @Test
  public void testCurrentCounterValue() {
    InMemoryMetricsStore store = buildStore();
    Assert.assertEquals(30, store.getTotalSumCounterValue("test"));
    Assert.assertEquals(40, store.getTotalSumCounterValue("test2"));
    Assert.assertEquals(30, store.getTotalSumCounterValue("test3"));
    Assert.assertEquals(0, store.getTotalSumCounterValue("test4"));
  }

  @Test
  public void testAverageCounterValue() {
    InMemoryMetricsStore store = buildStore();
    int value = store.getAverageCounterValue("test", 5L, 3L);
    Assert.assertEquals(10, value);
    value = store.getAverageCounterValue("test", 5L, 4L);
    Assert.assertEquals(15, value);
    value = store.getAverageCounterValue("test", 5L, 1L);
    Assert.assertEquals(0, value);
    value = store.getAverageCounterValue("test2", 3L, 1L);
    Assert.assertEquals(0, value);
    value = store.getAverageCounterValue("test2", 3L, 2L);
    Assert.assertEquals(20, value);
    value = store.getAverageCounterValue("test3", 3L, 1L);
    Assert.assertEquals(30, value);
    value = store.getAverageCounterValue("test3", 3L, 2L);
    Assert.assertEquals(15, value);
  }

  @Test
  public void testSumCounterValue() {
    InMemoryMetricsStore store = buildStore();
    int value = store.getSumCounterValue("test", 5L, 1L);
    Assert.assertEquals(0, value);
    value = store.getSumCounterValue("test", 5L, 3L);
    Assert.assertEquals(10, value);
    value = store.getSumCounterValue("test", 5L, 4L);
    Assert.assertEquals(30, value);
    value = store.getSumCounterValue("test2", 5L, 3L);
    Assert.assertEquals(0, value);
    value = store.getSumCounterValue("test2", 5L, 4L);
    Assert.assertEquals(40, value);
    value = store.getSumCounterValue("test2", 1L, 2L);
    Assert.assertEquals(0, value);
  }

  @Test
  public void testAverageRateValue() {
    InMemoryMetricsStore store = buildStore();
    int value = store.getAverageRateValue("test", 5L, 1L);
    Assert.assertEquals(0, value);
    value = store.getAverageRateValue("test", 5L, 3L);
    Assert.assertEquals(1, value);
    value = store.getAverageRateValue("test", 5L, 4L);
    Assert.assertEquals(1, value);
    value = store.getAverageRateValue("test2", 5L, 3L);
    Assert.assertEquals(0, value);
    value = store.getAverageRateValue("test", 1L, 2L);
    Assert.assertEquals(0, value);
  }

  @Test
  public void testEmptyStore() {
    InMemoryMetricsStore store = new InMemoryMetricsStore();
    Assert.assertEquals(0, store.getAverageCounterValue("test", 1L, 1L));
    Assert.assertEquals(0, store.getSumCounterValue("test", 1L, 1L));
    Assert.assertEquals(0, store.getAverageRateValue("test", 1L, 1L));
    Assert.assertEquals(0, store.getTotalSumCounterValue("test"));
  }

  private InMemoryMetricsStore buildStore() {
    IntervalMetrics s1 = new IntervalMetrics();
    s1.incrBy("test", 20);
    s1.incrBy("test2", 40);
    s1.putRate("test", 2);
    s1.putRate("test2", 4);
    s1.setStartTime(1L);
    IntervalMetrics s2 = new IntervalMetrics();
    s2.incrBy("test", 10);
    s2.incrBy("test3", 30);
    s2.putRate("test", 1);
    s2.putRate("test3", 3);
    s2.setStartTime(2L);
    InMemoryMetricsStore store = new InMemoryMetricsStore();
    store.add(s1);
    store.add(s2);
    return store;
  }

}
