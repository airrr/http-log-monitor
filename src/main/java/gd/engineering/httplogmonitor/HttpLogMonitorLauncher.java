package gd.engineering.httplogmonitor;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gd.engineering.httplogmonitor.aggregator.BlockingStatsAggregator;
import gd.engineering.httplogmonitor.alerter.AlertBuilder;
import gd.engineering.httplogmonitor.alerter.AlerterManager;
import gd.engineering.httplogmonitor.model.HttpLogLine;
import gd.engineering.httplogmonitor.model.metrics.InMemoryMetricsStore;
import gd.engineering.httplogmonitor.model.metrics.MetricsStore;
import gd.engineering.httplogmonitor.reporter.HttpSectionConsoleReporter;
import gd.engineering.httplogmonitor.reporter.MetricsReporter;
import gd.engineering.httplogmonitor.tailer.ApacheAccessLogParser;
import gd.engineering.httplogmonitor.tailer.HttpLogTailer;
import gd.engineering.httplogmonitor.tailer.HttpLogTailerListener;

/**
 * Entrypoint for the HttpLogMonitorLauncher
 * <p>
 * The current implementation tails a file line by line, parses each line expecting the format defined at https://www.w3.org/Daemon/User/Config/Logging.html#common-logfile-format
 * and extract various metrics. Those metrics are aggregated at regular intervals and stored in memory.
 * After each aggregation, a small reporting is displayed on the console and a list of alerts are ran onto the metrics stored in memory.
 * Those alerts are both displayed on the console and in an log file defaulted at /tmp/alerts.log
 */
public class HttpLogMonitorLauncher {

  private static final Logger LOG = LoggerFactory.getLogger(HttpLogMonitorLauncher.class);
  private static final String DEFAULT_PROPERTY_FILE = "/application.properties";
  private static final String ARG_START = "--";
  private static final String ARG_DELIMITER = "=";

  public static void main(String[] args) {
    //Loading file & command line properties
    Properties properties = loadProperties(args);
    printPropertiesAndUsage(properties);

    //Instantiate main objects
    BlockingQueue<HttpLogLine> queue = new ArrayBlockingQueue<>(Integer.parseInt(properties.getProperty("logqueue.size")));
    MetricsStore store = new InMemoryMetricsStore();
    Clock clock = Clock.systemDefaultZone();
    HttpLogTailerListener logTailerListener = new HttpLogTailerListener(new ApacheAccessLogParser(), queue);
    HttpLogTailer logTailer = new HttpLogTailer(properties.getProperty("logfile"), logTailerListener, Integer.parseInt(properties.getProperty("tailer.delay.ms")));
    BlockingStatsAggregator aggregator = new BlockingStatsAggregator(queue, store, clock, Long.parseLong(properties.getProperty("aggregator.poll.timeout.ms")), Long.parseLong(properties.getProperty("aggregator.flush.interval.ms")));
    MetricsReporter reporter = new HttpSectionConsoleReporter(store, Integer.parseInt(properties.getProperty("reporter.max.sections.displayed")));
    AlerterManager alerter = new AlerterManager(store, Integer.parseInt(properties.getProperty("alerter.thread.pool.size")), clock);
    alerter.addAlerts(AlertBuilder.buildAlertsFromProperties(properties));
    aggregator.addStatsFlushListener(reporter);
    aggregator.addStatsFlushListener(alerter);

    //Start tailer and aggregator threads
    Thread aggregatorThread = new Thread(aggregator, "aggregator");
    aggregatorThread.start();
    logTailer.start();

    //Register graceful shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        logTailer.stop();
        aggregatorThread.interrupt();
        aggregatorThread.join();
        alerter.stop();
        LOG.info("Shutting down http monitor");
      } catch (InterruptedException e) {
        LOG.warn("Shutdown interruption");
      }
    }));
    try {
      aggregatorThread.join();
    } catch (InterruptedException e) {
    }
  }

  /**
   * Load the log monitor properties first from the default application.properties and
   * override then with any found on the command line
   *
   * @param args Command line arguments
   * @return Log monitor properties
   */
  private static Properties loadProperties(String[] args) {
    Properties properties = new Properties();
    loadDefaultProperties(properties);
    parseArgsToProperties(args, properties);
    return properties;
  }

  /**
   * Display the provided properties, sorted in alphabetical order
   *
   * @param properties Log monitor properties
   */
  private static void printPropertiesAndUsage(Properties properties) {
    Map<String, Object> sortedProperties = new TreeMap<>();
    LOG.info("---HttpLogMonitor properties---");
    properties.forEach((propertyName, propertyValue) -> sortedProperties.put(propertyName.toString(), propertyValue));
    sortedProperties.forEach((propertyName, propertyValue) -> LOG.info("{}:{}", propertyName, propertyValue));
    LOG.info("-------------------------------");
    LOG.info("Override: http-log-monitor.jar --<param>=<value> | e.g: --logfile=/tmp/next.log");
    LOG.info("-------------------------------");
  }

  /**
   * Simple parse method for command line arguments with the format --<param>=<val>
   * If the param is not found in the properties, it will not be set (the acceptable params are the ones listed in the default properties file)
   *
   * @param args       Command line arguments
   * @param properties Log monitor properties
   */
  private static void parseArgsToProperties(String[] args, Properties properties) {
    for (String arg : args) {
      if (arg.startsWith(ARG_START)) {
        String[] split = arg.substring(ARG_START.length()).split(ARG_DELIMITER);
        if (split.length == 2) {
          String propertyName = split[0];
          String propertyValue = split[1];
          if (properties.containsKey(propertyName)) {
            properties.put(propertyName, propertyValue);
          }
        }
      }
    }
  }

  /**
   * Load the default properties from the property file.
   * If not found, throw a runtime error
   *
   * @param properties Log monitor properties holding the content of the default property file
   */
  private static void loadDefaultProperties(Properties properties) {
    try {
      properties.load(HttpLogMonitorLauncher.class.getResourceAsStream(DEFAULT_PROPERTY_FILE));
    } catch (IOException e1) {
      LOG.error("Unable to load default property file, exiting");
      throw new RuntimeException();
    }
  }
}
