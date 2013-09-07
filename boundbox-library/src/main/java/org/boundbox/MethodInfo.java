package org.boundbox;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class MethodInfo {
    private String methodName;
    private TypeMirror returnType;
    private List<TypeMirror> parameterTypes;
    private List<? extends TypeMirror> thrownTypes;

    public MethodInfo( ExecutableElement element ) {
        methodName = element.getSimpleName().toString();
        returnType = element.getReturnType();
        parameterTypes = new ArrayList<TypeMirror>();
        for (VariableElement variableElement : element.getParameters()) {
            parameterTypes.add(variableElement.asType());
        }      
        thrownTypes = element.getThrownTypes();
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public TypeMirror getReturnType() {
        return returnType;
    }
    
    public List<TypeMirror> getParameterTypes() {
        return parameterTypes;
    }
    
    public List<? extends TypeMirror> getThrownTypes() {
        return thrownTypes;
    }
    
    public boolean isConstructor() {
        return "<init>".equals( methodName );
    }
}