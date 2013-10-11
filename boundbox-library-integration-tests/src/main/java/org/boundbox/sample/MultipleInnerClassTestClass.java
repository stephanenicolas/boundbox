package org.boundbox.sample;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class MultipleInnerClassTestClass {
    public class InnerClass {
        public int a = 0;
    };
    public static class StaticInnerClass2 {
        public int a = 3;
    };
    public static class StaticInnerClass {
        public int a = 2;
    };
    public class InnerClass2 {
        public int a = 1;
    };
}
