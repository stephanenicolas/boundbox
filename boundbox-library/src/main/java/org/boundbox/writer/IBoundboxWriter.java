package org.boundbox.writer;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;

import org.boundbox.processor.BoundBoxProcessor.BoundClassVisitor;

public interface IBoundboxWriter {

    public abstract void writeBoundBox(TypeElement boundClass, Filer filer, BoundClassVisitor boundClassVisitor)
            throws IOException;

}
