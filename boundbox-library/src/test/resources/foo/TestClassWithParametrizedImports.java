package foo;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass=TestClassWithParametrizedImports.class)
public class TestClassWithParametrizedImports {
    private List<CountDownLatch> latch;
    
    protected Set<File> foo() throws IOException {
        return null;
    }
}
