package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dependency {

  @JsonProperty("name")
  private String name;

  @JsonProperty("title")
  private String title;

  @JsonProperty("optional")
  private boolean optional;

  @JsonProperty("version")
  private String version;

  @JsonProperty("implied")
  private boolean implied;

  public Dependency() {
  }

  public Dependency(String name, String title, boolean optional, String version) {
    this(name, title, optional, version, false);
  }

  public Dependency(String name, String title, boolean optional, String version, boolean implied) {
    this.name = name;
    this.title = title;
    this.optional = optional;
    this.version = version;
    this.implied = implied;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isImplied() {
    return implied;
  }

  public void setImplied(boolean implied) {
    this.implied = implied;
  }
}
