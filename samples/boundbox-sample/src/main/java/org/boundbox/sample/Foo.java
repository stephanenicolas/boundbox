package org.boundbox.sample;

@SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class Foo extends MotherOfFoo {

    private String bar = "bar";

    private int foo() {
        return 42;
    }

    private int foo(int a) throws Exception {
        return 42;
    }

}
