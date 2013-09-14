package foo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.boundbox.BoundBox;

@SuppressWarnings({ "unused", "serial" })
@BoundBox(boundClass = TestClassWithParametrizedImports.class)
public class TestClassWithParametrizedImports extends HashMap<InputStream, OutputStream> {
    private List<CountDownLatch> latch;

    protected Set<File> foo() throws IOException {
        return null;
    }
}
