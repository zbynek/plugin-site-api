package io.jenkins.plugins.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.jsoup.nodes.Element;

import io.jenkins.plugins.services.ServiceException;

/**
 * Gets content from Jenkins wiki directly for non-standard patterns, e.g.
 * 
 * https://wiki.jenkins-ci.org/x/GAAHAQ
 * https://wiki.jenkins-ci.org/pages/viewpage.action?pageId=60915753
 */
public class ConfluenceDirectExtractor implements WikiExtractor {
  public static final String BASE_URL = "https://wiki.jenkins.io";
  private static final Pattern WIKI_HOST_REGEXP = Pattern.compile("^https?://wiki.jenkins(-ci.org|.io)",
      Pattern.CASE_INSENSITIVE);

  @Override
  public String getApiUrl(String wikiUrl) {
    Matcher matcher = WIKI_HOST_REGEXP.matcher(wikiUrl);
    if (matcher.find()) {
      return matcher.replaceFirst(BASE_URL);
    }
    return null;
  }

  @Override
  public String extractHtml(String httpContent, String url, HttpClientWikiService service) {
    return cleanWikiContent(httpContent, service);
  }

  public static String cleanWikiContent(String content, HttpClientWikiService service) throws ServiceException {
    final Element wikiContent = service.getElementByClassFromText("wiki-content", content);
    if (wikiContent == null) {
      return null;
    }
    // Remove the entire span at the top with the "Plugin Information" inside
    final Element topPluginInformation = wikiContent
        .select(".conf-macro.output-inline th :contains(Plugin Information)").first();
    if (topPluginInformation != null) {
      Element element = topPluginInformation;
      while (!element.tagName().equals("table")) {
        element = element.parent();
      }
      element.remove();
    }
    // Remove any table of contents
    wikiContent.getElementsByClass("toc").remove();

    // Remove the jira issues
    wikiContent.getElementsByClass(".jira-issues").remove();
    
    // Replace href/src with the wiki url
    service.convertLinksToAbsolute(wikiContent, BASE_URL, "/display/JENKINS/");
    return wikiContent.html();
  }

  @Override
  public List<Header> getHeaders() {
    return Collections.emptyList();
  }
}
