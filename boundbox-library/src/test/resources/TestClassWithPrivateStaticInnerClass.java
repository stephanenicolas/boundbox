import org.boundbox.BoundBox;

// part of TDD for https://github.com/stephanenicolas/boundbox/issues/2
//proposed by Flavien Laurent
@BoundBox(boundClass = TestClassWithPrivateStaticInnerClass.class)
public class TestClassWithPrivateStaticInnerClass {
    @SuppressWarnings("unused")
    private static class InnerClass {
    }
}
