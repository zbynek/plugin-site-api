package io.jenkins.plugins.services.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.http.Header;

public interface WikiExtractor {

  /**
   * @param wikiUrl content URL
   * @return API url that for accessing rendered content
   */
  String getApiUrl(@NotNull String wikiUrl);

  /**
   * <p>
   * Get clean wiki content so it's presentable to the UI
   * </p>
   *
   * @param apiContent content retrieved from API
   * @param documentation URL
   * @return cleaned content
   * @throws ServiceException in case something goes wrong
   */
  String extractHtml(@NotNull String apiContent, String url, HttpClientWikiService service);

  /**
   * @return HTTP headers
   */
  List<Header> getHeaders();

}
