package org.boundbox.model;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(exclude = { "effectiveInheritanceLevel", "fieldType" })
@ToString
public class FieldInfo implements Inheritable {
    private @Getter
    String fieldName;
    private TypeMirror fieldType;
    private @Getter
    int inheritanceLevel;
    private @Getter
    @Setter
    int effectiveInheritanceLevel;
    private @Getter
    @Setter
    boolean staticField;

    public FieldInfo(VariableElement e) {
        fieldName = e.getSimpleName().toString();
        fieldType = e.asType();
    }

    public FieldInfo(String fieldName, TypeMirror fieldType) {
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

    @Override
    public boolean isInherited() {
        return inheritanceLevel != 0;
    }

}
