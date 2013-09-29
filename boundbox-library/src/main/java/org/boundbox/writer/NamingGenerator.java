package org.boundbox.writer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;

/**
 * This entity can name stuff like BoundBox classes, methods, etc.
 * @author SNI
 */
public class NamingGenerator {
    public String createBoundBoxName(ClassInfo classInfo) {
        String className = classInfo.getClassName().contains(".") ? StringUtils.substringAfterLast(classInfo.getClassName(), ".") : classInfo.getClassName();
        return "BoundBoxOf" + className;
    }
    
    public String createInnerClassAccessorName(InnerClassInfo innerClassInfo) {
        return "boundBox_new_" + innerClassInfo.getClassName();
    }
    
    public String createMethodName(MethodInfo methodInfo, List<String> listSuperClassNames) {
        
        if (methodInfo.isConstructor()) {
            return "boundBox_new";
        } else if (methodInfo.isStaticInitializer()) {
            if( methodInfo.getInheritanceLevel() >0 ) {
                return "boundBox_static_init_"+extractSimpleName(listSuperClassNames.get(methodInfo.getInheritanceLevel()));
            } else {
                return "boundBox_static_init";
            }
        } else if (methodInfo.isInstanceInitializer()) {
            return "boundBox_init";
        } else {
            String getterName;
            if (methodInfo.getEffectiveInheritanceLevel() == 0) {
                getterName = methodInfo.getMethodName();
            } else {
                String superClassName = extractSimpleName(listSuperClassNames.get(methodInfo.getEffectiveInheritanceLevel()));
                getterName = "boundBox_super_" + superClassName + "_" + methodInfo.getMethodName();
            }
            return getterName;
        }
    }
    
    public String createGetterName(FieldInfo fieldInfo, List<String> listSuperClassNames, String fieldNameCamelCase) {
        String getterName;
        if (fieldInfo.getEffectiveInheritanceLevel() == 0) {
            getterName = "boundBox_get" + fieldNameCamelCase;
        } else {
            String superClassName = extractSimpleName(listSuperClassNames.get(fieldInfo.getEffectiveInheritanceLevel()));
            getterName = "boundBox_super_" + superClassName + "_get" + fieldNameCamelCase;
        }
        return getterName;
    }

    public String createSetterName(FieldInfo fieldInfo, List<String> listSuperClassNames, String fieldNameCamelCase) {
        String getterName;
        if (fieldInfo.getEffectiveInheritanceLevel() == 0) {
            getterName = "boundBox_set" + fieldNameCamelCase;
        } else {
            String superClassName = extractSimpleName(listSuperClassNames.get(fieldInfo.getEffectiveInheritanceLevel()));
            getterName = "boundBox_super_" + superClassName + "_set" + fieldNameCamelCase;
        }
        return getterName;
    }
    
    public String computeCamelCaseNameStartUpperCase(String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private String extractSimpleName(String className) {
        return className.contains(".") ? StringUtils.substringAfterLast(className, ".") : className;
    }
    

}
