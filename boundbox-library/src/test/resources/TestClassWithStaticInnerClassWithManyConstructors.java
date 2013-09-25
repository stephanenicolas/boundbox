import org.boundbox.BoundBox;

@BoundBox(boundClass = TestClassWithStaticInnerClassWithManyConstructors.class)
public class TestClassWithStaticInnerClassWithManyConstructors {
    public static class InnerClass {
        public InnerClass() {
        }
        public InnerClass(int a) {
        }
        public InnerClass(Object a) {
        }
    }
}
