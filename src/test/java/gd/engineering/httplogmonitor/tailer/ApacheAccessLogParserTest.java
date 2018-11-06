package gd.engineering.httplogmonitor.tailer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import gd.engineering.httplogmonitor.model.HttpLogLine;
import gd.engineering.httplogmonitor.model.InvalidLogLineException;

public class ApacheAccessLogParserTest {

  private ApacheAccessLogParser parser = new ApacheAccessLogParser();

  @Test
  public void testParserValidLine() {
    String validLogLine = "127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 34";
    HttpLogLine parsedLog = parser.parse(validLogLine);
    Assert.assertEquals(validLogLine, parsedLog.getOriginalLogLine());
    Assert.assertEquals("127.0.0.1", parsedLog.getRemoteHost());
    Assert.assertNull(parsedLog.getRemoteUser());
    Assert.assertEquals("frank", parsedLog.getUser());
    Assert.assertEquals("POST /api/user HTTP/1.0", parsedLog.getFullRequest());
    Assert.assertEquals(200, parsedLog.getStatusCode());
    Assert.assertEquals(34, parsedLog.getRequestSize());
    Assert.assertEquals("09/May/2018:16:00:42 +0000", parsedLog.getDateTime().format(parser.logTimestampFormatter));
  }

  @Test
  public void testParserValidLineEmptySize() {
    String validLogLine = "127.0.0.1 bob frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 -";
    HttpLogLine parsedLog = parser.parse(validLogLine);
    Assert.assertEquals(validLogLine, parsedLog.getOriginalLogLine());
    Assert.assertEquals("127.0.0.1", parsedLog.getRemoteHost());
    Assert.assertEquals("bob", parsedLog.getRemoteUser());
    Assert.assertEquals("frank", parsedLog.getUser());
    Assert.assertEquals("POST /api/user HTTP/1.0", parsedLog.getFullRequest());
    Assert.assertEquals(200, parsedLog.getStatusCode());
    Assert.assertEquals(0, parsedLog.getRequestSize());
    Assert.assertEquals("09/May/2018:16:00:42 +0000", parsedLog.getDateTime().format(parser.logTimestampFormatter));
  }

  @Test
  public void testParseSectionValidRequest() {
    String request = "/api/user";
    String section = parser.parseSection(request, request);
    Assert.assertEquals("/api", section);
    request = "/bob";
    section = parser.parseSection(request, request);
    Assert.assertEquals("/bob", section);
    request = "/bob?city=bordeaux&ts=123";
    section = parser.parseSection(request, request);
    Assert.assertEquals("/bob", section);
    request = "/index.html";
    section = parser.parseSection(request, request);
    Assert.assertEquals("/index.html", section);
    request = "/index.php?where=bordeaux";
    section = parser.parseSection(request, request);
    Assert.assertEquals("/index.php", section);
  }

  @Test(expected = InvalidLogLineException.class)
  public void testParseSectionInvalidRequest() {
    String request = "api/user";
    parser.parseSection(request, request);
  }

  @Test
  public void testSplitRequestValidRequest() {
    String request = "GET /bob HTTP/1.0";
    String[] reqSplit = parser.splitRequestField(request, request);
    Assert.assertEquals("GET", reqSplit[0]);
    Assert.assertEquals("/bob", reqSplit[1]);
    Assert.assertEquals("HTTP/1.0", reqSplit[2]);
  }

  @Test(expected = InvalidLogLineException.class)
  public void testSplitRequestInvalidRequestNull() {
    parser.splitRequestField(null, null);
  }

  @Test(expected = InvalidLogLineException.class)
  public void testSplitRequestInvalidRequestManyFields() {
    String request = "GET GET /bob HTTP/1.0";
    parser.splitRequestField(request, request);
  }

  @Test(expected = InvalidLogLineException.class)
  public void testSplitRequestInvalidRequestLessFields() {
    String request = "/bob HTTP/1.0";
    parser.splitRequestField(request, request);
  }


  @Test(expected = InvalidLogLineException.class)
  public void testParserEmptyLine() {
    String invalidLogLine = "";
    parser.parse(invalidLogLine);
  }

  @Test(expected = InvalidLogLineException.class)
  public void testParserInvalidLine() {
    String invalidLogLine = "- frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 34";
    parser.parse(invalidLogLine);
  }

  @Test(expected = InvalidLogLineException.class)
  public void testParserInvalidTimestampFormat() {
    String invalidLogLine = "127.0.0.1 - frank [09/May/2018:16:00:42] \"POST /api/user HTTP/1.0\" 200 34";
    parser.parse(invalidLogLine);
  }

  @Test
  public void testLogFormatRegexValid() {
    String validLogLine = "127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 34";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.getRegexToken(" "));
    Matcher matcher = p.matcher(validLogLine);
    Assert.assertTrue(matcher.matches());
  }

  @Test
  public void testLogFormatRegexInvalid() {
    String invalidLogLine = "- frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 34";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.getRegexToken(" "));
    Matcher matcher = p.matcher(invalidLogLine);
    Assert.assertFalse(matcher.matches());
    invalidLogLine = "a 127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 34";
    matcher = p.matcher(invalidLogLine);
    Assert.assertFalse(matcher.matches());
    invalidLogLine = "\n";
    matcher = p.matcher(invalidLogLine);
    Assert.assertFalse(matcher.matches());
    invalidLogLine = " ";
    matcher = p.matcher(invalidLogLine);
    Assert.assertFalse(matcher.matches());
  }

  @Test
  public void testLogFormatTokenRemoteHostValid() {
    String validHost = "192.169.1.1";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.REMOTE_HOST.format);
    Matcher matcher = p.matcher(validHost);
    Assert.assertTrue(matcher.matches());
  }

  @Test
  public void testLogFormatTokenRemoteHostInValid() {
    String invalidHost = "192.169.1";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.REMOTE_HOST.format);
    Matcher matcher = p.matcher(invalidHost);
    Assert.assertFalse(matcher.matches());
    invalidHost = "bob";
    matcher = p.matcher(invalidHost);
    Assert.assertFalse(matcher.matches());
    invalidHost = "192.158.1.1.1";
    matcher = p.matcher(invalidHost);
    Assert.assertFalse(matcher.matches());
    invalidHost = " ";
    matcher = p.matcher(invalidHost);
    Assert.assertFalse(matcher.matches());
  }

  @Test
  public void testLogFormatTokenRemoteUserValid() {
    String validRemoteUser = "bob";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.REMOTE_USER.format);
    Matcher matcher = p.matcher(validRemoteUser);
    Assert.assertTrue(matcher.matches());
    validRemoteUser = "-";
    matcher = p.matcher(validRemoteUser);
    Assert.assertTrue(matcher.matches());
  }

  @Test
  public void testLogFormatTokenRemoteUserInvalid() {
    String invalidRemoteUser = "";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.REMOTE_USER.format);
    Matcher matcher = p.matcher(invalidRemoteUser);
    Assert.assertFalse(matcher.matches());
    invalidRemoteUser = " ";
    matcher = p.matcher(invalidRemoteUser);
    Assert.assertFalse(matcher.matches());
    invalidRemoteUser = "\t";
    matcher = p.matcher(invalidRemoteUser);
    Assert.assertFalse(matcher.matches());
  }

  @Test
  public void testLogFormatTokenUserValid() {
    String validUser = "bob";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.USER.format);
    Matcher matcher = p.matcher(validUser);
    Assert.assertTrue(matcher.matches());
    validUser = "-";
    matcher = p.matcher(validUser);
    Assert.assertTrue(matcher.matches());
  }

  @Test
  public void testLogFormatTokenUserInvalid() {
    String invalidUser = "";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.USER.format);
    Matcher matcher = p.matcher(invalidUser);
    Assert.assertFalse(matcher.matches());
    invalidUser = " ";
    matcher = p.matcher(invalidUser);
    Assert.assertFalse(matcher.matches());
    invalidUser = "\t";
    matcher = p.matcher(invalidUser);
    Assert.assertFalse(matcher.matches());
  }

  @Test
  public void testLogFormatTokenTimestampValid() {
    String validTimestamp = "[09/May/2018:16:00:42 +0000]";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.TIMESTAMP.format);
    Matcher matcher = p.matcher(validTimestamp);
    Assert.assertTrue(matcher.matches());
  }

  @Test
  public void testLogFormatTokenTimestampInvalid() {
    String invalidTimestamp = "09/May/2018:16:00:42 +0000";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.TIMESTAMP.format);
    Matcher matcher = p.matcher(invalidTimestamp);
    Assert.assertFalse(matcher.matches());
    invalidTimestamp = "[]";
    matcher = p.matcher(invalidTimestamp);
    Assert.assertFalse(matcher.matches());
  }

  @Test
  public void testLogFormatTokenRequestValid() {
    String validRequest = "\"POST /api/user HTTP/1.0\"";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.REQUEST.format);
    Matcher matcher = p.matcher(validRequest);
    Assert.assertTrue(matcher.matches());
  }

  @Test
  public void testLogFormatTokenRequestInvalid() {
    String invalidRequest = "POST /api/user HTTP/1.0";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.REQUEST.format);
    Matcher matcher = p.matcher(invalidRequest);
    Assert.assertFalse(matcher.matches());
    invalidRequest = "\"\"";
    matcher = p.matcher(invalidRequest);
    Assert.assertFalse(matcher.matches());
  }

  @Test
  public void testLogFormatTokenStatusValid() {
    String validStatus = "202";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.STATUS.format);
    Matcher matcher = p.matcher(validStatus);
    Assert.assertTrue(matcher.matches());
  }

  @Test
  public void testLogFormatTokenStatusInvalid() {
    String invalidStatus = "4044";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.STATUS.format);
    Matcher matcher = p.matcher(invalidStatus);
    Assert.assertFalse(matcher.matches());
    invalidStatus = " ";
    matcher = p.matcher(invalidStatus);
    Assert.assertFalse(matcher.matches());
  }

  @Test
  public void testLogFormatTokenSizeValid() {
    String validSize = "123";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.SIZE.format);
    Matcher matcher = p.matcher(validSize);
    Assert.assertTrue(matcher.matches());
    validSize = "-";
    matcher = p.matcher(validSize);
    Assert.assertTrue(matcher.matches());
  }

  @Test
  public void testLogFormatTokenSizeInvalid() {
    String invalidSize = " ";
    Pattern p = Pattern.compile(ApacheAccessLogParser.HttpLogFormatToken.SIZE.format);
    Matcher matcher = p.matcher(invalidSize);
    Assert.assertFalse(matcher.matches());
    invalidSize = "bob";
    matcher = p.matcher(invalidSize);
    Assert.assertFalse(matcher.matches());
    invalidSize = "1-234";
    matcher = p.matcher(invalidSize);
    Assert.assertFalse(matcher.matches());
  }

}
