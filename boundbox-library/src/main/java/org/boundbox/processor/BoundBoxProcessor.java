/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */

package org.boundbox.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.boundbox.BoundBox;
import org.boundbox.model.ClassInfo;
import org.boundbox.writer.BoundboxWriter;
import org.boundbox.writer.IBoundboxWriter;

/**
 * Annotation processor
 *         http://blog.retep
 *         .org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
 *         https://forums.oracle.com/thread/1184190
 */
/*
 * TODO static initializers and instance initializers. Done ? 
 * TODO handle inner classes as bounded class ?
 * @author SNI
 *
 */
@SupportedAnnotationTypes("org.boundbox.BoundBox")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BoundBoxProcessor extends AbstractProcessor {

    private static final String BOUNDBOX_ANNOTATION_PARAMETER_BOUND_CLASS = "boundClass";
    private static final String BOUNDBOX_ANNOTATION_PARAMETER_MAX_SUPER_CLASS = "maxSuperClass";

    private Filer filer;
    private Messager messager;
    private Elements elements;
    private IBoundboxWriter boundboxWriter = new BoundboxWriter();
    private InheritanceComputer inheritanceComputer = new InheritanceComputer();
    private BoundClassScanner boundClassVisitor = new BoundClassScanner();
    private List<ClassInfo> listClassInfo = new ArrayList<ClassInfo>();

    @Override
    public void init(ProcessingEnvironment env) {
        filer = env.getFiler();
        messager = env.getMessager();
        elements = env.getElementUtils();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
        // Get all classes that has the annotation
        Set<? extends Element> classElements = roundEnvironment.getElementsAnnotatedWith(BoundBox.class);
        // For each class that has the annotation
        for (final Element classElement : classElements) {

            // Get the annotation information
            TypeElement boundClass = null;
            String maxSuperClass = null;
            List<? extends AnnotationMirror> listAnnotationMirrors = classElement.getAnnotationMirrors();
            if( listAnnotationMirrors ==  null ) {
                messager.printMessage(Kind.WARNING,
                        "listAnnotationMirrors is null",
                        classElement);
                return true;
            }

            String message = "";
            for( AnnotationMirror annotationMirror : listAnnotationMirrors ) {
                System.out.println( "mirror " + annotationMirror.getAnnotationType() );
                Map<? extends ExecutableElement, ? extends AnnotationValue > map = annotationMirror.getElementValues();
                for( Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : map.entrySet()) {
                    message += entry.getKey().getSimpleName().toString()+"\n";
                    message += entry.getValue().toString();
                    if( BOUNDBOX_ANNOTATION_PARAMETER_BOUND_CLASS.equals(entry.getKey().getSimpleName().toString())) {
                        boundClass = getAnnotationValueAsTypeElement(entry.getValue());
                    }
                    if( BOUNDBOX_ANNOTATION_PARAMETER_MAX_SUPER_CLASS.equals(entry.getKey().getSimpleName().toString())) {
                        maxSuperClass = getAnnotationValueAsTypeElement(entry.getValue()).asType().toString();
                    }
                }
            }

            if( boundClass ==  null ) {
                messager.printMessage(Kind.WARNING,
                        "BoundClass is null : " + message,
                        classElement);
                return true;
            }

            if( maxSuperClass != null ) {
                boundClassVisitor.setMaxSuperClass(maxSuperClass);
            }

            ClassInfo classInfo = boundClassVisitor.scan(boundClass);
            listClassInfo.add(classInfo);

            //perform some computations on meta model
            inheritanceComputer.computeInheritanceAndHiding(classInfo.getListFieldInfos());
            inheritanceComputer.computeInheritanceAndOverriding(classInfo.getListMethodInfos(), boundClass, elements);

            //write meta model to java class file
            try {
                String targetPackageName = classInfo.getTargetPackageName();
                String boundBoxClassName = classInfo.getBoundBoxClassName();

                String boundBoxFQN = targetPackageName.isEmpty() ? boundBoxClassName : targetPackageName+"."+boundBoxClassName; 
                JavaFileObject sourceFile = filer.createSourceFile(boundBoxFQN, (Element[]) null);
                Writer out = sourceFile.openWriter();

                boundboxWriter.writeBoundBox(classInfo, out);
            } catch (IOException e) {
                e.printStackTrace();
                error(classElement, e.getMessage() );
            }
        }

        return true;
    }

    public void setBoundboxWriter(IBoundboxWriter boundboxWriter) {
        this.boundboxWriter = boundboxWriter;
    }

    public List<ClassInfo> getListClassInfo() {
        return listClassInfo;
    }
    
    // ----------------------------------
    //  PRIVATE METHODS
    // ----------------------------------

    private TypeElement getAnnotationValueAsTypeElement(AnnotationValue annotationValue ) {
        DeclaredType declaredType = (DeclaredType) annotationValue.getValue();
        return (TypeElement) declaredType.asElement();
    }

    private void error(final Element element, final String message) {
        messager.printMessage(Kind.ERROR, message, element);
    }
}
