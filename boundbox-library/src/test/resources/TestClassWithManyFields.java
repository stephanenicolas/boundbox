import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithManyFields.class)
public class TestClassWithManyFields {
    private String foo = "test";
    private int a = 2;
    private double[] array1 = {0,9};
    private float[][] array2 = {{0},{9,4}};
}
