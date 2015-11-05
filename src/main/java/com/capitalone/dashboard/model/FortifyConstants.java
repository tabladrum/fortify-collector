
package com.capitalone.dashboard.model;

import org.apache.commons.lang.StringUtils;



import java.text.Normalizer;
import java.util.Locale;

public class FortifyConstants {

  public static final String AUDIT_FVDL_FILE = "audit.fvdl";


  private FortifyConstants() {
    // only static stuff
  }

  public static String fortifyRepositoryKey(String language) {
    return "fortify-" + StringUtils.lowerCase(language);
  }


  public static String fortifySQRuleKey(String kingdom, String category, String subcategory) {
    StringBuilder sb = new StringBuilder();
    if (StringUtils.isNotBlank(kingdom)) {
      sb.append(slugifyForKey(kingdom)).append("_");
    }
    if (StringUtils.isNotBlank(category)) {
      sb.append(slugifyForKey(category));
    }
    if (StringUtils.isNotBlank(subcategory)) {
      sb.append("_");
      sb.append(slugifyForKey(subcategory));
    }
    return sb.length() > 0 ? sb.toString() : null;
  }

  private static String slugifyForKey(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")
      .replaceAll("[^\\w+]", "_")
      .replaceAll("\\s+", "_")
      .replaceAll("[-]+", "_")
      .replaceAll("^_", "")
      .replaceAll("_$", "").toLowerCase(Locale.ENGLISH);
  }
}
