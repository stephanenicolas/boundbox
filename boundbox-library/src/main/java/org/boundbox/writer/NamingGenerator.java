package org.boundbox.writer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;

/**
 * This entity can name stuff like BoundBox classes, methods, etc.
 * @author SNI
 */
@AllArgsConstructor
@NoArgsConstructor
public class NamingGenerator {
    
    @NonNull
    /** Prefix used to name BoundBox classes. */
    private String boundBoxClassNamePrefix = "BoundBoxOf";
    
    @NonNull
    /** Prefix used to name BoundBox methods. */
    private String boundBoxMethodPrefix = "boundBox";
    
    public String createBoundBoxName(@NonNull ClassInfo classInfo) {
        String className = extractSimpleName(classInfo.getClassName());
        return prefixClass(className);
    }
    
    public String createInnerClassAccessorName(@NonNull InnerClassInfo innerClassInfo) {
        return prefixMethod("_new_" + innerClassInfo.getClassName());
    }
    
    public String createMethodName(@NonNull MethodInfo methodInfo, @NonNull List<String> listSuperClassNames) {
        
        if (methodInfo.isConstructor()) {
            return prefixMethod("_new");
        } else if (methodInfo.isStaticInitializer()) {
            if( methodInfo.getInheritanceLevel() == 0 ) {
                return prefixMethod("_static_init");
            } else {
                String superClassName = extractSimpleName(listSuperClassNames.get(methodInfo.getEffectiveInheritanceLevel()));
                return prefixMethod("_super_"+superClassName+"_static_init");
            }
        } else if (methodInfo.isInstanceInitializer()) {
            return prefixMethod("_init");
        } else {
            String methodWrapperName;
            if (methodInfo.getEffectiveInheritanceLevel() == 0) {
                methodWrapperName = methodInfo.getMethodName();
            } else {
                String superClassName = extractSimpleName(listSuperClassNames.get(methodInfo.getEffectiveInheritanceLevel()));
                methodWrapperName = prefixMethod("_super_" + superClassName + "_" + methodInfo.getMethodName());
            }
            return methodWrapperName;
        }
    }
    
    public String createGetterName(@NonNull FieldInfo fieldInfo, @NonNull List<String> listSuperClassNames, @NonNull String fieldNameCamelCase) {
        String getterName;
        if (fieldInfo.getEffectiveInheritanceLevel() == 0) {
            getterName = prefixMethod("_get" + fieldNameCamelCase);
        } else {
            String superClassName = extractSimpleName(listSuperClassNames.get(fieldInfo.getEffectiveInheritanceLevel()));
            getterName = prefixMethod("_super_" + superClassName + "_get" + fieldNameCamelCase);
        }
        return getterName;
    }

    public String createSetterName(@NonNull FieldInfo fieldInfo, @NonNull List<String> listSuperClassNames, @NonNull String fieldNameCamelCase) {
        String getterName;
        if (fieldInfo.getEffectiveInheritanceLevel() == 0) {
            getterName = prefixMethod("_set" + fieldNameCamelCase);
        } else {
            String superClassName = extractSimpleName(listSuperClassNames.get(fieldInfo.getEffectiveInheritanceLevel()));
            getterName = prefixMethod("_super_" + superClassName + "_set" + fieldNameCamelCase);
        }
        return getterName;
    }
    
    public String computeCamelCaseNameStartUpperCase(@NonNull String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private String extractSimpleName(@NonNull String className) {
        return className.contains(".") ? StringUtils.substringAfterLast(className, ".") : className;
    }
    
    private String prefixClass( @NonNull String className ) {
       return boundBoxClassNamePrefix + className;
    }
    
    private String prefixMethod( @NonNull String methodName) {
        return boundBoxMethodPrefix + methodName;
    }
}
