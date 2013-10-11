import org.boundbox.BoundBox;

@BoundBox(boundClass=TestClassWithInvisibleInnerClassAndMethodWithExceptionOfThatType.class)
@SuppressWarnings("serial")
public class TestClassWithInvisibleInnerClassAndMethodWithExceptionOfThatType {
    private static class B extends Exception{}
    public void foo() throws B {}
}

