package org.boundbox.sample;

@SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class StaticInnerClassWithManyConstructorsTestClass {
    public static class InnerClass {
        public InnerClass() {}
        public InnerClass( int a ) {}
        public InnerClass( Object a ) {}
    };
}
