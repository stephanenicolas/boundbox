import org.boundbox.BoundBox;

@BoundBox(boundClass=TestClassWithInvisibleInnerClassAndMethodWithParamOfThatType.class)
public class TestClassWithInvisibleInnerClassAndMethodWithParamOfThatType {
    private static class B {}
    public void foo(B b) {}
}

