package gd.engineering.httplogmonitor.model;

import java.time.ZonedDateTime;

/**
 * POJO representing an apache access.log line.
 * Stores the original log line as well
 */
public class HttpLogLine {
  private String remoteHost;
  private String remoteUser;
  private String user;
  private ZonedDateTime dateTime;
  private String fullRequest;
  private int statusCode;
  private int requestSize;
  private String httpMethod;
  private String section;
  private String originalLogLine;

  public String getRemoteHost() {
    return remoteHost;
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public String getRemoteUser() {
    return remoteUser;
  }

  public void setRemoteUser(String remoteUser) {
    this.remoteUser = remoteUser;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public ZonedDateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(ZonedDateTime dateTime) {
    this.dateTime = dateTime;
  }

  public String getFullRequest() {
    return fullRequest;
  }

  public void setFullRequest(String fullRequest) {
    this.fullRequest = fullRequest;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public int getRequestSize() {
    return requestSize;
  }

  public void setRequestSize(int requestSize) {
    this.requestSize = requestSize;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getOriginalLogLine() {
    return originalLogLine;
  }

  public void setOriginalLogLine(String originalLogLine) {
    this.originalLogLine = originalLogLine;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  @Override
  public String toString() {
    return "HttpLogLine{" +
        "remoteHost='" + remoteHost + '\'' +
        ", remoteUser='" + remoteUser + '\'' +
        ", user='" + user + '\'' +
        ", dateTime=" + dateTime +
        ", fullRequest='" + fullRequest + '\'' +
        ", statusCode=" + statusCode +
        ", requestSize=" + requestSize +
        ", httpMethod='" + httpMethod + '\'' +
        ", section='" + section + '\'' +
        ", originalLogLine='" + originalLogLine + '\'' +
        '}';
  }
}
