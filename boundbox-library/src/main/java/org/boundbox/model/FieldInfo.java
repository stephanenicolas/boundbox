package org.boundbox.model;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(exclude = { "effectiveInheritanceLevel", "fieldType" })
@ToString
@SuppressWarnings("PMD.UnusedPrivateField")
public class FieldInfo implements Inheritable {
    @Getter
    private String fieldName;
    private TypeMirror fieldType;
    @Getter
    private int inheritanceLevel;
    @Getter
    @Setter
    private int effectiveInheritanceLevel;
    @Getter
    @Setter
    private boolean staticField;

    public FieldInfo(@NonNull VariableElement e) {
        fieldName = e.getSimpleName().toString();
        fieldType = e.asType();
    }

    public FieldInfo(@NonNull String fieldName, TypeMirror fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getFieldTypeName() {
        return fieldType.toString();
    }

    public void setInheritanceLevel(int inheritanceLevel) {
        this.inheritanceLevel = inheritanceLevel;
        this.effectiveInheritanceLevel = inheritanceLevel;
    }

}
