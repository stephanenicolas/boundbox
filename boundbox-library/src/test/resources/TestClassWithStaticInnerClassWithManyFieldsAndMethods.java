import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithStaticInnerClassWithManyFieldsAndMethods.class)
public class TestClassWithStaticInnerClassWithManyFieldsAndMethods {
    public static class InnerClass {
        public int a;
        public final Object b = null;
        private void foo() {}
        private void bar( int a) {}
    }
}
