package io.jenkins.plugins.services.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubContentsExtractor extends GithubExtractor {
  private final static class GithubContentMatcher implements GithubMatcher {
    private final Matcher matcher;

    private GithubContentMatcher(Matcher matcher) {
      this.matcher = matcher;
    }

    @Override
    public String buildApiUrl( String clientId, String secret) {
      return String.format(CONTENTS_ENDPOINT, 
          matcher.group(1), matcher.group(3), matcher.group(2), clientId, secret);
    }

    @Override
    public String getDirectory() {
      String filePath = matcher.group(3);
      return "/" + filePath.substring(0, filePath.lastIndexOf("/") + 1);
    }

    @Override
    public String getBranch() {
      return matcher.group(2);
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

  private static final Pattern REPO_PATTERN = Pattern
      .compile("https?://github.com/jenkinsci/([^/.]+)/blob/([^/]+)/(.+\\.(md|adoc))$");
  
  private static final String CONTENTS_ENDPOINT = 
      "https://api.github.com/repos/jenkinsci/%s/contents/%s?ref=%s&client_id=%s&client_secret=%s";

  @Override
  protected GithubMatcher getDelegate(String url) {
    final Matcher matcher = REPO_PATTERN.matcher(url);
    return new GithubContentMatcher(matcher);
  }

}
