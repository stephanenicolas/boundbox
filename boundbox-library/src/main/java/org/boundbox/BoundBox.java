package org.boundbox;

public @interface BoundBox {
    Class<?> boundClass();

    Class<?> maxSuperClass() default Object.class;
    
    /*
     * List of extra field to be boundboxed
     * Default behavior: all fields found while reading boundClass source are boundboxed
     */
    BoundBoxField[] extraFields() default {};
}
