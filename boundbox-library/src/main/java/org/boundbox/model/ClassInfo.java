package org.boundbox.model;

import java.util.List;

public class ClassInfo {
    private String className;
    private List<FieldInfo> listFieldInfos;
    private List<MethodInfo> listMethodInfos;
    private List<String> listSuperClassNames;
    
    
    private ClassInfo(String className) {
        this.className = className;
    }
    
    public String getClassName() {
        return className;
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
    public List<String> getListSuperClassNames() {
        return listSuperClassNames;
    }
    public void setListSuperClassNames(List<String> listSuperClassNames) {
        this.listSuperClassNames = listSuperClassNames;
    }
    public void setClassName(String className) {
        this.className = className;
    }



}