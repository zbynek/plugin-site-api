package io.jenkins.plugins.services.impl;

/**
 * Maps browser URLs to path components and API URLs
 */
public interface GithubMatcher {
  /**
   * @return per-repository API endpoint, without leading slash
   */
  String getEndpoint();

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
