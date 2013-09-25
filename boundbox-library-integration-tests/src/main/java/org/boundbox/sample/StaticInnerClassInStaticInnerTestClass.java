package org.boundbox.sample;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class StaticInnerClassInStaticInnerTestClass {
    public static class InBetweenClass {
        public static class InnerClass {
        };
    };
}
