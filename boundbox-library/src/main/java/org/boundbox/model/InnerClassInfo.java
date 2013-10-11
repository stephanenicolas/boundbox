package org.boundbox.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


//CHECKSTYLE:OFF HideUtilityClassConstructorCheck
@SuppressWarnings("PMD.UnusedPrivateField")
@EqualsAndHashCode(callSuper=true,exclude={"effectiveInheritanceLevel"})
@ToString(callSuper=true)
public class InnerClassInfo extends ClassInfo implements Inheritable, Comparable<InnerClassInfo> {

    @Setter
    @Getter
    private ClassInfo superClassInfo;
    
    @Getter
    private int inheritanceLevel;
    
    @Getter
    @Setter
    private int effectiveInheritanceLevel;
    
    @Getter
    @Setter
    private boolean staticInnerClass;
    
    public InnerClassInfo(String className) {
        super(className);
    }
    
    public void setInheritanceLevel(int inheritanceLevel) {
        this.inheritanceLevel = inheritanceLevel;
        this.effectiveInheritanceLevel = inheritanceLevel;
    }
    
    @Override
    public int compareTo(InnerClassInfo other) {
        return new CompareToBuilder() 
        .append(isStaticInnerClass(), other.isStaticInnerClass()) 
        .append(className, other.className) 
        .toComparison();
    }

}
//CHECKSTYLE:ON 
