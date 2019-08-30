package io.jenkins.plugins.services;

/**
 * <p>Responsible for retrieving and cleaning wiki content for a plugin</p>
 */
public interface WikiService {

  /**
   * <p>Get wiki content</p>
   *
   * @param url URL to the wiki content`
   * @return content
   * @throws ServiceException in case something goes wrong
   */
  String getWikiContent(String url) throws ServiceException;

}
