package org.boundbox.writer;

import java.io.IOException;
import java.io.Writer;

import org.boundbox.model.ClassInfo;

public interface IBoundboxWriter {

    public abstract void writeBoundBox(ClassInfo classInfo, Writer writer) throws IOException;

}
