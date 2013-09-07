package org.boundbox;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class MethodInfo {
    private String methodName;
    private TypeMirror returnType;
    private List<FieldInfo> parameterTypes;
    private List<? extends TypeMirror> thrownTypes;

    public MethodInfo( ExecutableElement element ) {
        methodName = element.getSimpleName().toString();
        returnType = element.getReturnType();
        parameterTypes = new ArrayList<FieldInfo>();
        for (VariableElement variableElement : element.getParameters()) {
            parameterTypes.add( new FieldInfo(variableElement));
        }      
        thrownTypes = element.getThrownTypes();
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public TypeMirror getReturnType() {
        return returnType;
    }
    
    public List<FieldInfo> getParameterTypes() {
        return parameterTypes;
    }
    
    public List<? extends TypeMirror> getThrownTypes() {
        return thrownTypes;
    }
    
    public boolean isConstructor() {
        return "<init>".equals( methodName );
    }
}