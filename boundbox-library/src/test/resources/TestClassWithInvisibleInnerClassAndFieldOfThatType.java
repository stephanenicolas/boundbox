import org.boundbox.BoundBox;

@BoundBox(boundClass=TestClassWithInvisibleInnerClassAndFieldOfThatType.class)
@SuppressWarnings("unused")
public class TestClassWithInvisibleInnerClassAndFieldOfThatType {
    private static class B {}
    private B foo;
}

