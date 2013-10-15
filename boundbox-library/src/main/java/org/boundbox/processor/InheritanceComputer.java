package org.boundbox.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import lombok.extern.java.Log;

import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;

@Log
public class InheritanceComputer {
    
    public void computeInheritanceInInnerClasses(ClassInfo classInfo, Elements elements) {
        for( InnerClassInfo innerClassInfo : classInfo.getListInnerClassInfo() ) {
            computeInheritanceAndHidingFields(innerClassInfo.getListFieldInfos());
            computeInheritanceAndOverridingMethods(innerClassInfo.getListMethodInfos(), innerClassInfo.getElement(), elements );
            computeInheritanceAndHidingInnerClasses(innerClassInfo.getListInnerClassInfo());
            computeInheritanceInInnerClasses( innerClassInfo, elements);
        }
    }
    
    public void computeInheritanceAndHidingFields(List<FieldInfo> listFieldInfos) {
        // get min inheritance level of Field.
        Map<String, FieldInfo> mapFieldNameToMinFieldInfo = new HashMap<String, FieldInfo>();
        for (FieldInfo fieldInfo : listFieldInfos) {
            if (!mapFieldNameToMinFieldInfo.containsKey(fieldInfo.getFieldName())) {
                mapFieldNameToMinFieldInfo.put(fieldInfo.getFieldName(), fieldInfo);
            } else {
                FieldInfo minFieldInfo = mapFieldNameToMinFieldInfo.get(fieldInfo.getFieldName());
                if (minFieldInfo.getInheritanceLevel() > fieldInfo.getInheritanceLevel()) {
                    mapFieldNameToMinFieldInfo.put(fieldInfo.getFieldName(), fieldInfo);
                }
            }
        }

        // and replace it to 0
        for (FieldInfo minFields : mapFieldNameToMinFieldInfo.values()) {
            minFields.setEffectiveInheritanceLevel(0);
        }
    }
    
    public void computeInheritanceAndHidingInnerClasses(List<InnerClassInfo> listInnerClassInfos) {
        // get min inheritance level of Field.
        Map<String, InnerClassInfo> mapFieldNameToMinInnerClassInfo = new HashMap<String, InnerClassInfo>();
        for (InnerClassInfo innerClassInfo : listInnerClassInfos) {
            if (!mapFieldNameToMinInnerClassInfo.containsKey(innerClassInfo.getClassName())) {
                mapFieldNameToMinInnerClassInfo.put(innerClassInfo.getClassName(), innerClassInfo);
            } else {
                InnerClassInfo minFieldInfo = mapFieldNameToMinInnerClassInfo.get(innerClassInfo.getClassName());
                if (minFieldInfo.getInheritanceLevel() > innerClassInfo.getInheritanceLevel()) {
                    mapFieldNameToMinInnerClassInfo.put(innerClassInfo.getClassName(), innerClassInfo);
                }
            }
        }
        
        // and replace it to 0
        for (InnerClassInfo minClasses : mapFieldNameToMinInnerClassInfo.values()) {
            minClasses.setEffectiveInheritanceLevel(0);
        }
    }

    public void computeInheritanceAndOverridingMethods(List<MethodInfo> listMethodInfos, TypeElement typeElement, Elements elements) {
        // put all methods with same name in a list
        Map<String, List<MethodInfo>> mapMethodSignatureNameToListMethodInfo = new HashMap<String, List<MethodInfo>>();
        for (MethodInfo methodInfo : listMethodInfos) {
            if (!mapMethodSignatureNameToListMethodInfo.containsKey(methodInfo.getMethodName())) {
                mapMethodSignatureNameToListMethodInfo.put(methodInfo.getMethodName(),
                        new ArrayList<MethodInfo>(Arrays.asList(methodInfo)));
            } else {
                List<MethodInfo> methodInfoList = mapMethodSignatureNameToListMethodInfo.get(methodInfo.getMethodName());
                methodInfoList.add(methodInfo);
            }
        }

        // identify overrides
        for (Map.Entry<String, List<MethodInfo>> entry : mapMethodSignatureNameToListMethodInfo.entrySet()) {
            List<MethodInfo> listMethodInfosForName = entry.getValue();
            for (int i = 0; i < listMethodInfosForName.size(); i++) {
                for (int j = 0; j < listMethodInfos.size() && j != i; j++) {
                    MethodInfo left = listMethodInfos.get(i);
                    MethodInfo right = listMethodInfos.get(j);
                    if (left.getInheritanceLevel() != right.getInheritanceLevel()) {
                        if (elements.overrides(left.getElement(), right.getElement(), typeElement)) {
                            right.setOverriden(true);
                            log.info(left.getMethodName() + " overrides " + right.getMethodName());
                        } else if (elements.overrides(right.getElement(), left.getElement(), typeElement)) {
                            left.setOverriden(true);
                            log.info(right.getMethodName() + " overrides " + left.getMethodName());
                        }
                    }
                }

            }
        }

        // get min inheritance method
        Map<String, MethodInfo> mapMethodNameToMinMethodInfo = new HashMap<String, MethodInfo>();
        for (Map.Entry<String, List<MethodInfo>> entry : mapMethodSignatureNameToListMethodInfo.entrySet()) {
            List<MethodInfo> listMethodInfosForName = entry.getValue();
            for (MethodInfo methodInfo : listMethodInfosForName) {
                String methodName = entry.getKey();
                if (!mapMethodNameToMinMethodInfo.containsKey(methodName)) {
                    mapMethodNameToMinMethodInfo.put(methodName, methodInfo);
                } else {
                    if (mapMethodNameToMinMethodInfo.get(methodName).getInheritanceLevel() > methodInfo.getInheritanceLevel()) {
                        mapMethodNameToMinMethodInfo.put(methodName, methodInfo);
                    }
                }

            }
        }

        // and replace it to 0
        for (Map.Entry<String, MethodInfo> entry : mapMethodNameToMinMethodInfo.entrySet()) {
            for (MethodInfo methodInfo : mapMethodSignatureNameToListMethodInfo.get(entry.getKey())) {
                if (methodInfo.getInheritanceLevel() == entry.getValue().getInheritanceLevel()) {
                    methodInfo.setEffectiveInheritanceLevel(0);
                }
            }
        }
    }

}
