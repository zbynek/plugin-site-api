package io.jenkins.plugins.services.impl;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.PluginRelease;
import io.jenkins.plugins.models.PluginReleases;
import io.jenkins.plugins.models.Scm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

public class HttpClientGithubReleasesTest {
  static protected final String WIREMOCK_PATH = "src/test/resources/wiremocks/HttpClientGithubReleasesTest";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(
    options()
      .dynamicPort()
      .usingFilesUnderDirectory(WIREMOCK_PATH)
  );

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
    if (Boolean.getBoolean("enable.recording")) {
      File recordingsDir = new File(WIREMOCK_PATH + "/mappings");
      recordingsDir.mkdirs();
      File filesDir = new File(WIREMOCK_PATH + "/__files");
      filesDir.mkdirs();
      wireMockRule.enableRecordMappings(
        new SingleRootFileSource(recordingsDir.getAbsolutePath()),
        new SingleRootFileSource(filesDir.getAbsolutePath())
      );

      stubFor(get(urlMatching(".*")).atPriority(10)
        .willReturn(aResponse().proxiedFrom("https://api.github.com")));
    }

    this.httpClientGithubReleases = new HttpClientGithubReleases(new DefaultConfigurationService() {
      @Override
      public String getGithubClientId() {
        return "";
      }

      @Override
      public String getGithubClientSecret() {
        return "";
      }

      @Override
      public String getGithubApiBase() {
        return wireMockRule.baseUrl();
      }
    });
  }

  @Test
  public void getReleasesHappy() throws Exception {
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
