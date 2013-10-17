import org.boundbox.BoundBox;

//Thanks to Philippe Prados for this nasty example
@BoundBox(boundClass = TestClassIsStaticInnerClassThatExtendsNonStaticInnerClass.StaticInner.class)
@SuppressWarnings("unused")
public class TestClassIsStaticInnerClassThatExtendsNonStaticInnerClass {
    public class InnerClass {
        private int foo = 2;
    }

    public static class StaticInner extends InnerClass {
        public StaticInner(TestClassIsStaticInnerClassThatExtendsNonStaticInnerClass obj) {
            obj.super();
        }
    }
}
