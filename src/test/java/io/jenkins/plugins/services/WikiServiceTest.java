package io.jenkins.plugins.services;

import io.jenkins.plugins.models.GeneratedPluginData;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.services.impl.ConfluenceApiExtractor;
import io.jenkins.plugins.services.impl.ConfluenceDirectExtractor;
import io.jenkins.plugins.services.impl.DefaultConfigurationService;
import io.jenkins.plugins.services.impl.GithubExtractor;
import io.jenkins.plugins.services.impl.HttpClientWikiService;
import io.jenkins.plugins.services.impl.WikiExtractor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class WikiServiceTest {

  private HttpClientWikiService wikiService;

  @Before
  public void setUp() {
    wikiService = new HttpClientWikiService();
    wikiService.postConstruct();
  }

  @Test
  public void testGetWikiContentConfluence() {
    final String url = "https://wiki.jenkins.io/display/JENKINS/Git+Plugin";
    final String content = wikiService.getWikiContent(url);
    assertValidContent(content);
  }

  @Test
  public void testGetWikiContentGit() {
    System.setProperty("github.client.id", "dummy");
    final String url = "https://github.com/jenkinsci/labelled-steps-plugin";
    final String content = wikiService.getWikiContent(url);
    assertValidContent(content);
  }

  @Test
  @Ignore("It's unclear what this is supposed to test")
  public void testGetWikiContent404() {
    final String url = "https://wiki.jenkins.io/display/JENKINS/nonexistant?foo";
    final String content = wikiService.getWikiContent(url);
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertEquals(HttpClientWikiService.getNonWikiContent(url), content);
  }

  @Test
  public void testGetWikiContentNotJenkins() {
    final String url = "https://www.google.com";
    final String content = wikiService.getWikiContent(url);
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertEquals(HttpClientWikiService.getNonWikiContent(url), content);
  }

  @Test
  public void testGetWikiContentNoUrl() {
    final String content = wikiService.getWikiContent(null);
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertEquals(HttpClientWikiService.getNoDocumentationFound(), content);
  }


  @Test
  public void testCleanWikiContent() throws IOException {
    final File file = new File("src/test/resources/wiki_content.html");
    final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    final String cleanContent = ConfluenceDirectExtractor.cleanWikiContent(content, wikiService);
    Assert.assertNotNull("Wiki content is null", cleanContent);
    final Document html = Jsoup.parseBodyFragment(cleanContent);
    html.getElementsByAttribute("href").forEach(element -> {
      final String value = element.attr("href");
      Assert.assertFalse("Wiki content not clean - href references to root : " + value, value.startsWith("/"));
    });
    html.getElementsByAttribute("src").forEach(element -> {
      final String value = element.attr("src");
      Assert.assertFalse("Wiki content not clean - src references to root : " + value, value.startsWith("/"));
    });
  }

  @Test
  public void testReplaceAttribute() throws IOException {
    final String baseUrl = "https://wiki.jenkins.io";
    final String src = "/some-image.jpg";
    final Element element = Jsoup.parseBodyFragment(String.format("<img id=\"test-image\" src=\"%s\"/>", src)).getElementById("test-image");
    wikiService.replaceAttribute(element, "src", baseUrl);
    Assert.assertEquals("Attribute replacement failed", baseUrl + src, element.attr("src"));
  }

  @Test
  public void testConfluenceApiExtractor() {
    ConfluenceApiExtractor confluenceApi = new ConfluenceApiExtractor();
    assertInvalid(confluenceApi, "https://wiki.jenkins-ci.org");
    assertInvalid(confluenceApi, "https://wiki.jenkins.io");
    assertInvalid(confluenceApi, "https://wiki.jenkins.io/x/123");
    assertValid(confluenceApi, "https://wiki.jenkins.io/display/JENKINS/Git+Plugin");
    assertValid(confluenceApi, "http://wiki.jenkins-ci.org/display/jenkins/Git+Plugin");
  }

  @Test
  public void testConfluenceDirectExtractor() {
    ConfluenceDirectExtractor confluenceApi = new ConfluenceDirectExtractor();
    assertValid(confluenceApi, "https://wiki.jenkins.io/pages/viewpage.action?pageId=60915753");
    assertValid(confluenceApi, "https://wiki.jenkins.io/x/xyz");
    assertInvalid(confluenceApi, "https://example.com");
  }

  @Test
  public void testGithubExtractor() {
    System.setProperty("github.client.id", "dummy");
    GithubExtractor githubApi = new GithubExtractor();
    assertValid(githubApi, "https://github.com/jenkinsci/xyz");
    assertValid(githubApi, "https://github.com/jenkinsci/xyz/");
    assertValid(githubApi, "http://github.com/jenkinsci/xyz.git");
    assertInvalid(githubApi, "https://github.com/other-org/repo");
    assertInvalid(githubApi, "https://github.com/jenkinsci/xyz/blob/file.md");
  }

  private void assertInvalid(WikiExtractor extractor, String string) {
    Assert.assertNull(extractor.getApiUrl(string));
  }

  private void assertValid(WikiExtractor extractor, String string) {
    Assert.assertNotNull(extractor.getApiUrl(string));
  }

  private void assertValidContent(String content) {
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertThat(content, CoreMatchers.not(CoreMatchers.containsString(
        HttpClientWikiService.EXTERNAL_DOCUMENTATION_PREFIX)));
    Assert.assertFalse("Wiki content is empty", content.isEmpty());
  }

}
