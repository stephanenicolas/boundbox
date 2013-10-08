import org.boundbox.BoundBox;

//Thanks to Philippe Prados for this nasty example
@BoundBox(boundClass = TestClassIsNonStaticInnerClassThatExtendsStaticInnerClass.StaticInner.class)
@SuppressWarnings("unused")
public class TestClassIsNonStaticInnerClassThatExtendsStaticInnerClass {
    public class InnerClass {
        private int foo = 2;
    }

    public static class StaticInner extends InnerClass {
        public StaticInner(TestClassIsNonStaticInnerClassThatExtendsStaticInnerClass obj) {
            obj.super();
        }
    }
}
