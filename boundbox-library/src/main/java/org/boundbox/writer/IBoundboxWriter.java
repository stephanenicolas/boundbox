package org.boundbox.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.boundbox.model.ClassInfo;

public interface IBoundboxWriter {

    public abstract void writeBoundBox(ClassInfo classInfo, List<String> listImports, Writer writer)
            throws IOException;

}
