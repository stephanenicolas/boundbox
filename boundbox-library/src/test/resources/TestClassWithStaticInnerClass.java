import org.boundbox.BoundBox;

// part of TDD for https://github.com/stephanenicolas/boundbox/issues/2
//proposed by Flavien Laurent
@BoundBox(boundClass = TestClassWithStaticInnerClass.class)
public class TestClassWithStaticInnerClass {
    public static class InnerClass {
        
    }
}
