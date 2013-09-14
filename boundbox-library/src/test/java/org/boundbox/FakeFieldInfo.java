package org.boundbox;

import org.boundbox.model.FieldInfo;

public class FakeFieldInfo extends FieldInfo {

    private String fieldTypeName;

    public FakeFieldInfo(String fieldName, String fieldTypeName) {
        super(fieldName, null);
        this.fieldTypeName = fieldTypeName;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }
}
