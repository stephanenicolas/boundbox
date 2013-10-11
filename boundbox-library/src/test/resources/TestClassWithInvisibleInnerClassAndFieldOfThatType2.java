import org.boundbox.BoundBox;

@BoundBox(boundClass=TestClassWithInvisibleInnerClassAndFieldOfThatType2.class)
@SuppressWarnings("unused")
public class TestClassWithInvisibleInnerClassAndFieldOfThatType2 {
    private static class B {
        public static class C {
        }
    }
    private B.C foo;
}

