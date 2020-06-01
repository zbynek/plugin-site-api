package io.jenkins.plugins.services.impl;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.PluginRelease;
import io.jenkins.plugins.models.PluginReleases;
import io.jenkins.plugins.models.Scm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.Assert.assertEquals;

public class HttpClientGithubReleasesTest {
  @Rule
  public WireMockRule wireMockRule = new WireMockRule();

  private HttpClientGithubReleases httpClientGithubReleases;

  static final private Plugin lighthouseReportPlugin = new Plugin() {
    @Override
    public Scm getScm() {
      return new Scm() {
        @Override
        public String getLink() {
          return "https://github.com/jenkinsci/lighthouse-report-plugin";
        }
      };
    }
  };
  static final private Plugin badScmPlugin = new Plugin() {
    @Override
    public Scm getScm() {
      return new Scm() {
        @Override
        public String getLink() {
          return "https://randomwebsite.com/for/some/reason";
        }
      };
    }
  };

  @Before
  public void setUp() {
    // wireMockRule.enableRecordMappings()

    this.httpClientGithubReleases = new HttpClientGithubReleases(new DefaultConfigurationService() {
      @Override
      public String getGithubApiBase() {
        return wireMockRule.baseUrl();
      }
    });
  }

  @Test
  public void getReleasesHappy() throws Exception {
    stubFor(get("/repos/jenkinsci/lighthouse-report-plugin/releases?client_id=null&client_secret=null")
      .willReturn(okJson("[\n" +
        "  {\n" +
        "    \"url\": \"https://api.github.com/repos/jenkinsci/lighthouse-report-plugin/releases/26831370\",\n" +
        "    \"assets_url\": \"https://api.github.com/repos/jenkinsci/lighthouse-report-plugin/releases/26831370/assets\",\n" +
        "    \"upload_url\": \"https://uploads.github.com/repos/jenkinsci/lighthouse-report-plugin/releases/26831370/assets{?name,label}\",\n" +
        "    \"html_url\": \"https://github.com/jenkinsci/lighthouse-report-plugin/releases/tag/lighthouse-report-0.1.0\",\n" +
        "    \"id\": 26831370,\n" +
        "    \"node_id\": \"MDc6UmVsZWFzZTI2ODMxMzcw\",\n" +
        "    \"tag_name\": \"lighthouse-report-0.1.0\",\n" +
        "    \"target_commitish\": \"master\",\n" +
        "    \"name\": \"Lighthouse Report Plugin - 0.1.0\",\n" +
        "    \"draft\": false,\n" +
        "    \"author\": {\n" +
        "      \"login\": \"github-actions[bot]\",\n" +
        "      \"id\": 41898282,\n" +
        "      \"node_id\": \"MDM6Qm90NDE4OTgyODI=\",\n" +
        "      \"avatar_url\": \"https://avatars2.githubusercontent.com/in/15368?v=4\",\n" +
        "      \"gravatar_id\": \"\",\n" +
        "      \"url\": \"https://api.github.com/users/github-actions%5Bbot%5D\",\n" +
        "      \"html_url\": \"https://github.com/apps/github-actions\",\n" +
        "      \"followers_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/followers\",\n" +
        "      \"following_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/following{/other_user}\",\n" +
        "      \"gists_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/gists{/gist_id}\",\n" +
        "      \"starred_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/starred{/owner}{/repo}\",\n" +
        "      \"subscriptions_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/subscriptions\",\n" +
        "      \"organizations_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/orgs\",\n" +
        "      \"repos_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/repos\",\n" +
        "      \"events_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/events{/privacy}\",\n" +
        "      \"received_events_url\": \"https://api.github.com/users/github-actions%5Bbot%5D/received_events\",\n" +
        "      \"type\": \"Bot\",\n" +
        "      \"site_admin\": false\n" +
        "    },\n" +
        "    \"prerelease\": false,\n" +
        "    \"created_at\": \"2020-05-24T00:27:05Z\",\n" +
        "    \"published_at\": \"2020-05-24T00:30:23Z\",\n" +
        "    \"assets\": [\n" +
        "\n" +
        "    ],\n" +
        "    \"tarball_url\": \"https://api.github.com/repos/jenkinsci/lighthouse-report-plugin/tarball/lighthouse-report-0.1.0\",\n" +
        "    \"zipball_url\": \"https://api.github.com/repos/jenkinsci/lighthouse-report-plugin/zipball/lighthouse-report-0.1.0\",\n" +
        "    \"body\": \"<!-- Optional: add a release summary here -->\\r\\n\\r\\n## \uD83D\uDE80 New features and improvements\\r\\n\\r\\n* Enable multiple reports (#2) @nishant-gupta\\r\\n\\r\\n## \uD83D\uDCE6 Dependency updates\\r\\n\\r\\n* Bump react-lighthouse-viewer from 2.0.0 to 2.8.0 (#7) @dependabot\\r\\n* [Security] Bump acorn from 5.7.3 to 5.7.4 (#6) @dependabot\\r\\n* Bump plugin from 3.50 to 4.2 (#3) @dependabot\\r\\n* Bump babel-eslint from 10.0.3 to 10.1.0 (#5) @dependabot\\r\\n* Bump react from 16.10.2 to 16.13.1 (#9) @dependabot\\r\\n* Bump eslint from 6.5.1 to 6.8.0 (#10) @dependabot\\r\\n\"\n" +
        "  }\n" +
        "]\n")));

    PluginReleases releases = this.httpClientGithubReleases.getReleases(lighthouseReportPlugin);
    assertEquals(1, releases.getReleases().size());
    assertEquals(
      new PluginRelease(
        "lighthouse-report-0.1.0",
        "Lighthouse Report Plugin - 0.1.0",
        Date.from(Instant.ofEpochMilli(1590280223000L)),
        "<!-- Optional: add a release summary here -->\r\n\r\n## 🚀 New features and improvements\r\n\r\n* Enable multiple reports (#2) @nishant-gupta\r\n\r\n## 📦 Dependency updates\r\n\r\n* Bump react-lighthouse-viewer from 2.0.0 to 2.8.0 (#7) @dependabot\r\n* [Security] Bump acorn from 5.7.3 to 5.7.4 (#6) @dependabot\r\n* Bump plugin from 3.50 to 4.2 (#3) @dependabot\r\n* Bump babel-eslint from 10.0.3 to 10.1.0 (#5) @dependabot\r\n* Bump react from 16.10.2 to 16.13.1 (#9) @dependabot\r\n* Bump eslint from 6.5.1 to 6.8.0 (#10) @dependabot\r\n"
      ),
      releases.getReleases().get(0)
    );
  }

  @Test
  public void getReleasesBadScmLink() throws Exception {
    PluginReleases releases = this.httpClientGithubReleases.getReleases(badScmPlugin);
    assertEquals(0, releases.getReleases().size());
  }
}
