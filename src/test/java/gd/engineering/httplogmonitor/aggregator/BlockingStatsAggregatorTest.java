package gd.engineering.httplogmonitor.aggregator;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import gd.engineering.httplogmonitor.model.metrics.InMemoryMetricsStore;
import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.reporter.MetricsReporter;

public class BlockingStatsAggregatorTest {

  private MetricsReporter dummyReporter = new MetricsReporter() {
    @Override
    public void report(IntervalMetrics lastStats) {
    }

    @Override
    public void onFlush(IntervalMetrics statsFlushed) {
    }
  };

  @Test
  public void testFlush() {
    InMemoryMetricsStore store = Mockito.spy(new InMemoryMetricsStore());
    BlockingStatsAggregator aggregator = new BlockingStatsAggregator(null, store, Clock.systemDefaultZone(), 500L, 1000L);
    MetricsReporter reporter = Mockito.spy(dummyReporter);
    aggregator.addStatsFlushListener(reporter);
    IntervalMetrics stats = new IntervalMetrics();
    aggregator.flush(stats, 1L, 2L, TimeUnit.SECONDS);
    Mockito.verify(store).add(stats);
    Mockito.verify(reporter).onFlush(stats);
    Assert.assertEquals(1L, stats.getStartTime());
    Assert.assertEquals(2L, stats.getEndTime());
  }

  @Test
  public void testFlushRates() {
    InMemoryMetricsStore store = Mockito.spy(new InMemoryMetricsStore());
    long flushInterval = 2L;
    BlockingStatsAggregator aggregator = new BlockingStatsAggregator(null, store, Clock.systemDefaultZone(), 1L, flushInterval);
    IntervalMetrics stats = new IntervalMetrics();
    stats.incrBy("test", 10);
    stats.incrBy("test2", 5);
    stats.incrBy("test3", 1);
    aggregator.flush(stats, 1L, 2L, TimeUnit.MILLISECONDS);
    Assert.assertEquals(5, stats.getRates().get("test").intValue());
    Assert.assertEquals(2, stats.getRates().get("test2").intValue());
    Assert.assertEquals(0, stats.getRates().get("test3").intValue());
  }

}
