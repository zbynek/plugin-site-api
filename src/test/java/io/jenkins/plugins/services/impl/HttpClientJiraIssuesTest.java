package io.jenkins.plugins.services.impl;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Strings;
import io.jenkins.plugins.models.JiraIssues;
import org.apache.http.client.CredentialsProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

public class HttpClientJiraIssuesTest {
  static protected final String WIREMOCK_PATH = "src/test/resources/wiremocks/HttpClientJiraIssuesTest";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(
    options()
      .dynamicPort()
      .usingFilesUnderDirectory(WIREMOCK_PATH)
  );

  private HttpClientJiraIssues httpClientJiraIssues;

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
        .willReturn(aResponse().proxiedFrom("https://issues.jenkins-ci.org")));
    }

    this.httpClientJiraIssues = new HttpClientJiraIssues(new DefaultConfigurationService() {
      @Override
      protected String getJiraUsername() {
        String username = super.getJiraUsername();
        if (Strings.isNullOrEmpty(username)) {
          return "username";
        }
        return username;
      }

      @Override
      protected String getJiraPassword() {
        String password = super.getJiraPassword();
        if (Strings.isNullOrEmpty(password)) {
          return "password";
        }
        return password;
      }

      @Override
      public String getJiraURL() {
        return wireMockRule.baseUrl();
      }
    });
  }

  @After
  public void tearDown() {
    // "If-None-Match" header used for ETag matching for caching connections
    // "Accept" header is used to specify previews. If it changes expected data may not be retrieved.
    wireMockRule
      .snapshotRecord(recordSpec().forTarget("https://issues.jenkins-ci.org/")
        .captureHeader("If-None-Match")
        .captureHeader("If-Modified-Since")
        .captureHeader("Cache-Control")
        .captureHeader("Accept")
        .extractTextBodiesOver(255));

  }

  @Test
  public void testHappyPath() throws IOException {
    JiraIssues issues = this.httpClientJiraIssues.getIssues("git");

    assertEquals(512, issues.getTotal());
    assertEquals(512, issues.issues.size());
  }

  @Test
  public void testComponentNotFoundName() throws IOException {
    JiraIssues issues = this.httpClientJiraIssues.getIssues("cors-filter-plugin");

    assertEquals(0, issues.getTotal());
    assertEquals(0, issues.issues.size());
  }

}
