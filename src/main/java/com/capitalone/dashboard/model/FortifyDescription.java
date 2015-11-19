
package com.capitalone.dashboard.model;

import com.capitalone.dashboard.element.Reference;

import java.util.ArrayList;
import java.util.Collection;

public class FortifyDescription {
  private String id;
  private String ref;
  private String descriptionAbstract;
  private String explanation;
  private String recommendations;
  private final Collection<Reference> references = new ArrayList<Reference>();
  private final Collection<String> tips = new ArrayList<String>();

  public String getId() {
    return this.id;
  }

  public FortifyDescription setId(String id) {
    this.id = id;
    return this;
  }

  public String getRef() {
    return this.ref;
  }

  public FortifyDescription setRef(String ref) {
    this.ref = ref;
    return this;
  }

  public FortifyDescription setDescriptionAbstract(String descriptionAbstract) {
    this.descriptionAbstract = descriptionAbstract;
    return this;
  }

  public FortifyDescription setExplanation(String explanation) {
    this.explanation = explanation;
    return this;
  }

  public FortifyDescription setRecommendations(String recommendations) {
    this.recommendations = recommendations;
    return this;
  }

  public String getRecommendations() {
    return this.recommendations;
  }

  public FortifyDescription addReference(Reference reference) {
    this.references.add(reference);
    return this;
  }

  public FortifyDescription addTip(String tip) {
    this.tips.add(tip);
    return this;
  }

  private String format() {
    StringBuilder builder = new StringBuilder();
    if (this.descriptionAbstract != null) {
      builder.append("<h2>ABSTRACT</h2>").append(this.descriptionAbstract);
    }
    if (this.explanation != null) {
      builder.append("<h2>EXPLANATION</h2>").append(this.explanation);
    }
    if (this.recommendations != null) {
      builder.append("<h2>RECOMMENDATIONS</h2>").append(this.recommendations);
    }
    if (!this.tips.isEmpty()) {
      builder.append("<h2>TIPS</h2><p><ul>");
      for (String tip : this.tips) {
        builder.append("<li>").append(tip).append("</li>");
      }
      builder.append("</ul></p>");
    }
    if (!this.references.isEmpty()) {
      builder.append("<h2>REFERENCES</h2>");
      int index = 0;
      for (Reference reference : this.references) {
        builder.append("<p>[").append(index + 1).append("] ").append(reference.getTitle());
        if (reference.getAuthor() != null) {
          builder.append(" - ").append(reference.getAuthor());
        }
        builder.append("</p>");
        index++;
      }
    }

    return builder.toString();
  }

  @Override
  public String toString() {
    return format();
  }
}
