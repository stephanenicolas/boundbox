package org.boundbox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Describes an extra field to be added to a BoundBoxed.
 * Extra field can be used when they can't be extracted from source.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface BoundBoxField {
	
	/* Name of the field in the target class. */
    public String fieldName();
    
    /* Type of the field. */
    public Class<?> fieldClass();
}