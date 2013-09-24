package org.boundbox.sample;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class StaticPrivateInnerClassTestClass {
    @SuppressWarnings("unused")
    private static class InnerClass {
    };
}
