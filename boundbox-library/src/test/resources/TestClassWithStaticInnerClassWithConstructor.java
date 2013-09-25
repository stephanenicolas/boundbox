import org.boundbox.BoundBox;

// part of TDD for https://github.com/stephanenicolas/boundbox/issues/2
//proposed by Flavien Laurent
@BoundBox(boundClass = TestClassWithStaticInnerClassWithConstructor.class)
public class TestClassWithStaticInnerClassWithConstructor {
    public static class InnerClass {
        public InnerClass() {
        }
    }
}
