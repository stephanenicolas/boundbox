import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithInheritedAndHidingField.class)
public class TestClassWithInheritedAndHidingField extends TestClassWithSingleField {
    private String foo = "test";
}
