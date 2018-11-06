package gd.engineering.httplogmonitor.reporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import gd.engineering.httplogmonitor.model.metrics.InMemoryMetricsStore;

public class HttpSectionConsoleReporterTest {

  @Test
  public void testGetSortedSectionsByHitsValid() {
    Map<String, Integer> counters = new HashMap<>();
    counters.put("hits", 30);
    counters.put("section:POST/api1:errors", 5);
    counters.put("section:POST/api1:hits", 10);
    counters.put("section:POST/api2:hits", 5);
    counters.put("section:GET/api3:hits", 1);
    counters.put("section:PUT/api4:hits", 20);
    counters.put("section:GET/api5:hits", 50);
    counters.put("section:GET/api5:successes", 42);
    HttpSectionConsoleReporter reporter = new HttpSectionConsoleReporter(new InMemoryMetricsStore(), 3);
    List<String> result = reporter.getSortedSectionMetricsByHits(counters, 3);
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("GET/api5", result.get(0));
    Assert.assertEquals("PUT/api4", result.get(1));
    Assert.assertEquals("POST/api1", result.get(2));
  }

  @Test
  public void testGetSortedSectionsByHitsValidNullEmpty() {
    HttpSectionConsoleReporter reporter = new HttpSectionConsoleReporter(new InMemoryMetricsStore(), 3);
    List<String> result = reporter.getSortedSectionMetricsByHits(null, 3);
    Assert.assertEquals(0, result.size());
    result = reporter.getSortedSectionMetricsByHits(new HashMap<>(), 3);
    Assert.assertEquals(0, result.size());
  }


}
