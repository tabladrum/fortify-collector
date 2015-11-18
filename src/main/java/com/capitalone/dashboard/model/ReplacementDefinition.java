
package com.capitalone.dashboard.model;

public class ReplacementDefinition {
  private final String key;
  private final String value;

  public ReplacementDefinition(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return this.key;
  }

  public String getValue() {
    return this.value;
  }

}
