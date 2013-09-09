package org.boundbox.model;

import java.util.List;

public class ClassInfo {
    private String className;
    private List<FieldInfo> listFieldInfos;
    private List<MethodInfo> listMethodInfos;
    private List<MethodInfo> listConstructorInfos;
    private List<String> listSuperClassNames;
    private String targetPackageName;
    private String targetClassName;
    private String boundBoxClassName;
    
    
    public ClassInfo(String className) {
        this.className = className;
        if( className.contains(".")) {
        targetPackageName = className.substring(0, className.lastIndexOf('.'));
        targetClassName = className.substring(className.lastIndexOf('.')+1);
        } else {
            targetPackageName = "";
            targetClassName = className;
        }
        boundBoxClassName = "BoundBoxOf"+targetClassName;
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
}