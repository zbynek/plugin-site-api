package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginRelease {
  @JsonProperty("tag_name") final private String tagName;
  @JsonProperty("name") final private String name;
  @JsonProperty("published_at") final private Date publishedAt;
  @JsonProperty("body") private String body;

  public PluginRelease() {
    this("", "", new Date(), "");
  }

  public PluginRelease(String tagName, String name, Date publishedAt, String body) {
    this.tagName = tagName;
    this.name = name;
    this.publishedAt = publishedAt;
    this.body = body;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PluginRelease)) {
      return false;
    }
    return this.tagName.equals(((PluginRelease) obj).tagName) &&
      this.name.equals(((PluginRelease) obj).name) &&
      this.publishedAt.equals(((PluginRelease) obj).publishedAt) &&
      this.body.equals(((PluginRelease) obj).body);
  }
}
