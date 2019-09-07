package io.jenkins.plugins.services.impl;

/**
 * Maps browser URLs to path components and API URLs
 */
public interface GithubMatcher {
  /**
   * @param clientId GitHub app client ID
   * @param secret GitHub app secret
   * @return API url to get content of the browser URL as HTML
   */
  String buildApiUrl(String clientId, String secret);

  /**
   * @return directory within repo, including trailing slash
   */
  String getDirectory();

  /**
   * @return branch or tag
   */
  String getBranch();

  /**
   * @return whether URL is valid
   */
  boolean find();

  /**
   * @return repository name
   */
  String getRepo();
}
