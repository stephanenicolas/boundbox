package org.boundbox.model;

public interface Inheritable {

    int getInheritanceLevel();

    int getEffectiveInheritanceLevel();

    boolean isInherited();

}
