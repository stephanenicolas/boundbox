import org.boundbox.BoundBox;

//TDD for issue #18
@BoundBox(boundClass = TestClassIsStaticInnerClassThatExtendsNonStaticInnerClass.InnerClass.class)
public class TestClassIsStaticInnerClassThatExtendsNonStaticInnerClass {
    public class InnerClass extends StaticInner {
    }

    public static class StaticInner {
        public static int foo = 2;
    }
}
