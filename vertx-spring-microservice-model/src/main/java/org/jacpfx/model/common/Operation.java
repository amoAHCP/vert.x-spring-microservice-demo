package org.jacpfx.model.common;

/**
 * Created by amo on 27.10.14.
 */
public class Operation {
    private String url;
    private String type;
    private String[] parameter;

    public Operation(String url, String type, String... param) {
        this.url = url;
        this.type = type;
        this.parameter = param;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getParameter() {
        return parameter;
    }

    public void setParameter(String[] parameter) {
        this.parameter = parameter;
    }
}
