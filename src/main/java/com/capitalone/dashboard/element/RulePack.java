
package com.capitalone.dashboard.element;

import com.capitalone.dashboard.model.FortifyDescription;
import com.capitalone.dashboard.model.FortifyRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RulePack {
  private String name;
  private String language;
  private final Map<String, String> descriptions = new HashMap<String, String>();
  private final Collection<FortifyRule> rules = new ArrayList<FortifyRule>();

  public String name() {
    return this.name;
  }

  public RulePack setName(String name) {
    this.name = name;
    return this;
  }

  public String language() {
    return this.language;
  }

  public RulePack setLanguage(String language) {
    this.language = language;
    return this;
  }

  public RulePack addDescription(FortifyDescription newDescription) {
    this.descriptions.put(newDescription.getId(), newDescription.toString());
    return this;
  }

  public Collection<FortifyRule> getRules() {
    return this.rules;
  }

  public RulePack addRule(FortifyRule rule) {
    this.rules.add(rule);
    return this;
  }

  public String getRuleLanguage(FortifyRule rule) {
    String ruleLanguage = rule.getLanguage();
    if (ruleLanguage == null) {
      ruleLanguage = this.language;
    }

    return ruleLanguage;
  }

  public String getHTMLDescription(FortifyDescription description) {
    String htmlDescription;
    String ref = description.getRef();
    if (ref == null) {
      htmlDescription = description.toString();
    } else {
      htmlDescription = this.descriptions.get(ref);
    }

    return htmlDescription;
  }

}
