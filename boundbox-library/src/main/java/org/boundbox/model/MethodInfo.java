package org.boundbox.model;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
// for testing
@EqualsAndHashCode(exclude = { "returnType", "thrownTypes", "effectiveInheritanceLevel", "element" })
@ToString
public class MethodInfo implements Inheritable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    protected @Getter
    String methodName;
    protected @Getter
    @Setter
    TypeMirror returnType;
    protected @Getter
    List<FieldInfo> parameterTypes;
    protected @Getter
    List<? extends TypeMirror> thrownTypes;
    private @Getter
    int inheritanceLevel;
    private @Getter
    @Setter
    int effectiveInheritanceLevel;
    private @Getter
    ExecutableElement element;
    private @Getter
    @Setter
    boolean overriden;
    private @Getter
    @Setter
    boolean staticMethod;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public MethodInfo(ExecutableElement element) {
        this.element = element;
        methodName = element.getSimpleName().toString();
        returnType = element.getReturnType();
        parameterTypes = new ArrayList<FieldInfo>();
        for (VariableElement variableElement : element.getParameters()) {
            parameterTypes.add(new FieldInfo(variableElement));
        }
        thrownTypes = element.getThrownTypes();
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    public String getReturnTypeName() {
        return returnType.toString();
    }

    public List<String> getThrownTypeNames() {
        List<String> thrownTypeNames = new ArrayList<String>();
        for (TypeMirror typeMirror : thrownTypes) {
            thrownTypeNames.add(typeMirror.toString());
        }
        return thrownTypeNames;
    }

    public boolean isConstructor() {
        return "<init>".equals(methodName);
    }

    public boolean isInstanceInitializer() {
        return "".equals(methodName);
    }

    public boolean isStaticInitializer() {
        return "<clinit>".equals(methodName);
    }

    public boolean hasReturnType() {
        return returnType != null && !"void".equalsIgnoreCase(returnType.toString());
    }

    public void setInheritanceLevel(int inheritanceLevel) {
        this.inheritanceLevel = inheritanceLevel;
        this.effectiveInheritanceLevel = inheritanceLevel;
    }

    public boolean isInherited() {
        return inheritanceLevel != 0;
    }

}
