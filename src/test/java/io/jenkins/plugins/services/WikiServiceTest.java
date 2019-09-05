package io.jenkins.plugins.services;

import io.jenkins.plugins.services.impl.ConfluenceApiExtractor;
import io.jenkins.plugins.services.impl.ConfluenceDirectExtractor;
import io.jenkins.plugins.services.impl.GithubExtractor;
import io.jenkins.plugins.services.impl.HttpClientWikiService;
import io.jenkins.plugins.services.impl.WikiExtractor;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
    // heading inserted by plugin site, should be removed here
    Assert.assertThat(content.toLowerCase(Locale.US),
        CoreMatchers.not(CoreMatchers.containsString("<h1")));
    // check removal of padding class that makes embedding hard
    Assert.assertThat(content,
        CoreMatchers.not(CoreMatchers.containsString(GithubExtractor.BOOTSTRAP_PADDING_5)));
  }

  @Test
  public void testGetWikiContent404() {
    final String url = "https://wiki.jenkins.io/display/JENKINS/H2+API+Plugin";
    final String content = wikiService.getWikiContent(url);
    Assert.assertNotNull("Wiki content is null", content);
    // if we know it's a 404, show "not found" rather than a link
    Assert.assertEquals(HttpClientWikiService.getNoDocumentationFound(), content);
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
  public void testCleanWikiContentConfluence() throws IOException {
    final File file = new File("src/test/resources/wiki_content.html");
    final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    final String cleanContent = ConfluenceDirectExtractor.cleanWikiContent(content, wikiService);
    Assert.assertNotNull("Wiki content is null", cleanContent);
    assertAllLinksMatch(cleanContent, "https?://.*", "https://.*");
  }
  
  @Test
  public void testCleanWikiContentGithub() throws IOException {
    final File file = new File("src/test/resources/github_content.html");
    final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    final String cleanContent = new GithubExtractor().extractHtml(content, 
        "https://github.com/jenkinsci/configuration-as-code-plugin", wikiService);
    Assert.assertNotNull("Wiki content is null", cleanContent);
    assertAllLinksMatch(cleanContent, "(#|https?://).*", "https?://.*");
  }
  
  @Test
  public void testCleanWikiExcerptGithub() throws IOException {
    final File file = new File("src/test/resources/github_excerpt.html");
    final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    final String cleanContent = new GithubExtractor().extractHtml(content, 
        "https://github.com/jenkinsci/configuration-as-code-plugin", wikiService);
    Assert.assertNotNull("Wiki content is null", cleanContent);
    String hrefRegexp = "#getting-started|https://github.com/jenkinsci/configuration-as-code-plugin/blob/master/.*";
    String srcRegexp = "https://cdn.jsdelivr.net/gh/jenkinsci/configuration-as-code-plugin@master/.*"
         + "|https://camo.githubusercontent.com/[a-z0-9]*/[a-z0-9]*";
    assertAllLinksMatch(cleanContent, hrefRegexp, srcRegexp);
  }
  
  private void assertAllLinksMatch(String content, String hrefRegexp, String srcRegexp) {
    final Document html = Jsoup.parseBodyFragment(content);
    html.getElementsByAttribute("href").forEach(element -> {
      final String value = element.attr("href");
      Assert.assertTrue("Wiki content not clean - href references to root : " + value, value.matches(hrefRegexp));
    });
    html.getElementsByAttribute("src").forEach(element -> {
      final String value = element.attr("src");
      Assert.assertTrue("Wiki content not clean - src references to root : " + value, value.matches(srcRegexp));
    });
  }

  @Test
  public void testReplaceAttribute() throws IOException {
    final String host = "https://wiki.jenkins.io";
    final String basePath = "/display/JENKINS/";

    final String src = "/some-image.jpg";
    final Element element = makeImage(src);
    wikiService.replaceAttribute(element, "src", host, basePath);
    Assert.assertEquals("Attribute replacement failed", host + src, element.attr("src"));

    final Element elementAbsolute = makeImage(host + src);
    wikiService.replaceAttribute(elementAbsolute, "src", host, basePath);
    Assert.assertEquals("Attribute replacement failed", host + src, elementAbsolute.attr("src"));
  }

  @Test
  public void testReplaceAttributeRelative() throws IOException {
    final String host = "https://github.com";
    final String basePath = "/jenkinsci/beer-plugin/";

    final String srcRelative = "some-image.jpg";
    final Element element = makeImage(srcRelative);
    wikiService.replaceAttribute(element, "src", host, basePath);
    Assert.assertEquals("Attribute replacement failed", host + basePath + srcRelative, element.attr("src"));
  }

  private Element makeImage(String src) {
    return Jsoup.parseBodyFragment(String.format("<img id=\"test-image\" src=\"%s\"/>", src)).getElementById("test-image");
  }

  @Test
  public void testConfluenceApiExtractor() {
    ConfluenceApiExtractor confluenceApi = new ConfluenceApiExtractor();
    assertInvalid(confluenceApi, "https://wiki.jenkins-ci.org");
    assertInvalid(confluenceApi, "https://wiki.jenkins.io");
    assertInvalid(confluenceApi, "https://wiki.jenkins.io/x/123");
    assertInvalid(confluenceApi, "https://wiki.jenkins.io/display/JENKINS/Git+Plugin/2");
    assertValid(confluenceApi, "https://wiki.jenkins.io/display/JENKINS/Git+Plugin");
    assertValid(confluenceApi, "https://wiki.jenkins.io/display/JENKINS/Git+Plugin/");
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
