import org.boundbox.BoundBox;

@BoundBox(boundClass = TestClassWithStaticInnerClass.class)
public class TestClassWithStaticInnerClass {
    public static class InnerClass {
        public static int foo = 2;
    }
}
