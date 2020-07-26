package io.jenkins.plugins.models;

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class PluginReleaseTest {

  @Test
  public void disallowsScaryHtml() {
    PluginRelease pluginRelease = new PluginRelease(
      "tagName",
      "name",
      new Date(),
      "https://github.com/jenkinsci/lighthouse-report-plugin/releases/tag/lighthouse-report-0.2",
      "# header1\n## header2\n <script>alert('hi')</script>"
    );
    assertThat(
      pluginRelease.getBodyHTML(),
      containsString("&lt;script&gt;alert('hi')&lt;/script&gt;")
    );
  }
}
