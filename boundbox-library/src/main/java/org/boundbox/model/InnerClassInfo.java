package org.boundbox.model;

import lombok.Getter;
import lombok.Setter;


//CHECKSTYLE:OFF HideUtilityClassConstructorCheck
@SuppressWarnings("PMD.UnusedPrivateField")
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
    private boolean staticField;
    
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
