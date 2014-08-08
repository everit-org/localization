package org.everit.osgi.localization.ri.internal;

public class LocalizedValue implements Comparable<LocalizedValue> {

    private boolean defaultValue;

    private String languageTag;

    private String value;

    @Override
    public int compareTo(LocalizedValue o) {
        int compareTo = languageTag.compareTo(o.getLanguageTag());
        if (compareTo != 0) {
            return compareTo;
        }
        return value.compareTo(o.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LocalizedValue other = (LocalizedValue) obj;
        if (languageTag == null) {
            if (other.languageTag != null) {
                return false;
            }
        } else if (!languageTag.equals(other.languageTag)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((languageTag == null) ? 0 : languageTag.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setLanguageTag(String languageTag) {
        this.languageTag = languageTag;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
