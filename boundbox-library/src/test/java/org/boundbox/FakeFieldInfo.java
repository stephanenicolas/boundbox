package org.boundbox;

import lombok.NonNull;

import org.boundbox.model.FieldInfo;

public class FakeFieldInfo extends FieldInfo {

    private String fieldTypeName;

    public FakeFieldInfo(@NonNull String fieldName, @NonNull String fieldTypeName) {
        super(fieldName, null);
        this.fieldTypeName = fieldTypeName;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }
}
