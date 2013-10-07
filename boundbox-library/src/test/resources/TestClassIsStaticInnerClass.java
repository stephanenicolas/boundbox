import org.boundbox.BoundBox;

@BoundBox(boundClass = TestClassIsStaticInnerClass.InnerClass.class)
public class TestClassIsStaticInnerClass {
    public static class InnerClass {
        public static int foo = 2;
    }
}
