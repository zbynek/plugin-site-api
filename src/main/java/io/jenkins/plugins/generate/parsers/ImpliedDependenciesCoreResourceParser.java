package io.jenkins.plugins.generate.parsers;

import hudson.util.VersionNumber;
import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Dependency;
import io.jenkins.plugins.models.Plugin;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adds implied dependencies to detached plugins if a plugin's core dependency is old.
 */
public class ImpliedDependenciesCoreResourceParser implements PluginDataParser {

  private final Map<String, String> dependencyNameToTitleMap;

  private static class Detachment {
    String plugin;
    VersionNumber detachedCore;
    VersionNumber impliedVersion;
  }

  private List<Detachment> detachments = new ArrayList<>();

  public ImpliedDependenciesCoreResourceParser(JSONObject updateCenterJson) {
    final JSONObject pluginsJson = updateCenterJson.getJSONObject("plugins");
    dependencyNameToTitleMap = buildDependencyNameToTitleMap(pluginsJson);

    String dataUrl = "https://raw.githubusercontent.com/jenkinsci/jenkins/master/core/src/main/resources/jenkins/split-plugins.txt";
    try {
      for (String line : IOUtils.readLines(new URL(dataUrl).openConnection().getInputStream(), Charset.forName("UTF-8"))) {
        if (line.trim().isEmpty() || line.trim().startsWith("#")) {
          continue;
        }
        String[] parts = line.split(" ");
        Detachment d = new Detachment();
        d.plugin = parts[0];
        d.detachedCore = new VersionNumber(parts[1]);
        d.impliedVersion = new VersionNumber(parts[2]);
        detachments.add(d);
      }
    } catch (IOException ex) {
      // ignore
    }
  }

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    List<Dependency> impliedDependencies = new ArrayList<>();
    VersionNumber requiredCore = new VersionNumber(plugin.getRequiredCore());
    for (Detachment d : detachments) {
      if (requiredCore.isOlderThan(d.detachedCore) && !d.plugin.equals(plugin.getName())) {
        impliedDependencies.add(new Dependency(d.plugin, dependencyNameToTitleMap.get(d.plugin), false, d.impliedVersion.toString(), true));
      }
    }
    plugin.addDependencies(impliedDependencies);
  }

  private Map<String, String> buildDependencyNameToTitleMap(JSONObject pluginsJson) {
    return pluginsJson.keySet().stream()
      .map(pluginsJson::getJSONObject)
      .collect(Collectors.toMap(plugin -> plugin.getString("name"), plugin -> plugin.getString("title")));
  }
}
