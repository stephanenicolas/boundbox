package org.boundbox;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;

import org.boundbox.BoundBoxProcessor.BoundClassVisitor;

public interface IBoundboxWriter {

    public abstract void writeBoundBox(TypeElement boundClass, Filer filer, BoundClassVisitor boundClassVisitor)
            throws IOException;

}
