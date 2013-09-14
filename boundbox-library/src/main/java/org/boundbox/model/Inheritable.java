package org.boundbox.model;

public interface Inheritable {

    public abstract int getInheritanceLevel();

    public abstract int getEffectiveInheritanceLevel();

    public abstract boolean isInherited();

}
