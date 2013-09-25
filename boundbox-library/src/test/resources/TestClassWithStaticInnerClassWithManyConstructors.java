import org.boundbox.BoundBox;

// part of TDD for https://github.com/stephanenicolas/boundbox/issues/2
//proposed by Flavien Laurent
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
