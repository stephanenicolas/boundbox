import org.boundbox.BoundBox;

//not really inherited but flattened
@BoundBox(boundClass = TestClassWithStaticInheritedAndHiddingInnerClass.class)
public class TestClassWithStaticInheritedAndHiddingInnerClass extends TestClassWithStaticInheritedInnerClass{
    public static class InnerClass {
    }
}
