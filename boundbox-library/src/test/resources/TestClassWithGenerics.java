import java.util.List;

import org.boundbox.BoundBox;

@BoundBox(boundClass = TestClassWithGenerics.class)
public class TestClassWithGenerics {
    public void doIt(List<String> strings) {
        // part of TDD for https://github.com/stephanenicolas/boundbox/issues/1
        //proposed by Flavien Laurent
    }
}
