import org.boundbox.BoundBox;
import org.boundbox.BoundBoxField;

@SuppressWarnings("unused")
@BoundBox(
			boundClass = TestClassWithSingleExtraFieldAlreadyExists.class,
			extraFields = {
				@BoundBoxField(
						fieldName = "foo",
						fieldClass = String.class
				)
			}
)
public class TestClassWithSingleExtraFieldAlreadyExists {
    private String foo = "test";
}
