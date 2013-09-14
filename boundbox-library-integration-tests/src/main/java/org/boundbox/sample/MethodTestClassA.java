package org.boundbox.sample;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
@SuppressWarnings("unused")
public class MethodTestClassA {
    private String foo() {
        return "a";
    }

    private String bar() {
        return "b";
    }
}
