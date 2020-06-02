package io.jenkins.plugins.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.jenkins.plugins.endpoints.PluginEndpoint;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.PluginRelease;
import io.jenkins.plugins.models.PluginReleases;
import io.jenkins.plugins.services.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class HttpClientReleases extends HttpClient {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private Logger logger = LoggerFactory.getLogger(PluginEndpoint.class);

  private final ConfigurationService configurationService;

  public HttpClientReleases(ConfigurationService configurationService) {
    super();
    this.configurationService = configurationService;
  }


  public PluginReleases getReleases(Plugin plugin) throws IOException {
    String clientId = this.configurationService.getGithubClientId();
    String clientSecret = this.configurationService.getGithubClientSecret();

    if (clientId == null) {
      logger.warn("No GitHub Client ID specified, using anonymous credentials to access github api");
    }

    final String[] scmUrl = StringUtils.removeEnd(
      plugin.getScm().getLink().replaceAll("https://github.com/", "").replaceAll( "http://github.com/", ""),
      "/"
    ).split("/");

    if (scmUrl.length != 2) {
      logger.warn("Not sure what to do with SCM URL of " + plugin.getScm().getLink() + " == " + Arrays.toString(scmUrl));
      return new PluginReleases(new ArrayList<>());
    }

    final String URL = String.format(
      "%s/repos/%s/%s/releases?client_id=%s&client_secret=%s",
      this.configurationService.getGithubApiBase(),
      scmUrl[0],
      scmUrl[1],
      clientId,
      clientSecret
    );

    String jsonInput = this.getHttpContent(URL, Collections.emptyList());
    if (Strings.isNullOrEmpty(jsonInput)) {
      throw new IOException("Empty return value");
    }

    return new PluginReleases(objectMapper.readValue(
      jsonInput,
      new TypeReference<ArrayList<PluginRelease>>() {}
    ));
  }
}
