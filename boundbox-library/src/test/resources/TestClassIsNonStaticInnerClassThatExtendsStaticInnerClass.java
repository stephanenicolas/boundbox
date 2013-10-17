import org.boundbox.BoundBox;

//TDD for issue #18
@BoundBox(boundClass = TestClassIsNonStaticInnerClassThatExtendsStaticInnerClass.InnerClass.class)
public class TestClassIsNonStaticInnerClassThatExtendsStaticInnerClass {
    public class InnerClass extends StaticInner {
    }

    public static class StaticInner {
        public static int foo = 2;
    }
}
