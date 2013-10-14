import org.boundbox.BoundBox;

@BoundBox(boundClass=TestClassWithPackageInvisibleInnerClassAndFieldOfThatType.class)
@SuppressWarnings("unused")
public class TestClassWithPackageInvisibleInnerClassAndFieldOfThatType {
    static class B {}
    private B foo;
}

