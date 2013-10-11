package org.boundbox.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang3.StringUtils;

//CHECKSTYLE:OFF HideUtilityClassConstructorCheck
@SuppressWarnings("PMD.UnusedPrivateField")
@EqualsAndHashCode(of = { "className" })
@ToString
public class ClassInfo {
    private static final String PACKAGE_SEPARATOR = ".";

    public static final String DEFAULT_BOUND_BOX_OF_CLASS_PREFIX = "BoundBoxOf";

    @NonNull
    @Getter
    protected String className;
    @Setter
    @Getter
    private List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
    @Setter
    @Getter
    private List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
    @Setter
    @Getter
    private List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();
    @Setter
    @Getter
    private List<String> listSuperClassNames = new ArrayList<String>();
    @Getter
    private String boundClassPackageName;
    @Getter
    private String boundClassName;
    @Setter
    @Getter
    private Set<String> listImports = new HashSet<String>();
    @Setter
    @Getter
    private List<InnerClassInfo> listInnerClassInfo = new ArrayList<InnerClassInfo>();

    public ClassInfo(String className) {
        this.className = className;
        if (className.contains(PACKAGE_SEPARATOR)) {
            boundClassPackageName = StringUtils.substringBeforeLast(className, PACKAGE_SEPARATOR);
            boundClassName = StringUtils.substringAfterLast(className, PACKAGE_SEPARATOR);
        } else {
            boundClassPackageName = StringUtils.EMPTY;
            boundClassName = className;
        }
        listSuperClassNames.add(className);
    }

}
// CHECKSTYLE:ON
