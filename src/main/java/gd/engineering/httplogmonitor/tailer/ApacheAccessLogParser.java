package gd.engineering.httplogmonitor.tailer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import gd.engineering.httplogmonitor.model.HttpLogLine;
import gd.engineering.httplogmonitor.model.InvalidLogLineException;

/**
 * Parser for apache access logs following this format [https://www.w3.org/Daemon/User/Config/Logging.html#common-logfile-format]
 * e.g: 127.0.0.1 - frank [09/May/2018:16:00:42 +0000] "POST /api/user HTTP/1.0" 200 34
 * <p>
 * The parsing is mostly managed by the inner enum {@link gd.engineering.httplogmonitor.tailer.ApacheAccessLogParser.HttpLogFormatToken} which
 * holds a regex for each field
 * <p>
 * Any invalid line will throw an {@link gd.engineering.httplogmonitor.model.InvalidLogLineException} with the erroneous line and reason
 */
public class ApacheAccessLogParser implements HttpLogParser {

  private static final String HYPHEN = "-";
  private static final String FORWARD_SLASH = "/";
  private static final char CHAR_FORWARD_SLASH = '/';
  private static final char CHAR_QMARK = '?';
  private static final String LOG_TOKEN_SEPARATOR = " ";
  private static final String REQUEST_FIELD_TOKEN_SEPARATOR = " ";
  private static final int REQUEST_FIELD_TOKENS = 3;
  private static final int REQUEST_FIELD_RESOURCE_INDEX = 1;
  private static final int REQUEST_FIELD_METHOD_INDEX = 0;
  private static final Set<String> HTTP_METHODS = new HashSet<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "TRACE", "OPTIONS"));
  private Pattern logPattern = Pattern.compile(HttpLogFormatToken.getRegexToken(LOG_TOKEN_SEPARATOR));
  DateTimeFormatter logTimestampFormatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

  /**
   * Parse a apache access.log line to a HttpLogLine model
   *
   * @param logLine Apache access.log line following https://www.w3.org/Daemon/User/Config/Logging.html#common-logfile-format
   * @return The HttpLogLine model corresponding to the log line
   * @throws InvalidLogLineException For invalid lines
   */
  public HttpLogLine parse(String logLine) {
    if (StringUtils.isEmpty(logLine)) {
      throw new InvalidLogLineException("empty", logLine);
    }
    Matcher matcher = logPattern.matcher(logLine);
    HttpLogLine parsedLine;

    if (matcher.matches()) {
      parsedLine = new HttpLogLine();
      String timestampField = matcher.group(HttpLogFormatToken.TIMESTAMP.group);
      String requestSizeField = nullIfHyphen(matcher.group(HttpLogFormatToken.SIZE.group));
      String[] requestField = splitRequestField(matcher.group(HttpLogFormatToken.REQUEST.group), logLine);
      parsedLine.setOriginalLogLine(logLine);
      parsedLine.setRemoteHost(nullIfHyphen(matcher.group(HttpLogFormatToken.REMOTE_HOST.group)));
      parsedLine.setRemoteUser(nullIfHyphen(matcher.group(HttpLogFormatToken.REMOTE_USER.group)));
      parsedLine.setUser(matcher.group(HttpLogFormatToken.USER.group));
      parsedLine.setFullRequest(matcher.group(HttpLogFormatToken.REQUEST.group));
      parsedLine.setStatusCode(Integer.parseInt(matcher.group(HttpLogFormatToken.STATUS.group)));
      parsedLine.setRequestSize(requestSizeField == null ? 0 : Integer.parseInt(requestSizeField));
      parsedLine.setSection(parseSection(requestField[REQUEST_FIELD_RESOURCE_INDEX], logLine));
      parsedLine.setHttpMethod(parseHttpMethod(requestField[REQUEST_FIELD_METHOD_INDEX], logLine));
      try {
        parsedLine.setDateTime(ZonedDateTime.parse(timestampField, logTimestampFormatter));
      } catch (DateTimeParseException pex) {
        throw new InvalidLogLineException("date field not matching pattern", logLine, pex);
      }
    } else {
      throw new InvalidLogLineException("bad format", logLine);
    }
    return parsedLine;
  }

  /**
   * Validates and split the request part of the log like [POST /api/user HTTP/1.0]
   * to an array with method, resource and protocol
   *
   * @param fullRequest HTTP request details
   * @param logLine     Original log line
   * @return An array of string of length 3 with the http method, resource and protocol
   * @throws InvalidLogLineException If the http request details are invalids
   */
  String[] splitRequestField(String fullRequest, String logLine) {
    if (StringUtils.isEmpty(fullRequest)) {
      throw new InvalidLogLineException("request empty", logLine);
    }
    String[] splitRequest = fullRequest.split(REQUEST_FIELD_TOKEN_SEPARATOR);
    if (splitRequest.length != REQUEST_FIELD_TOKENS) {
      throw new InvalidLogLineException("request not matching [METHOD RESOURCE PROTOCOL]", logLine);
    }
    return splitRequest;
  }

  /**
   * Parse the http section from the full http path.
   * The section starts from the first forward slash and stops to either the first forward slash or question mark
   *
   * @param path    Http path
   * @param logLine Original log line
   * @return The http section
   * @throws InvalidLogLineException If the http path is invalid
   */
  String parseSection(String path, String logLine) {
    if (!path.startsWith(FORWARD_SLASH)) {
      throw new InvalidLogLineException("request resource missing starting slash", logLine);
    }
    return path.substring(0, findEndSectionIndexFromPath(path));
  }


  /**
   * Validates the http method from the log line
   *
   * @param method  Raw http method from the log line
   * @param logLine Original log line
   * @return The http method provided
   * @throws InvalidLogLineException If the http method is invalid
   */
  private String parseHttpMethod(String method, String logLine) {
    if (!HTTP_METHODS.contains(method)) {
      throw new InvalidLogLineException("invalid http method " + method, logLine);
    }
    return method;
  }

  /**
   * Given a http path, find the index of the last character delimiting the section.
   * E.g: /api/user will return 4, /user?who=bob will return 5
   *
   * @param path Valid http path
   * @return Index of the end of the section
   */
  private int findEndSectionIndexFromPath(String path) {
    for (int i = 1; i < path.length(); i++) {
      if (path.charAt(i) == CHAR_FORWARD_SLASH || path.charAt(i) == CHAR_QMARK) {
        return i;
      }
    }
    return path.length();
  }

  /**
   * Simple method which return null if the field is Hyphen. Useful to parse empty user and remote user fields
   *
   * @param field Any string
   * @return Null if the provided string is a hyphen, the string otherwise
   */
  private String nullIfHyphen(String field) {
    return HYPHEN.equals(field) ? null : field;
  }

  /**
   * Representation of the apache log fields. Each field has a specific associated regex used for parsing and group for the order
   */
  public enum HttpLogFormatToken {
    REMOTE_HOST(1, "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})"),
    REMOTE_USER(2, "(\\S+)"),
    USER(3, "(\\S+)"),
    TIMESTAMP(4, "\\[(.+?)\\]"),
    REQUEST(5, "\"(.+?)\""),
    STATUS(6, "(\\d{3})"),
    SIZE(7, "(\\d+|-)");

    int group;
    String format;

    HttpLogFormatToken(int group, String format) {
      this.group = group;
      this.format = format;
    }

    /**
     * Build the log regex by joining all the tokens with the provided separator
     *
     * @param separator Separates the tokens
     * @return The overall regex for access log matching
     * @throws IllegalArgumentException if the separator is empty
     */
    public static String getRegexToken(String separator) {
      if (StringUtils.isEmpty(separator)) {
        throw new IllegalArgumentException("Empty separator for getregextoken");
      }
      return "^" + Stream.of(values()).map(token -> token.format).collect(Collectors.joining(separator));
    }
  }
}
