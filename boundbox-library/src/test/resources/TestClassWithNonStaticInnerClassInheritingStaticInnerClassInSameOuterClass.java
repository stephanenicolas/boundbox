import org.boundbox.BoundBox;

//TDD for issue #18
@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithNonStaticInnerClassInheritingStaticInnerClassInSameOuterClass.class)
public class TestClassWithNonStaticInnerClassInheritingStaticInnerClassInSameOuterClass {
    
    public static class StaticInner {
        private static int foo = 2;
    }
    public class SubInnerClass extends StaticInner {
    }
}

