package org.boundbox.sample;

@SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class NonStaticInnerClassWithManyFieldsAndMethodsTestClass {
    public class InnerClass {
        public int a;
        public Object b;
        private void foo() {}
        private void bar( int a) {}
    };
}
