package org.boundbox.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

public class ClassInfo {
    public static final String DEFAULT_BOUND_BOX_OF_CLASS_PREFIX = "BoundBoxOf";
    
    private @Getter String className;
    private @Getter @Setter List<FieldInfo> listFieldInfos;
    private @Getter @Setter List<MethodInfo> listMethodInfos;
    private @Getter @Setter List<MethodInfo> listConstructorInfos;
    private @Getter @Setter List<String> listSuperClassNames;
    private @Getter String targetPackageName;
    private @Getter String targetClassName;
    private @Getter String boundBoxClassName;
    private @Getter @Setter List<String> listImports;

    public ClassInfo(String className) {
        this.className = className;
        if( className.contains(".")) {
            targetPackageName = StringUtils.substringBeforeLast(className, ".");
            targetClassName = StringUtils.substringAfterLast(className, ".");
        } else {
            targetPackageName = StringUtils.EMPTY;
            targetClassName = className;
        }
        boundBoxClassName = DEFAULT_BOUND_BOX_OF_CLASS_PREFIX+targetClassName;
        listSuperClassNames = new ArrayList<String>();
        listSuperClassNames.add(className);
    }

}