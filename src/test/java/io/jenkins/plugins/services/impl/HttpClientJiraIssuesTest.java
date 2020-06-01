package io.jenkins.plugins.services.impl;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.plugins.models.JiraIssues;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

public class HttpClientJiraIssuesTest {
  static final private String WIREMOCK_DIR = "./src/test/resources/wiremocks/HttpClientJiraIssuesTest";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(
    options().fileSource(new SingleRootFileSource(WIREMOCK_DIR))
  );
  private HttpClientJiraIssues httpClientJiraIssues;

  @Before
  public void setUp() {
    wireMockRule.startRecording("https://issues.jenkins-ci.org/");
    wireMockRule.enableRecordMappings(
      new SingleRootFileSource(WIREMOCK_DIR  + "/recordings"),
      new SingleRootFileSource(WIREMOCK_DIR  + "/files")
    );

    this.httpClientJiraIssues = new HttpClientJiraIssues(new DefaultConfigurationService() {
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
  public void getIssues() throws IOException {
    JiraIssues issues = this.httpClientJiraIssues.getIssues("git");

    assertEquals(512, issues.getTotal());
    assertEquals(512, issues.issues.size());
  }
}
