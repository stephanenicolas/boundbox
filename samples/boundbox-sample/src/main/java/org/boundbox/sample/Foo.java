package org.boundbox.sample;

@SuppressWarnings("unused")
public class Foo extends MotherOfFoo {

    private String bar = "bar";

    private int foo() {
        return 42;
    }

    private int foo(int a) throws Exception {
        return 42;
    }

}
