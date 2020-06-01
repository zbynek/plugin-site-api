package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class PluginReleases {
  public ArrayList<PluginRelease> getReleases() {
    return releases;
  }

  @JsonProperty("releases")
  private final ArrayList<PluginRelease> releases;

  public PluginReleases(ArrayList<PluginRelease> releases) {
    this.releases = releases;
  }
}
