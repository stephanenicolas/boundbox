package org.boundbox.sample;

@SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class VisibilityAndPackageTestClass {
    private class Inner {
        private C foo = new C();
    }
    static class C {}
}
