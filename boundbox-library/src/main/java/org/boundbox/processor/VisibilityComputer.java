package org.boundbox.processor;

import java.util.List;

import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.MethodInfo;

public class VisibilityComputer {
    
    public void processVisibleTypes(ClassInfo classInfo, List<String> listOfInvisibleTypes ) {
        for(MethodInfo methodInfo : classInfo.getListMethodInfos() ) {
            if( listOfInvisibleTypes.contains(methodInfo.getReturnTypeName()) ) {
                methodInfo.setReturnTypeName("java.lang.Object");
            }
            for( String thrownTypeName : methodInfo.getThrownTypeNames() ) {
                if( listOfInvisibleTypes.contains(thrownTypeName) ) {
                    //TODO and this can be complex...
                }
            }
            for( FieldInfo paramInfo : methodInfo.getParameterTypes() ) {
                if( listOfInvisibleTypes.contains(paramInfo.getFieldTypeName()) ) {
                    paramInfo.setFieldTypeName("java.lang.Object");
                }
            }

        }
    }

}
