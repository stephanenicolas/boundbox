package org.boundbox.model;

import lombok.Getter;
import lombok.Setter;


//CHECKSTYLE:OFF HideUtilityClassConstructorCheck
@SuppressWarnings("PMD.UnusedPrivateField")
public class InnerClassInfo extends ClassInfo {

    @Setter
    @Getter
    private ClassInfo superClassInfo;
    
    @Setter
    @Getter
    private int innerClassIndex;

    public InnerClassInfo(String className) {
        super(className);
    }

}
//CHECKSTYLE:ON 
