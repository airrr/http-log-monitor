# Http log monitor

The log monitor is an application which tails an apache log access file and reports various metrics.
It runs a simple monitoring system on those metrics.

## Getting Started

Two ways to run the log monitor: Docker or maven + java
For both methods, be at the sources root directory

### Docker run

The simplest way to run the log monitor is with docker.
```
docker build . -t http-log-monitor
```
The command above will build the http-log-monitor image.

You can simply run the application after with:
```
docker run http-log-monitor
```
By default, http-log-monitor tails the /tmp/access.log file within this container. Mounting your own /tmp would make this easy to test

```
docker run -v /tmp:/tmp http-log-monitor
```

### Java and Maven

For this method, you need Java 1.8 and Maven installed

```
mvn package
```

This will build the executable jar in the /target folder
You can run the application now with:
```
java -jar target/http-log-monitor.jar
```

## Testing

### Unit tests

From the source root directory run:

```mvn test```

### Using the log generator

Provided with this project is a small python interactive command line tool to generate apache access log lines.

Check its README for more details.

#### With Docker

Generate the logs in one session, mounting the logs generator /tmp to your /tmp
```
docker run -ti -v /tmp:/tmp loggen
idle | /tmp/access.log > start
```
Run the log monitor on the other session
```
docker run -v /tmp:/tmp http-log-monitor
```

#### Without Docker

Generate the logs in one session

```$xslt
python main.py
idle | /tmp/access.log > start
```
Run the log monitor on the other ession
```$xslt
java -jar target/http-log-monitor.jar
```


## Application parameters

You can override the application properties by providing them to the command line such as
```--param=value```. Some examples:

### Usage

* Override the tailed log using docker:

  ```docker run http-log-monitor --logfile=/tmp/another-log-file``` 

* Flush the metrics every seconds:

  ```docker run http-log-monitor --aggregator.flush.interval.ms=1000```

* Change the traffic alert threshold:

   ```docker run http-log-monitor --alert.traffic.threshold:5```

### Parameters list

Here are all the supported parameters:
* aggregator.flush.interval.ms: Metric flush frequency in milliseconds
* aggregator.poll.timeout.ms: Log polling frequency in milliseconds
* alerter.thread.pool.size: Alerter executor service size
* logfile: File being tailed and monitored. If the file does not exist, the application will wait for it
* logqueue.size: Size of the internal blocking queue
* reporter.max.sections.displayed: Number of top http sections to be reported
* tailer.delay.ms: Log tailing frequency in milliseconds
* alert.list: List of active alerts
* alert.*alertname*.threshold: Threshold for this specific alert
* alert.*alertname*.window.alert.ms: Evaluation alert window for this specific alert
* alert.*alertname*.window.recovery.ms: Evaluation recovery window this specific alert

### Default values


```
logfile=/tmp/access.log
logqueue.size=5000
tailer.delay.ms=250
reporter.max.sections.displayed=5
aggregator.poll.timeout.ms=250
aggregator.flush.interval.ms=10000
alerter.thread.pool.size=2
alert.list=traffic
alert.traffic.threshold=10
alert.traffic.window.alert.ms=120000
alert.traffic.window.recovery.ms=120000
```

## Reporting

### Metrics reporting

Metrics are reported on the console after each metric flush.

```
00:42:22.961 ---HTTP monitor report between 06/Nov/2018:00:42:21 and 06/Nov/2018:00:42:22---
00:42:22.961 Total hits since start: 4 | Error rate: 0%
00:42:22.961 Interval hits: 2 | Error rate: 0%
00:42:22.961 *** Top 5 sections by traffic
00:42:22.961 section: traffic part | hit count | error rate (4XX, 5XX)
00:42:22.961 POST/api5: 100% | 2 | 0%
```

### Alerts reporting

Alerts are reported on the console and within a log file stored within /tmp/alerts.log for historical reasons.

## Code overview

Here is an overview of the code structure:

```$xslt



                                                                                                +------------+
                                                                                                |            |
                                                                                                | Alert task |
                                                                                                |            |
                                                                                                +------^-----+
                                                                                                       | Creates task on flush
                                                                                                       |
   +---------+      +--------+                                        Send flush event         +---------------+
   |         |      |        |                                                                 |               |
   | LogFile +------> Tailer |                                   +-----------------------------> Alert Manager |
   |         |      |        |                                   |                             |               |
   +---------+      +--------+                                   |                             +-------^-------+
                        |                                        |                                     |Reads from store
             Validates  |                                        |                                     |
             and parses |    +------------------+      +--------------------+                  +---------------+
                        |    |                  |      |                    |  Store metrics   |               |
                        +---->  Blocking Queue  +------> Metrics Aggregator +------------------> Metrics Store |
                             |                  |Polls |                    |  after each flush|               |
                             +------------------+      +--------------------+                  +---------------+
                                                                 |                                     |Reads from store
                                                                 |                                     |
                                                                 |                             +-------v----------+
                                                                 |                             |                  |
                                                                 +-----------------------------> Console Reporter |
                                                                                               |                  |
                                                                       Send flush event        +------------------+





```

* The tailer thread polls regularly any new line appended to the log file. The tailer handles log rotation and truncation.

* Each new line is validated and parsed to an object which is put into a blocking queue.

* Another thread, the metrics aggregator, polls this queue regularly. It aggregates the metrics from each log object during a certain time interval.

* When this time interval is over, it flushes those metrics to a metric store and notifies a reporter and an alert manager.

* On the flush event, the reporter uses the new metrics and the existing metrics to display various traffic metrics.

* On the same flush event, the alert manager submits a task per alert. This task will check the value monitored by each alert and display a message based upon the current alert state and its new state.  


## Design improvements

* For the sake of the exercise, I have tried to keep the dependencies to a minimum. To start with, I'd use Spring to manage the properties, for dependency injection and for flush scheduling.

* Each of those small blocks (tailer, aggregator, alerter, reporter) should be its own application

* I would replace the blocking queue and metrics store with external applications:
  * Blocking queue: Any message broker would work (RabbitMQ, Kafka) 
  * Metrics store: Some work would be required on the application layer interacting with the storage to store efficiently the metrics.
    * RDBMS would not be appropriate to manage flexibly metrics.
    * A key value store with sorting properties on the key would be good like DynamoDB. 
 
* All the rates are integers and should be floats

* The alert definition is not very flexible and requires some improvements. I did not dig too much on externalizing the alerts but each alert could have a query on the metrics store and an expression evaluating the value returned.

* The alert manager and the reporter could be web services.   

* Under heavy load, the metrics aggregation could not process all the queue messages during a flush:
  * Multi threading the aggregation would speed up the process but require a reduce step to merge the different thread results before flushing
  

## Built With

* [Apache Commons Tailer](https://commons.apache.org/proper/commons-io/javadocs/api-2.4/org/apache/commons/io/input/Tailer.html) - Tailer library
* [Maven](https://maven.apache.org/) - Dependency Management
* [Logback](https://logback.qos.ch/) - With the SLF4J facade, for the html alert logging 

## Authors

* **Gaetan Deputier**

## Acknowledgments

Thanks Datadog for this fun project!

```
^..^      /
/_/\_____/
   /\   /\
  /  \ /  \
```