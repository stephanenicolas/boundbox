package org.boundbox.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ClassInfo {
    public static final String DEFAULT_BOUND_BOX_OF_CLASS_PREFIX = "BoundBoxOf";
    
    private String className;
    private List<FieldInfo> listFieldInfos;
    private List<MethodInfo> listMethodInfos;
    private List<MethodInfo> listConstructorInfos;
    private List<String> listSuperClassNames;
    private String targetPackageName;
    private String targetClassName;
    private String boundBoxClassName;
    private List<String> listImports;

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

    public String getClassName() {
        return className;
    }

    public String getTargetPackageName() {
        return targetPackageName;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public String getBoundBoxClassName() {
        return boundBoxClassName;
    }

    public List<FieldInfo> getListFieldInfos() {
        return listFieldInfos;
    }
    public void setListFieldInfos(List<FieldInfo> listFieldInfos) {
        this.listFieldInfos = listFieldInfos;
    }
    public List<MethodInfo> getListMethodInfos() {
        return listMethodInfos;
    }
    public void setListMethodInfos(List<MethodInfo> listMethodInfos) {
        this.listMethodInfos = listMethodInfos;
    }
    public void setListConstructorInfos(List<MethodInfo> listConstructorInfos) {
        this.listConstructorInfos = listConstructorInfos;
    }
    public List<MethodInfo> getListConstructorInfos() {
        return listConstructorInfos;
    }
    public List<String> getListSuperClassNames() {
        return listSuperClassNames;
    }
    public void setListSuperClassNames(List<String> listSuperClassNames) {
        this.listSuperClassNames = listSuperClassNames;
    }
    public List<String> getListImports() {
        return this.listImports;
    }
    public void setListImports(List<String> listImports) {
        this.listImports = listImports;
    }
    
}