package org.boundbox.model;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class MethodInfo implements Inheritable {
    protected String methodName;
    protected TypeMirror returnType;
    protected List<FieldInfo> parameterTypes;
    protected List<? extends TypeMirror> thrownTypes;
    private int inheritanceLevel;
    private int effectiveInheritanceLevel;
    private ExecutableElement element;
    private boolean overriden;

    public MethodInfo( ExecutableElement element ) {
        this.element = element;
        methodName = element.getSimpleName().toString();
        returnType = element.getReturnType();
        parameterTypes = new ArrayList<FieldInfo>();
        for (VariableElement variableElement : element.getParameters()) {
            parameterTypes.add( new FieldInfo(variableElement));
        }      
        thrownTypes = element.getThrownTypes();
    }

    //for testing
    protected MethodInfo() {
    }

    public String getMethodName() {
        return methodName;
    }

    public TypeMirror getReturnType() {
        return returnType;
    }

    public String getReturnTypeName() {
        return returnType.toString();
    }

    public List<FieldInfo> getParameterTypes() {
        return parameterTypes;
    }

    public List<? extends TypeMirror> getThrownTypes() {
        return thrownTypes;
    }

    public List<String> getThrownTypeNames() {
        List<String> thrownTypeNames = new ArrayList<String>();
        for (TypeMirror typeMirror : thrownTypes) {
            thrownTypeNames.add(typeMirror.toString());
        }
        return thrownTypeNames;
    }

    public boolean isConstructor() {
        return "<init>".equals( methodName );
    }

    public boolean hasReturnType() {
        return returnType != null && !"void".equalsIgnoreCase(returnType.toString());
    }

    public void setInheritanceLevel(int inheritanceLevel) {
        this.inheritanceLevel = inheritanceLevel;
        this.effectiveInheritanceLevel = inheritanceLevel;
    }

    public int getInheritanceLevel() {
        return inheritanceLevel;
    }
    
    public void setEffectiveInheritanceLevel(int effectiveInheritanceLevel) {
        this.effectiveInheritanceLevel = effectiveInheritanceLevel;
    }
    
    public int getEffectiveInheritanceLevel() {
        return effectiveInheritanceLevel;
    }

    public boolean isInherited() {
        return inheritanceLevel != 0;
    }
    
    public ExecutableElement getElement() {
        return element;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof MethodInfo))
            return false;
        MethodInfo other = (MethodInfo) obj;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        if (parameterTypes == null) {
            if (other.parameterTypes != null)
                return false;
        } else if (!parameterTypes.equals(other.parameterTypes))
            return false;
        if (inheritanceLevel != other.inheritanceLevel)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MethodInfo [methodName=" + methodName + ", returnType=" + returnType + ", parameterTypes=" + parameterTypes
                + ", thrownTypes=" + thrownTypes + "]";
    }

    public void setOverriden(boolean overriden) {
        this.overriden = overriden;
    }
    
    public boolean isOverriden() {
        return overriden;
    }


}