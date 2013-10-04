import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithSingleFinalField.class)
public class TestClassWithSingleFinalField {
    private final String foo = "test";
}
