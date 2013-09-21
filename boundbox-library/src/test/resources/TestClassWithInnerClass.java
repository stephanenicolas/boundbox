import org.boundbox.BoundBox;

// part of TDD for https://github.com/stephanenicolas/boundbox/issues/2
//proposed by Flavien Laurent
@BoundBox(boundClass = TestClassWithInnerClass.class)
public class TestClassWithInnerClass {
    public int a = 0;
    public void foo() {};
    @SuppressWarnings("unused")
    private class InnerClass {
        
    }
}
