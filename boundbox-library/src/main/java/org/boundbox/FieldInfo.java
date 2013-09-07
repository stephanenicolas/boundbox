package org.boundbox;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class FieldInfo {
    private String fieldName;
    private TypeMirror type;
    
    public FieldInfo( VariableElement e ) {
        fieldName = e.getSimpleName().toString();
        type = e.asType();
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public TypeMirror getType() {
        return type;
    }
    
}