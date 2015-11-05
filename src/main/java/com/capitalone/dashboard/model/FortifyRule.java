
package com.capitalone.dashboard.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.annotation.CheckForNull;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FortifyRule {
  // According to Fortify documentation 3.90, language is not in the XSD, but it's in their RulePack :/
  private String language;
  private FormatVersion formatVersion;
  private String ruleID;
  private String notes;
  private String vulnKingdom;
  private String vulnCategory;
  private String vulnSubcategory;
  private String defaultSeverity;
  private FortifyDescription description;

  public String getLanguage() {
    return this.language;
  }

  public FortifyRule setLanguage(String language) {
    this.language = language;
    return this;
  }

  public FormatVersion getFormatVersion() {
    return this.formatVersion;
  }

  public FortifyRule setFormatVersion(String formatVersion) {
    this.formatVersion = new FormatVersion(formatVersion);
    return this;
  }

  public String getRuleID() {
    return this.ruleID;
  }

  public FortifyRule setRuleID(String ruleID) {
    this.ruleID = ruleID;
    return this;
  }

  public FortifyRule setNotes(String notes) {
    this.notes = notes;
    return this;
  }

  public FortifyRule setVulnCategory(String vulnCategory) {
    this.vulnCategory = vulnCategory;
    return this;
  }

  public FortifyRule setVulnKingdom(String vulnKingdom) {
    this.vulnKingdom = vulnKingdom;
    return this;
  }

  public FortifyRule setVulnSubcategory(String vulnSubcategory) {
    this.vulnSubcategory = vulnSubcategory;
    return this;
  }

  public String getDefaultSeverity() {
    return this.defaultSeverity;
  }

  public FortifyRule setDefaultSeverity(String defaultSeverity) {
    this.defaultSeverity = defaultSeverity;
    return this;
  }

  public FortifyDescription getDescription() {
    return this.description;
  }

  public FortifyRule setDescription(FortifyDescription description) {
    this.description = description;
    return this;
  }

  public String getNotes() {
    return this.notes;
  }

  @CheckForNull
  public String getSonarKey() {
    return FortifyConstants.fortifySQRuleKey(this.vulnKingdom, this.vulnCategory, this.vulnSubcategory);
  }

  @CheckForNull
  public String getName() {
    if (StringUtils.isNotBlank(this.vulnCategory)) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.vulnCategory);
      if (StringUtils.isNotBlank(this.vulnSubcategory)) {
        sb.append(": ");
        sb.append(this.vulnSubcategory);
      }
      return sb.toString();
    } else if (StringUtils.isNotBlank(this.vulnSubcategory)) {
      return this.vulnSubcategory;
    }
    return null;
  }

  public String[] getTags() {
    List<String> tags = new ArrayList<String>();
    if (StringUtils.isNotBlank(this.vulnKingdom)) {
      tags.add(slugify(this.vulnKingdom));
    }
    if (StringUtils.isNotBlank(this.vulnCategory)) {
      tags.add(slugify(this.vulnCategory));
    }
    if (StringUtils.isNotBlank(this.vulnSubcategory)) {
      tags.add(slugify(this.vulnSubcategory));
    }
    return tags.toArray(new String[tags.size()]);
  }

  private static String slugify(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")
      .replaceAll("[^\\w+]", "-")
      .replaceAll("\\s+", "-")
      .replaceAll("_", "-")
      .replaceAll("[-]+", "-")
      .replaceAll("^-", "")
      .replaceAll("-$", "").toLowerCase(Locale.ENGLISH);
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this).toString();
  }

}
