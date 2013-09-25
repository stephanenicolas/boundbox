package org.boundbox.sample;

@SuppressWarnings("all")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class NonStaticInnerClassWithManyConstructorsTestClass {
    public class InnerClass {
        public InnerClass() {}
        public InnerClass( int a ) {}
        public InnerClass( Object a ) {}
    };
}
