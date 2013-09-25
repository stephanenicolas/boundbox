import java.util.ArrayList;

import org.boundbox.BoundBox;
import org.boundbox.BoundBoxField;

@BoundBox(
		boundClass = TestClassWithManyExtraFields.class,
		extraFields = {
			@BoundBoxField(
					fieldName = "foo",
					fieldClass = String.class
			),
			@BoundBoxField(
					fieldName = "a",
					fieldClass = int.class
			),
			@BoundBoxField(
					fieldName = "array1",
					fieldClass = double[].class
			),
			@BoundBoxField(
					fieldName = "array2",
					fieldClass = float[][].class
			),
			@BoundBoxField(
					fieldName = "ss",
					fieldClass = ArrayList.class
			)
		}	
)
public class TestClassWithManyExtraFields {
    /*private String foo = "test";
    private int a = 2;
    private double[] array1 = { 0, 9 };
    private float[][] array2 = { { 0 }, { 9, 4 } };
    private ArrayList<String> ss = new ArrayList<String>();*/
}
