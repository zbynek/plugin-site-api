package io.jenkins.plugins.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.jenkins.plugins.models.JiraIssue;
import io.jenkins.plugins.models.JiraIssues;
import io.jenkins.plugins.services.ConfigurationService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpClientJiraIssues {
  private final ConfigurationService configurationService;
  private Logger logger = LoggerFactory.getLogger(HttpClientJiraIssues.class);

  private ObjectMapper mapper = new ObjectMapper();

  public HttpClientJiraIssues(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  private CloseableHttpClient getHttpClient() {
    final RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
      .setConnectionRequestTimeout(5000)
      .setConnectTimeout(5000)
      .setSocketTimeout(5000)
      .build();
    return HttpClients.custom().setDefaultCredentialsProvider(this.configurationService.getJiraCredentials()).setDefaultRequestConfig(requestConfig).build();
  }

  private String getHttpContent(String url) {
    url = this.configurationService.getJiraURL() + url;
    logger.info("getHttpContent(" + url + ")");
    final HttpGet get = new HttpGet(url);
    try (final CloseableHttpClient httpClient = getHttpClient();
         final CloseableHttpResponse response = httpClient.execute(get)) {
      final HttpEntity entity = response.getEntity();
      final String html = EntityUtils.toString(entity, StandardCharsets.UTF_8);
      EntityUtils.consume(entity);

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        return html;
      } else {
        final String msg = String.format("Unable to get content from %s - returned status code %d - %S", url, response.getStatusLine().getStatusCode(), html);
        logger.warn(msg);
        return null;
      }
    } catch (IOException e) {
      final String msg = "Problem getting wiki content";
      logger.error(msg, e);
      return null;
    }
  }

  public JiraIssues getIssues(String pluginName) throws IOException {
    return getIssues(pluginName, 0);
  }

  public JiraIssues getIssues(String pluginName, int startAt) throws IOException {
    int maxResults = 100;
    JiraIssues jiraIssues = new JiraIssues();

    String query = URLEncoder.encode("project=JENKINS AND status in (Open, \"In Progress\", Reopened) AND component=" + pluginName + "-plugin", "UTF-8");
    String jsonInput = getHttpContent("/rest/api/2/search?startAt=" + startAt + "&maxResults=" + maxResults + "&jql=" + query);
    if (Strings.isNullOrEmpty(jsonInput)) {
      throw new IOException("Empty return value");
    }

    JSONObject obj = new JSONObject(jsonInput);

    JSONArray jsonIssues = obj.getJSONArray("issues");
    for (Object issue : jsonIssues) {
      JSONObject jsonIssue = (JSONObject) issue;

      JiraIssue jiraIssue = new JiraIssue();
      jiraIssue.key = jsonIssue.getString("key");

      JSONObject fields = jsonIssue.getJSONObject("fields");
      jiraIssue.created = fields.optString("created");
      jiraIssue.updated = fields.optString("updated");
      jiraIssue.summary = fields.optString("summary");

      JSONObject issuetype = fields.optJSONObject("issuetype");
      JSONObject priority = fields.optJSONObject("priority");
      JSONObject status = fields.optJSONObject("status");
      JSONObject reporter = fields.optJSONObject("reporter");
      JSONObject assignee = fields.optJSONObject("assignee");
      JSONObject resolution = fields.optJSONObject("resolution");

      jiraIssue.issueType = issuetype != null ? issuetype.optString("name") : null;
      jiraIssue.priority =  priority != null ? priority.optString("name") : null;
      jiraIssue.status =  status != null ? status.optString("status") : null;
      jiraIssue.resolution = resolution != null ? reporter.optString("name") : null;
      jiraIssue.reporter = reporter != null ? reporter.optString("displayName") : null;
      jiraIssue.assignee = assignee != null ? assignee.optString("displayName") : null;

      jiraIssues.issues.add(jiraIssue);

    }
    if (obj.getInt("startAt") + jsonIssues.length() < obj.getInt("total")) {
      jiraIssues.issues.addAll(getIssues(pluginName, startAt + maxResults).issues);
    }

    return jiraIssues;
  }
}
