package org.boundbox.model;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(exclude = { "effectiveInheritanceLevel" })
@ToString
@SuppressWarnings("PMD.UnusedPrivateField")
public class FieldInfo implements Inheritable {
    @Getter
    private String fieldName;
    @Getter
    @Setter
    private String fieldTypeName;
    @Getter
    private int inheritanceLevel;
    @Getter
    @Setter
    private int effectiveInheritanceLevel;
    @Getter
    @Setter
    private boolean staticField;
    @Getter
    @Setter
    private boolean isFinalField;

    public FieldInfo(@NonNull VariableElement e) {
        fieldName = e.getSimpleName().toString();
        fieldTypeName = e.asType().toString();
    }

    public FieldInfo(@NonNull String fieldName, TypeMirror fieldType) {
        this.fieldName = fieldName;
        this.fieldTypeName = fieldType.toString();
    }
    
    public FieldInfo(@NonNull String fieldName, @NonNull String fieldTypeName) {
        this.fieldName = fieldName;
        this.fieldTypeName = fieldTypeName;
    }

    public void setInheritanceLevel(int inheritanceLevel) {
        this.inheritanceLevel = inheritanceLevel;
        this.effectiveInheritanceLevel = inheritanceLevel;
    }

}
