import org.boundbox.BoundBox;

@BoundBox(boundClass = TestClassWithStaticInnerClassWithConstructor.class)
public class TestClassWithStaticInnerClassWithConstructor {
    public static class InnerClass {
        public InnerClass() {
        }
    }
}
