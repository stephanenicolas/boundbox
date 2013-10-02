import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithPrefixes.class, prefixes={"BB","bb"})
public class TestClassWithPrefixes {
    private String foo = "test";
}
