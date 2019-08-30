package io.jenkins.plugins.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfluenceApiExtractor implements WikiExtractor {
  private static final String WIKI_REST_API_TITLE = "https://wiki.jenkins.io/rest/api/content?expand=body.view&title=%s";
  private static final Pattern WIKI_URL_REGEXP_TITLE = Pattern
      .compile("^https?://wiki.jenkins(-ci.org|.io)/display/(jenkins|hudson)/([^/]*)/?$", Pattern.CASE_INSENSITIVE);

  @Override
  public String extractHtml(String jsonStr, String url, HttpClientWikiService wikiService) {
    try {
      JSONArray json = new JSONObject(jsonStr).getJSONArray("results");
      if (json.length() > 0) {
        String html = json.getJSONObject(0).getJSONObject("body").getJSONObject("view").getString("value");
        return ConfluenceDirectExtractor.cleanWikiContent(wrapInElement(html), wikiService);
      }
      return HttpClientWikiService.getNoDocumentationFound();
    } catch (RuntimeException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static String wrapInElement(String html) {
    return "<body class=wiki-content>" + html + "</body>";
  }

  @Override
  public String getApiUrl(String wikiUrl) {
    Matcher matcher = WIKI_URL_REGEXP_TITLE.matcher(wikiUrl);
    if (matcher.find()) {
      return String.format(WIKI_REST_API_TITLE, matcher.group(3));
    }
    return null;
  }

  @Override
  public List<Header> getHeaders() {
    return Collections.emptyList();
  }

}
