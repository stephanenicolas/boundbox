import org.boundbox.BoundBox;

@BoundBox(boundClass = TestClassWithStaticInnerClass.class)
public class TestClassWithStaticInnerClass {
    public static class InnerClass {
        @SuppressWarnings("unused")
        public static int foo = 2;
    }
}
