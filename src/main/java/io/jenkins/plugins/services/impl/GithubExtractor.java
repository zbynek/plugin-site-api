package io.jenkins.plugins.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class GithubExtractor implements WikiExtractor {

  private static final String README_ENDPOINT = "https://api.github.com/repos/jenkinsci/%s/readme?client_id=%s&client_secret=%s";
  private static final Pattern REPO_PATTERN = Pattern
      .compile("https?://github.com/jenkinsci/([^/.]+)(\\.git)?(/|/blob/master/README\\.md)?$");

  @Override
  public String getApiUrl(String wikiUrl) {
    Matcher matcher = REPO_PATTERN.matcher(wikiUrl);
    String clientId = getClientId();
    if (clientId != null && matcher.find()) {
      return String.format(README_ENDPOINT, matcher.group(1), clientId, System.getenv("GITHUB_SECRET"));
    }
    return null;
  }

  private String getClientId() {
    String clientId = StringUtils.trimToNull(System.getenv("GITHUB_CLIENT_ID"));
    if (clientId != null) {
      return clientId;
    }
    return StringUtils.trimToNull(System.getProperty("github.client.id"));
  }

  @Override
  public String extractHtml(String apiContent, HttpClientWikiService service) {
    return apiContent;
  }

  @Override
  public List<Header> getHeaders() {
    Header header = new BasicHeader("Accept", "application/vnd.github.v3.html");
    return Collections.singletonList(header);
  }

}
