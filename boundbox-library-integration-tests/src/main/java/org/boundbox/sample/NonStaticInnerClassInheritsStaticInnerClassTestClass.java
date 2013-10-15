package org.boundbox.sample;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = { "URF_UNREAD_FIELD" }, justification = "Only used for tests")
@SuppressWarnings("unused")
//TDD for issue #18
public class NonStaticInnerClassInheritsStaticInnerClassTestClass {
    //this test is order dependent, that's a fail. 
    //there is absolutely nothing that we can do to prevent this.
    private static class StaticInnerClass {
        private static int foo = 2;
    }

    public class InnerClass extends StaticInnerClass {
    };
}
