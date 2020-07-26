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
  
  @Test
  public void rightLink() {
    PluginRelease pluginRelease = new PluginRelease(
      "saml-1.1.5",
      "1.1.5",
      new Date(),
      "https://github.com/jenkinsci/saml-plugin/releases/tag/saml-1.1.5",
      "<!-- Optional: add a release summary here -->\r\n## \uD83D\uDC1B Bug Fixes\r\n\r\n* Handle windows paths (#78) @willwh\r\n\r\n## \uD83D\uDCE6 Dependency updates\r\n\r\n* [JENKINS-60742](https://issues.jenkins-ci.org/browse/JENKINS-60742) - Bump core to 2.176.1 version and bump plugin dependecies (#82) @kuisathaverat\r\n* [JENKINS-60679](https://issues.jenkins-ci.org/browse/JENKINS-60679) - Bump bouncycastle api plugin due NoSuchMethodError exception (#81) @kuisathaverat\r\n\r\n## \uD83D\uDCDD Documentation updates\r\n\r\n* Update TROUBLESHOOTING.md (#80) @duemir\r\n* fix typo (#79) @tehmaspc\r\n"
    );
    assertThat(
      pluginRelease.getBodyHTML(),
      containsString("<h2>\uD83D\uDC1B Bug Fixes</h2>\n<ul>\n<li>Handle windows paths (<a href=\"https://github.com/jenkinsci/saml-plugin/issues/78\">#78</a>) <a href=\"https://github.com/willwh\"><strong>@willwh</strong></a></li>\n</ul>\n<h2>\uD83D\uDCE6 Dependency updates</h2>\n<ul>\n<li><a href=\"https://issues.jenkins-ci.org/browse/JENKINS-60742\">JENKINS-60742</a> - Bump core to 2.176.1 version and bump plugin dependecies (<a href=\"https://github.com/jenkinsci/saml-plugin/issues/82\">#82</a>) <a href=\"https://github.com/kuisathaverat\"><strong>@kuisathaverat</strong></a></li>\n<li><a href=\"https://issues.jenkins-ci.org/browse/JENKINS-60679\">JENKINS-60679</a> - Bump bouncycastle api plugin due NoSuchMethodError exception (<a href=\"https://github.com/jenkinsci/saml-plugin/issues/81\">#81</a>) <a href=\"https://github.com/kuisathaverat\"><strong>@kuisathaverat</strong></a></li>\n</ul>\n<h2>\uD83D\uDCDD Documentation updates</h2>\n<ul>\n<li>Update TROUBLESHOOTING.md (<a href=\"https://github.com/jenkinsci/saml-plugin/issues/80\">#80</a>) <a href=\"https://github.com/duemir\"><strong>@duemir</strong></a></li>\n<li>fix typo (<a href=\"https://github.com/jenkinsci/saml-plugin/issues/79\">#79</a>) <a href=\"https://github.com/tehmaspc\"><strong>@tehmaspc</strong></a></li>\n</ul>\n")
    );
  }
}
