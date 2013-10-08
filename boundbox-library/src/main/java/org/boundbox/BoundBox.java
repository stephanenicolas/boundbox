package org.boundbox;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
//no target, on purpose
/**
 * The main annotation of the BoundBox library. 
 * @author SNI
 */
public @interface BoundBox {
    /** The class to be bound. */
    Class<?> boundClass();

    /** The highest super class in the hierarchy that this BoundBox exposes. @see <a href="https://github.com/stephanenicolas/boundbox/wiki/Limiting-hierarchy-exposure-of-a-BoundBox">Limiting hierarchy exposure.</a>*/
    Class<?> maxSuperClass() default Object.class;
    
    /**
     * List of extra fields to add to the BoundBox.
     * @see <a href="https://github.com/stephanenicolas/boundbox/wiki/Adding-extra-fields-to-a-boundbox">Adding extra fields to a BoundBox.</a>
     */
    BoundBoxField[] extraFields() default {};
    
    /** Prefixes used to name BoundBox classes and methods respectively. */
    String[] prefixes() default {"BoundBoxOf","boundBox"};

    /** Package name of the BoundBox. */
    String boundBoxPackage() default "";
}
