package org.boundbox;


//TODO which target? which retention?
/*
 * Use to describe an extra field to be boundboxed.
 * Extra field can be used when they can't be extracted from source.
 */
public @interface BoundBoxField {
	
	/*
	 * Name of the field in the target class
	 */
    public String fieldName();
    
    /*
     * Type of the field
     */
    public Class<?> fieldClass();
}