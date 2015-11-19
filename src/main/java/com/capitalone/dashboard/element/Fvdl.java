package com.capitalone.dashboard.element;

import com.capitalone.dashboard.element.Description;
import com.capitalone.dashboard.element.Vulnerability;
import com.capitalone.dashboard.model.ReplacementDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class Fvdl {
  private final com.capitalone.dashboard.element.Build build;
  private final Map<String, String> descriptions = new HashMap<String, String>();
  private final Collection<Vulnerability> vulnerabilities;

  public Fvdl(com.capitalone.dashboard.element.Build build, Collection<Description> descriptions, Collection<Vulnerability> vulnerabilities) {
    this.build = build;
    for (Description description : descriptions) {
      this.descriptions.put(description.getClassID(), description.getAbstract());
    }
    this.vulnerabilities = vulnerabilities;
  }

  public com.capitalone.dashboard.element.Build getBuild() {
    return this.build;
  }

  public Collection<Vulnerability> getVulnerabilities() {
    return this.vulnerabilities;
  }

  public String getDescription(Vulnerability vulnerability) {
    String message = "No message found";
    String abstractDescription = this.descriptions.get(vulnerability.getClassID());
    if (abstractDescription != null) {
      message = abstractDescription;
      for (ReplacementDefinition replacementDefinition : vulnerability.getReplacementDefinitions()) {
        String key = replacementDefinition.getKey();
        String value = replacementDefinition.getValue();
        String regex = "<Replace key=\"" + Matcher.quoteReplacement(key) + "\"/>";
        value = Matcher.quoteReplacement(value);
        message = message.replaceAll(regex, value);
      }
      message = message.replaceAll("\\<[^>]*>", "");
    }
    return message;
  }

}
