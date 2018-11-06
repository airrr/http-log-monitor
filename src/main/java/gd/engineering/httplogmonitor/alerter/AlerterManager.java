package gd.engineering.httplogmonitor.alerter;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import gd.engineering.httplogmonitor.model.metrics.IntervalMetrics;
import gd.engineering.httplogmonitor.model.metrics.MetricsFlushListener;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;

/**
 * Class which manages triggering the alert on the metrics store after any new flushed metric
 */
public class AlerterManager implements MetricsFlushListener {

  private MetricsStore metricsStore;
  private List<Alert> alerts;
  private ExecutorService alertTaskPool;
  private Clock clock;

  /**
   * Build a new AlerterManager
   *
   * @param store        Metrics store
   * @param taskPoolSize Size of the executor service managing the alerts
   * @param clock        Clock provided to the alerts
   */
  public AlerterManager(MetricsStore store, int taskPoolSize, Clock clock) {
    this.metricsStore = store;
    this.clock = clock;
    this.alerts = new ArrayList<>();
    this.alertTaskPool = Executors.newFixedThreadPool(taskPoolSize);
  }

  @Override
  public void onFlush(IntervalMetrics statsFlushed) {
    for (Alert alert : alerts) {
      alertTaskPool.submit(new AlertTask(alert, statsFlushed, this.metricsStore, this.clock));
    }
  }

  /**
   * Stops the executor service managing the alerts
   *
   * @throws InterruptedException If any interruption happens during the shutdown
   */
  public void stop() throws InterruptedException {
    alertTaskPool.shutdownNow();
    alertTaskPool.awaitTermination(1, TimeUnit.SECONDS);
  }

  /**
   * Add the provided alerts to the manager
   *
   * @param alerts Collection of alerts
   */
  public void addAlerts(Collection<Alert> alerts) {
    this.alerts.addAll(alerts);
  }

}
