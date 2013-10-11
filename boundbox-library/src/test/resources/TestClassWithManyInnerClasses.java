import org.boundbox.BoundBox;

@BoundBox(boundClass=TestClassWithManyInnerClasses.class)
public class TestClassWithManyInnerClasses {
    public class InnerClass {
        public int a = 0;
    };
    public static class StaticInnerClass2 {
        public int b = 3;
    };
    public static class StaticInnerClass {
        public int c = 2;
    };
    public class InnerClass2 {
        public int d = 1;
    };
}