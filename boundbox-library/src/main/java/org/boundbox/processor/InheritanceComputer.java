package org.boundbox.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.boundbox.model.FieldInfo;
import org.boundbox.model.MethodInfo;

public class InheritanceComputer {

    public void computeInheritanceAndHiding( List<FieldInfo> listFieldInfos) {
        //get min inheritance level of Field.
        Map<String, FieldInfo> mapFieldNameToMinFieldInfo = new HashMap<String, FieldInfo>();
        for( FieldInfo fieldInfo : listFieldInfos ) {
            if( ! mapFieldNameToMinFieldInfo.containsKey(fieldInfo.getFieldName())) {
                mapFieldNameToMinFieldInfo.put( fieldInfo.getFieldName(), fieldInfo);
            } else {
                FieldInfo minFieldInfo = mapFieldNameToMinFieldInfo.get(fieldInfo.getFieldName());
                if( minFieldInfo.getInheritanceLevel() > fieldInfo.getInheritanceLevel() ) {
                    mapFieldNameToMinFieldInfo.put( fieldInfo.getFieldName(), fieldInfo);
                }
            }
        }
        
        //and replace it to 0
        for( FieldInfo minFields : mapFieldNameToMinFieldInfo.values() ) {
            minFields.setEffectiveInheritanceLevel(0);
        }
    }

    public void computeInheritanceAndOverriding(List<MethodInfo> listMethodInfos) {
        //get min inheritance level of Field.
        Map<String, MethodInfo> mapMethodSignatureNameToMinMethodInfo = new HashMap<String, MethodInfo>();
        for( MethodInfo MethodInfo : listMethodInfos ) {
            if( ! mapMethodSignatureNameToMinMethodInfo.containsKey(MethodInfo.getMethodName())) {
                mapMethodSignatureNameToMinMethodInfo.put( MethodInfo.getMethodName(), MethodInfo);
            } else {
                MethodInfo minMethodInfo = mapMethodSignatureNameToMinMethodInfo.get(MethodInfo.getMethodName());
                if( minMethodInfo.getInheritanceLevel() > MethodInfo.getInheritanceLevel() ) {
                    mapMethodSignatureNameToMinMethodInfo.put( MethodInfo.getMethodName(), MethodInfo);
                }
            }
        }
        
        //and replace it to 0
        for( MethodInfo minFields : mapMethodSignatureNameToMinMethodInfo.values() ) {
            minFields.setEffectiveInheritanceLevel(0);
        }
    }
    
    private String computeMethodInfoSignature( MethodInfo methodInfo ) {
        //TODO add param types
        return methodInfo.getMethodName();
    }
    
    private boolean isOverrideOf( MethodInfo methodInfoOverride, MethodInfo methodInfoOverriden ) {
        //TODO 
        return false;
    }

    
    //TODO same for methods
}
