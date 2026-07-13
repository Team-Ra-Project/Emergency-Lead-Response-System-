package model;

import java.io.Serializable;

/** Simple key/value setting row for the Settings module. */
public class Setting implements Serializable {
    private String key;
    private String value;

    public Setting() {}
    public Setting(String key, String value) { this.key = key; this.value = value; }

    public String getKey() { return key; }
    public void setKey(String v) { key = v; }
    public String getValue() { return value; }
    public void setValue(String v) { value = v; }
}
