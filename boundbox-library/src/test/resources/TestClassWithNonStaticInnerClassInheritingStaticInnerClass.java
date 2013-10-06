import org.boundbox.BoundBox;

//TDD for issue #18
@BoundBox(boundClass = TestClassWithNonStaticInnerClassInheritingStaticInnerClass.class)
public class TestClassWithNonStaticInnerClassInheritingStaticInnerClass {
    public class SubInnerClass extends B.Inner {
    }
}

class B {
    public static class Inner {
        public static int foo;
    }
}
