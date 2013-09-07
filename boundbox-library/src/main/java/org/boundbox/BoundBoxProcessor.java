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

package org.boundbox;

import java.io.IOException;
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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.TypeKindVisitor6;
import javax.tools.Diagnostic.Kind;

/**
 * Annotation processor
 * @author <a href=\"mailto:christoffer@christoffer.me\">Christoffer Pettersson</a>
 *         http://blog.retep
 *         .org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
 *         https://forums.oracle.com/thread/1184190
 */

@SupportedAnnotationTypes("org.boundbox.BoundBox")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BoundBoxProcessor extends AbstractProcessor {

    private static final String BOUNDBOX_ANNOTATION_PARAMETER_BOUND_CLASS = "boundClass";

    private Filer filer;
    private Messager messager;
    private IBoundboxWriter boundboxWriter = new BoundboxWriter();
    private BoundClassVisitor boundClassVisitor = new BoundClassVisitor();

    @Override
    public void init(ProcessingEnvironment env) {
        filer = env.getFiler();
        messager = env.getMessager();
    }
    
    public void setBoundboxWriter(IBoundboxWriter boundboxWriter) {
        this.boundboxWriter = boundboxWriter;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {

        // Get all classes that has the annotation
        Set<? extends Element> classElements = roundEnvironment.getElementsAnnotatedWith(BoundBox.class);

        // For each class that has the annotation
        for (final Element classElement : classElements) {

            // Get the annotation information
            TypeElement boundClass = null;
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
                        boundClass = getBoundClassAsTypeElement(entry);
                    }
                }
            }

            if( boundClass ==  null ) {
                messager.printMessage(Kind.WARNING,
                        "BoundClass is null : " + message,
                        classElement);
                return true;
            }

            
            boundClass.accept(boundClassVisitor, true);

            try {
                boundboxWriter.writeBoundBox(boundClass, filer, boundClassVisitor);
            } catch (IOException e) {
                e.printStackTrace();
                error(classElement, e.getMessage() );
            }
        }

        return true;
    }

    private TypeElement getBoundClassAsTypeElement(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry) {
        return entry.getValue().accept(new TypeNameExtractorAnnotationValueVisitor(), null);
    }

    private void error(final Element element, final String message) {
        processingEnv.getMessager().printMessage(Kind.ERROR, message, element);
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------
    private final class TypeNameExtractorAnnotationValueVisitor extends SimpleAnnotationValueVisitor6<TypeElement, Void> {
        @Override
        public TypeElement visitType(TypeMirror t, Void p) {
            TypeElement typeElement = t.accept(new SimpleTypeVisitor6<TypeElement, Void>() {

                @Override
                public TypeElement visitDeclared(DeclaredType declaredType, Void p) {
                    TypeElement typeElement = declaredType.asElement().accept(new ElementKindVisitor6<TypeElement, Void>() {
                        @Override
                        public TypeElement visitTypeAsClass(TypeElement e, Void p) {
                            for (Element enclosedElement : e.getEnclosedElements()) {
                                enclosedElement.accept(this, null);
                            }
                            return e;
                        }
                    }, null);
                    return typeElement;
                }

            }, null);
            return typeElement;
        }

    }

    public final class BoundClassVisitor extends ElementKindVisitor6<Void, Boolean> {


        private List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        private List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        private List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();


        public List<FieldInfo> getListFieldInfos() {
            return listFieldInfos;
        }

        public List<MethodInfo> getListMethodInfos() {
            return listMethodInfos;
        }

        public List<MethodInfo> getListConstructorInfos() {
            return listConstructorInfos;
        }

        @Override
        public Void visitTypeAsClass(TypeElement e, Boolean isBoundClass) {
            System.out.println("class ->" + e.getSimpleName());
            //http://stackoverflow.com/q/7738171/693752
            for (Element enclosedElement : e.getEnclosedElements()) {
                enclosedElement.accept(this, isBoundClass);
            }

            System.out.println("super class ->" + e.getSuperclass().toString());
            TypeMirror superclassOfBoundClass = e.getSuperclass();
            //TODO should be not needed to visit : http://stackoverflow.com/a/7739269/693752
            if( !"java.lang.Object".equals(superclassOfBoundClass.toString()) ) {
                superclassOfBoundClass.accept(new TypeKindVisitor6<Void, Void>() {
                    @Override
                    public Void visitDeclared(DeclaredType t, Void p) {
                        t.asElement().accept(BoundClassVisitor.this, false);
                        System.out.println("super declared type ->" + t.toString());
                        return super.visitDeclared(t, p);
                    }
                }, null);
            }
            return super.visitTypeAsClass(e, isBoundClass);
        }

        @Override
        public Void visitExecutable(ExecutableElement e, Boolean isBoundClass) {
            System.out.println("executable ->" + e.getSimpleName());
            MethodInfo methodInfo = new MethodInfo(e);
            if( methodInfo.isConstructor() ) {
                if( isBoundClass ) {
                    listConstructorInfos.add(methodInfo);
                }
            } else {
                //prevents methods overriden in subclass to be re-added in super class. 
                if( !listMethodInfos.contains( listMethodInfos ) ) {
                    listMethodInfos.add( methodInfo);
                    System.out.println("method ->" + methodInfo.getMethodName() + " added." );
                } else {
                    System.out.println("method ->" + methodInfo.getMethodName() + " already added.");
                }
            }
            return super.visitExecutable(e, isBoundClass);
        }

        @Override
        public Void visitVariableAsField(VariableElement e, Boolean isBoundClass) {
            FieldInfo fieldInfo = new FieldInfo(e);
            if( !listFieldInfos.contains( fieldInfo ) ) {
                listFieldInfos.add( fieldInfo);
                System.out.println("field ->" + fieldInfo.getFieldName() + " added." );
            } else {
                System.out.println("field ->" + fieldInfo.getFieldName() + " already added.");
            }
            return super.visitVariableAsField(e, isBoundClass);
        }
    }
    
    public BoundClassVisitor getBoundClassVisitor() {
        return boundClassVisitor;
    }
}
