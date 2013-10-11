import org.boundbox.BoundBox;

@BoundBox(boundClass=TestClassWithInvisibleInnerClassAndMethodReturningIt.class)
public class TestClassWithInvisibleInnerClassAndMethodReturningIt {
    private static class B {}
    public B foo() {return null;}
}

