package foo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithImports.class)
public class TestClassWithImports {
    private CountDownLatch latch;

    protected File foo() throws IOException {
        return null;
    }
}
