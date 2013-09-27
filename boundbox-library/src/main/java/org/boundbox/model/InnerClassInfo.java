package org.boundbox.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


//CHECKSTYLE:OFF HideUtilityClassConstructorCheck
@SuppressWarnings("PMD.UnusedPrivateField")
@EqualsAndHashCode(callSuper=true)
@ToString
public class InnerClassInfo extends ClassInfo implements Inheritable {

    @Setter
    @Getter
    private ClassInfo superClassInfo;
    
    @Setter
    @Getter
    private int innerClassIndex;

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
    public boolean isInherited() {
        return inheritanceLevel != 0;
    }

}
//CHECKSTYLE:ON 
