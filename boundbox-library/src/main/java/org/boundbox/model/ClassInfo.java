package org.boundbox.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

//CHECKSTYLE:OFF HideUtilityClassConstructorCheck
@SuppressWarnings("PMD.UnusedPrivateField")
public class ClassInfo {
    public static final String DEFAULT_BOUND_BOX_OF_CLASS_PREFIX = "BoundBoxOf";

    @NonNull
    @Getter
    private String className;
    @Setter
    @Getter
    private List<FieldInfo> listFieldInfos;
    @Setter
    @Getter
    private List<MethodInfo> listMethodInfos;
    @Setter
    @Getter
    private List<MethodInfo> listConstructorInfos;
    @Setter
    @Getter
    private List<String> listSuperClassNames;
    @Getter
    private String targetPackageName;
    @Getter
    private String targetClassName;
    @Getter
    private String boundBoxClassName;
    @Setter
    @Getter
    private List<String> listImports;

    public ClassInfo(String className) {
        this.className = className;
        if (className.contains(".")) {
            targetPackageName = StringUtils.substringBeforeLast(className, ".");
            targetClassName = StringUtils.substringAfterLast(className, ".");
        } else {
            targetPackageName = StringUtils.EMPTY;
            targetClassName = className;
        }
        boundBoxClassName = DEFAULT_BOUND_BOX_OF_CLASS_PREFIX + targetClassName;
        listSuperClassNames = new ArrayList<String>();
        listSuperClassNames.add(className);
    }

}
//CHECKSTYLE:ON 
