package org.boundbox.processor;

import java.util.List;

import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;

public class VisibilityComputer {

    public void processVisibleTypes(ClassInfo classInfo, List<String> listOfInvisibleTypes) {
        // TODO constructor ?
        for (FieldInfo fieldInfo : classInfo.getListFieldInfos()) {
            processField(fieldInfo, listOfInvisibleTypes);
        }
        for (MethodInfo methodInfo : classInfo.getListMethodInfos()) {
            processMethod(methodInfo, listOfInvisibleTypes);
        }
        for (InnerClassInfo innerClassInfo : classInfo.getListInnerClassInfo()) {
            processInnerClass(innerClassInfo, listOfInvisibleTypes);
        }
    }

    public void processInnerClass(InnerClassInfo innerClassInfo, List<String> listOfInvisibleTypes) {
        for (MethodInfo constructorInfo : innerClassInfo.getListConstructorInfos()) {
            processConstructor(constructorInfo, listOfInvisibleTypes);
        }
        for (FieldInfo fieldInfo : innerClassInfo.getListFieldInfos()) {
            processField(fieldInfo, listOfInvisibleTypes);
        }
        for (MethodInfo methodInfo : innerClassInfo.getListMethodInfos()) {
            processMethod(methodInfo, listOfInvisibleTypes);
        }
        for (InnerClassInfo innerInnerClassInfo : innerClassInfo.getListInnerClassInfo()) {
            processInnerClass(innerInnerClassInfo, listOfInvisibleTypes);
        }
    }

    private void processConstructor(MethodInfo methodInfo, List<String> listOfInvisibleTypes) {
        //TODO if the inner class itself is not visible
        for (String thrownTypeName : methodInfo.getThrownTypeNames()) {
            if (listOfInvisibleTypes.contains(thrownTypeName)) {
                // TODO and this can be complex...
            }
        }
        for (FieldInfo paramInfo : methodInfo.getParameterTypes()) {
            if (listOfInvisibleTypes.contains(paramInfo.getFieldTypeName())) {
                paramInfo.setFieldTypeName("java.lang.Object");
            }
        }
    }
    
    private void processField(FieldInfo fieldInfo, List<String> listOfInvisibleTypes) {
        if (listOfInvisibleTypes.contains(fieldInfo.getFieldTypeName())) {
            fieldInfo.setFieldTypeName("java.lang.Object");
        }
    }

    private void processMethod(MethodInfo methodInfo, List<String> listOfInvisibleTypes) {
        if (listOfInvisibleTypes.contains(methodInfo.getReturnTypeName())) {
            methodInfo.setReturnTypeName("java.lang.Object");
        }
        for (String thrownTypeName : methodInfo.getThrownTypeNames()) {
            if (listOfInvisibleTypes.contains(thrownTypeName)) {
                // TODO and this can be complex...
            }
        }
        for (FieldInfo paramInfo : methodInfo.getParameterTypes()) {
            if (listOfInvisibleTypes.contains(paramInfo.getFieldTypeName())) {
                paramInfo.setFieldTypeName("java.lang.Object");
            }
        }
    }

}
