import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithPrefix.class, prefixes={"BB"})
public class TestClassWithPrefix {
    private String foo = "test";
}
