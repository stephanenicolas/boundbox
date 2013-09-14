package org.boundbox.sample;

@SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class MotherOfFoo {

    private String motherBar = "bar";

    private int motherFoo() {
        return 42;
    }

    private int motherFoo(int a) throws Exception {
        return 42;
    }

}
