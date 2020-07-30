package io.jenkins.plugins.services.impl;

import io.jenkins.plugins.services.ConfigurationService;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpClient {
  protected Logger logger = LoggerFactory.getLogger(HttpClient.class);

  protected final ConfigurationService configurationService;

  @Inject
  protected HttpClient(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  protected CloseableHttpClient getHttpClient(String url) {
    final RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
      .setConnectionRequestTimeout(5000)
      .setConnectTimeout(5000)
      .setSocketTimeout(5000)
      .build();
    HttpClientBuilder httpClientBuilder = HttpClients.custom();
    return httpClientBuilder.setDefaultRequestConfig(requestConfig).build();
  }

  public String getHttpContent(String url, List<Header> headers) {
    final HttpGet get = new HttpGet(url);
    headers.stream().forEach(get::setHeader);
    if (url.startsWith(this.configurationService.getGithubApiBase())) {
      this.configurationService.getGithubCredentials().stream().forEach(get::setHeader);;
    } else if (url.startsWith(this.configurationService.getJiraURL())) {
      this.configurationService.getJiraCredentials().stream().forEach(get::setHeader);
    }

    try (final CloseableHttpClient httpClient = getHttpClient(url);
         final CloseableHttpResponse response = httpClient.execute(get)) {
      if (this.isValidStatusCode(response.getStatusLine().getStatusCode())) {
        final HttpEntity entity = response.getEntity();
        final String html = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        EntityUtils.consume(entity);
        return html;
      } else {
        final String msg = String.format("Unable to get content from %s - returned status code %d", url,
          response.getStatusLine().getStatusCode());
        logger.warn(msg);
        return null;
      }
    } catch (IOException e) {
      final String msg = "Problem getting content " + url;
      logger.error(msg, e);
      return null;
    }
  }

  protected boolean isValidStatusCode(int statusCode) {
    return statusCode == HttpStatus.SC_OK;
  }
}
