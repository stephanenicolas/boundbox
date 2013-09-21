import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithStaticField.class)
public class TestClassWithStaticField {
    private static String foo = "test";
    private static int a = 1;
}
