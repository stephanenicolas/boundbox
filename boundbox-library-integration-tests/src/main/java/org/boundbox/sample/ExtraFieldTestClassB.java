package org.boundbox.sample;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
@SuppressWarnings("unused")
public class ExtraFieldTestClassB extends FieldTestClassA {
    private String field1 = "b";

}
