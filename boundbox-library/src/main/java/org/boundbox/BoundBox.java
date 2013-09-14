package org.boundbox;

public @interface BoundBox {
    Class<?> boundClass();

    Class<?> maxSuperClass() default Object.class;
}
