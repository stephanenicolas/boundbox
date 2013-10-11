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
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
// for testing
@EqualsAndHashCode(exclude = { "returnTypeName", "thrownTypeNames", "effectiveInheritanceLevel", "element" })
@ToString
@SuppressWarnings("PMD.UnusedPrivateField")
public class MethodInfo implements Inheritable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Getter
    protected String methodName;
    @Setter
    protected String returnTypeName;
    @Getter
    protected List<FieldInfo> parameterTypes;
    @Getter
    protected List<String> thrownTypeNames;
    @Getter
    private int inheritanceLevel;
    @Setter
    @Getter
    private int effectiveInheritanceLevel;
    @Getter
    private ExecutableElement element;
    @Setter
    @Getter
    private boolean overriden;
    @Setter
    @Getter
    private boolean staticMethod;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public MethodInfo(@NonNull ExecutableElement element) {
        this.element = element;
        methodName = element.getSimpleName().toString();
        returnTypeName = element.getReturnType().toString();
        parameterTypes = new ArrayList<FieldInfo>();
        for (VariableElement variableElement : element.getParameters()) {
            parameterTypes.add(new FieldInfo(variableElement));
        }
        thrownTypeNames = new ArrayList<String>();
        for (TypeMirror typeMirror : element.getThrownTypes()) {
            thrownTypeNames.add(typeMirror.toString());
        }
    }
    
    public MethodInfo(@NonNull String methodName,@NonNull String returnTypeName, List<FieldInfo> listParameters,
            List<String> listThrownTypeNames) {
        this.methodName = methodName;
        this.returnTypeName = returnTypeName;
        this.parameterTypes = listParameters;
        this.thrownTypeNames = listThrownTypeNames;
    }


    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    public String getReturnTypeName() {
        return returnTypeName;
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
        return returnTypeName != null && !"void".equalsIgnoreCase(returnTypeName);
    }

    public void setInheritanceLevel(int inheritanceLevel) {
        this.inheritanceLevel = inheritanceLevel;
        this.effectiveInheritanceLevel = inheritanceLevel;
    }

}
