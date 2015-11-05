package com.capitalone.dashboard.model;

import com.capitalone.dashboard.model.CollectorItem;

public class FortifyProject extends CollectorItem {
    private static final String INSTANCE_URL = "instanceUrl";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_VERSION = "projectversion";
    private static final String PROJECT_ID = "projectId";

    public String getInstanceUrl() {
        return (String) getOptions().get(INSTANCE_URL);
    }

    public void setInstanceUrl(String instanceUrl) {
        getOptions().put(INSTANCE_URL, instanceUrl);
    }

    public String getProjectId() {
        return (String) getOptions().get(PROJECT_ID);
    }

    public void setProjectId(String id) {
        getOptions().put(PROJECT_ID, id);
    }

    public String getProjectName() {
        return (String) getOptions().get(PROJECT_NAME);
    }

    public void setProjectName(String name) {
        getOptions().put(PROJECT_NAME, name);
    }

    public String getProjectVersion() {
        return (String) getOptions().get(PROJECT_VERSION);
    }

    public void setProjectVersion(String version) {
        getOptions().put(PROJECT_VERSION, version);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FortifyProject that = (FortifyProject) o;
        return (getProjectId() == that.getProjectId()) && getInstanceUrl().equals(that.getInstanceUrl()) && getProjectVersion().equals(that.getProjectVersion());
    }

    @Override
    public int hashCode() {
        int result = getInstanceUrl().hashCode();
        result = 31 * result + new Long(getProjectId()).hashCode();
        return result;
    }
}
