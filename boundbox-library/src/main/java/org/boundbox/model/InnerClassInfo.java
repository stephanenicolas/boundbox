package org.boundbox.model;

import javax.lang.model.element.TypeElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//CHECKSTYLE:OFF HideUtilityClassConstructorCheck
@SuppressWarnings("PMD.UnusedPrivateField")
@EqualsAndHashCode(callSuper=true,exclude={"effectiveInheritanceLevel","element"})
@ToString(callSuper=true)
public class InnerClassInfo extends ClassInfo implements Inheritable {

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
    
    @Getter 
    private TypeElement element;
    
    public InnerClassInfo(TypeElement element) {
        super(element.getSimpleName().toString());
        this.element = element;
    }
    
    public InnerClassInfo(String className) {
        super(className);
    }
    
    public void setInheritanceLevel(int inheritanceLevel) {
        this.inheritanceLevel = inheritanceLevel;
        this.effectiveInheritanceLevel = inheritanceLevel;
    }
    
}
//CHECKSTYLE:ON 
