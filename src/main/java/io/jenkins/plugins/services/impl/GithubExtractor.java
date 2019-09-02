package io.jenkins.plugins.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class GithubExtractor implements WikiExtractor {

  private static final String README_ENDPOINT = "https://api.github.com/repos/jenkinsci/%s/readme?client_id=%s&client_secret=%s";
  private static final Pattern REPO_PATTERN = Pattern
      .compile("https?://github.com/jenkinsci/([^/.]+)(\\.git)?(/|/blob/master/README\\.md)?$");
  private static final Logger LOGGER = Logger.getLogger(GithubExtractor.class.getName());

  @Override
  public String getApiUrl(String wikiUrl) {
    Matcher matcher = REPO_PATTERN.matcher(wikiUrl);
    if (!matcher.find()) {
      return null;
    }

    String clientId = getClientId();
    if (clientId == null) {
      LOGGER.log(Level.WARNING, "Cannot retrieve API URL for {0}. No GitHub Client ID specified", wikiUrl);
      return null;
    }

    return String.format(README_ENDPOINT, matcher.group(1), clientId, System.getenv("GITHUB_SECRET"));
  }

  private String getClientId() {
    String clientId = StringUtils.trimToNull(System.getenv("GITHUB_CLIENT_ID"));
    if (clientId != null) {
      return clientId;
    }
    return StringUtils.trimToNull(System.getProperty("github.client.id"));
  }

  @Override
  public String extractHtml(String apiContent, String url, HttpClientWikiService service) {
    Matcher matcher = REPO_PATTERN.matcher(url);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid github URL" + url);
    }
    final Document html = Jsoup.parse(apiContent);
    final Element mainDiv = html.getElementsByTag("body").get(0).child(0);
    //TODO(oleg_nenashev): Support organization and branch customization?
    convertLinksToAbsolute(service, mainDiv, "jenkinsci", matcher.group(1), "master");
    return mainDiv.toString();
  }

  private void convertLinksToAbsolute(HttpClientWikiService service, Element wikiContent, String orgName, String repoName, String branch) {
    String documentationHost = String.format("https://github.com/%s/%s/blob/%s/", orgName, repoName, branch);
    String imageHost = String.format("https://raw.githubusercontent.com/%s/%s/%s/", orgName, repoName, branch);

    // Relative hyperlinks, we resolve "/docs/rest-api.adoc" as https://github.com/jenkinsci/folder-auth-plugin/blob/master/docs/rest-api.adoc
    wikiContent.getElementsByAttribute("href").forEach(element -> service.replaceAttribute(element, "href", documentationHost, ""));
    //TODO: Should we host images from our infrastructure? What are the GitHub terms here?
    // Relative image inclusions, we resolve /docs/images/screenshot.png as https://raw.githubusercontent.com/jenkinsci/folder-auth-plugin/master/docs/images/screenshot.png
    wikiContent.getElementsByAttribute("src").forEach(element -> service.replaceAttribute(element, "src", imageHost, ""));
  }

  @Override
  public List<Header> getHeaders() {
    Header header = new BasicHeader("Accept", "application/vnd.github.v3.html");
    return Collections.singletonList(header);
  }

}
