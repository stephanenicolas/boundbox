package org.boundbox.sample;

@SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class VisibilityTestClass {
    private class Inner {
        public class InnerInner{
            private C foo = new C();
        }
    }
    private static class C {}
}
