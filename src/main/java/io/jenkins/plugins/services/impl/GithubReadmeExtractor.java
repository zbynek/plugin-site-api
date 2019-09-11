package io.jenkins.plugins.services.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubReadmeExtractor extends GithubExtractor {

  private final static class GithubReadmeMatcher implements GithubMatcher {
    private final Matcher matcher;

    private GithubReadmeMatcher(Matcher matcher) {
      this.matcher = matcher;
    }

    @Override
    public String buildApiUrl(String clientId, String secret) {
      return String.format(README_ENDPOINT, matcher.group(1), clientId, secret);
    }

    @Override
    public String getDirectory() {
      return "/";
    }

    @Override
    public String getBranch() {
      String branch = matcher.group(3);
      return branch == null ? "master" : branch;
    }

    @Override
    public boolean find() {
      return matcher.find();
    }

    @Override
    public String getRepo() {
      return matcher.group(1);
    }
  }

  private static final String README_ENDPOINT = "https://api.github.com/repos/jenkinsci/%s/readme?client_id=%s&client_secret=%s";
  private static final Pattern REPO_PATTERN = Pattern
      .compile("https?://github.com/jenkinsci/([^/.]+)(\\.git|/tree/([^/]+))?/?$");

  @Override
  protected GithubMatcher getDelegate(String url) {
    final Matcher matcher = REPO_PATTERN.matcher(url);
    return new GithubReadmeMatcher(matcher);
  }

}
