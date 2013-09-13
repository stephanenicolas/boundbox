package org.boundbox.model;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class FieldInfo implements Inheritable {
    private String fieldName;
    private TypeMirror fieldType;
    private int inheritanceLevel;
    private int effectiveInheritanceLevel;
    private boolean staticField;
    
    public FieldInfo( VariableElement e ) {
        fieldName = e.getSimpleName().toString();
        fieldType = e.asType();
    }
    
    
    public FieldInfo(String fieldName, TypeMirror fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }


    public String getFieldName() {
        return fieldName;
    }
    
    public String getFieldTypeName() {
        return fieldType.toString();
    }
    
    public void setInheritanceLevel(int inheritanceLevel) {
        this.inheritanceLevel = inheritanceLevel;
        this.effectiveInheritanceLevel = inheritanceLevel;
    }
    
    /* (non-Javadoc)
     * @see org.boundbox.Inheritable#getInheritanceLevel()
     */
    @Override
    public int getInheritanceLevel() {
        return inheritanceLevel;
    }
    
    public void setEffectiveInheritanceLevel(int effectiveInheritanceLevel) {
        this.effectiveInheritanceLevel = effectiveInheritanceLevel;
    }
    
    public int getEffectiveInheritanceLevel() {
        return effectiveInheritanceLevel;
    }
    
    @Override
    public boolean isInherited() {
        return inheritanceLevel != 0;
    }

    public boolean isStaticField() {
        return staticField;
    }
    
    public void setStaticField(boolean staticField) {
        this.staticField = staticField;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + inheritanceLevel;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FieldInfo))
            return false;
        FieldInfo other = (FieldInfo) obj;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        if (inheritanceLevel != other.inheritanceLevel)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "FieldInfo [fieldName=" + fieldName + ", fieldType=" + fieldType + "]";
    }


}