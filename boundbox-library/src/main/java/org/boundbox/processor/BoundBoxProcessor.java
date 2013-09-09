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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.TypeKindVisitor6;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.boundbox.BoundBox;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.MethodInfo;
import org.boundbox.writer.BoundboxWriter;
import org.boundbox.writer.IBoundboxWriter;

/**
 * Annotation processor
 * @author <a href=\"mailto:christoffer@christoffer.me\">Christoffer Pettersson</a>
 *         http://blog.retep
 *         .org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
 *         https://forums.oracle.com/thread/1184190
 */
/*
 * TODO handle inheritance of bounded class
 * TODO handle imports inside boundbox class
 * TODO static methods
 * TODO static initializers
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
    private IBoundboxWriter boundboxWriter = new BoundboxWriter();
    private BoundClassVisitor boundClassVisitor = new BoundClassVisitor();
    private List<ClassInfo> listClassInfo = new ArrayList<ClassInfo>();

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
                        boundClass = getBoundClassAsTypeElement(entry);
                    }
                    if( BOUNDBOX_ANNOTATION_PARAMETER_MAX_SUPER_CLASS.equals(entry.getKey().getSimpleName().toString())) {
                        maxSuperClass = getBoundClassAsTypeElement(entry).asType().toString();
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
            
            try {
                String targetPackageName = classInfo.getTargetPackageName();
                String boundBoxClassName = classInfo.getBoundBoxClassName();

                String boundBoxFQN = targetPackageName == null ? boundBoxClassName : targetPackageName+"."+boundBoxClassName; 
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

    public final class BoundClassVisitor extends ElementKindVisitor6<Void, Integer> {

        private String maxSuperClassName = Object.class.getName();
        private List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        private List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        private List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();

        public ClassInfo scan( TypeElement boundClass ) {
            boundClass.accept(this, 0);
            ClassInfo classInfo = new ClassInfo(boundClass.getQualifiedName().toString());
            classInfo.setListFieldInfos(new ArrayList<FieldInfo>(listFieldInfos));
            classInfo.setListMethodInfos(new ArrayList<MethodInfo>(listMethodInfos));
            classInfo.setListConstructorInfos(new ArrayList<MethodInfo>(listConstructorInfos));
            listClassInfo.add(classInfo);
            listConstructorInfos.clear();
            listMethodInfos.clear();
            listFieldInfos.clear();
            maxSuperClassName = Object.class.getName();
            return classInfo;
        }

        public void setMaxSuperClass(Class<?> maxSuperClass) {
            this.maxSuperClassName = maxSuperClass.getName();
        }
        
        public void setMaxSuperClass(String className ) {
            this.maxSuperClassName = className;
        }

        public String getMaxSuperClass() {
            return maxSuperClassName;
        }

        @Override
        public Void visitTypeAsClass(TypeElement e, final Integer inheritanceLevel) {
            System.out.println("class ->" + e.getSimpleName());
            boolean isInnerClass = e.getNestingKind().isNested();
            System.out.println("nested ->" + isInnerClass);
            if( isInnerClass ) {
                return super.visitTypeAsClass(e, inheritanceLevel);
            }
            //http://stackoverflow.com/q/7738171/693752
            for (Element enclosedElement : e.getEnclosedElements()) {
                enclosedElement.accept(this, inheritanceLevel);
            }

            System.out.println("super class ->" + e.getSuperclass().toString());
            TypeMirror superclassOfBoundClass = e.getSuperclass();
            //TODO should be not needed to visit : http://stackoverflow.com/a/7739269/693752
            if( !maxSuperClassName.equals(superclassOfBoundClass.toString()) ) {
                superclassOfBoundClass.accept(new TypeKindVisitor6<Void, Void>() {
                    @Override
                    public Void visitDeclared(DeclaredType t, Void p) {
                        t.asElement().accept(BoundClassVisitor.this, inheritanceLevel + 1);
                        System.out.println("super declared type ->" + t.toString());
                        return super.visitDeclared(t, p);
                    }
                }, null);
            }
            return super.visitTypeAsClass(e, inheritanceLevel);
        }

        @Override
        public Void visitExecutable(ExecutableElement e, Integer inheritanceLevel) {
            System.out.println("executable ->" + e.getSimpleName());
            MethodInfo methodInfo = new MethodInfo(e);
            if( methodInfo.isConstructor() ) {
                if( inheritanceLevel ==0 ) {
                    listConstructorInfos.add(methodInfo);
                }
            } else {
                methodInfo.setInheritanceLevel( inheritanceLevel );
                //prevents methods overriden in subclass to be re-added in super class. 
                if( !listMethodInfos.contains( listMethodInfos ) ) {
                    listMethodInfos.add( methodInfo);
                    System.out.println("method ->" + methodInfo.getMethodName() + " added." );
                } else {
                    System.out.println("method ->" + methodInfo.getMethodName() + " already added.");
                }
            }
            return super.visitExecutable(e, inheritanceLevel);
        }

        @Override
        public Void visitVariableAsField(VariableElement e, Integer inheritanceLevel) {
            FieldInfo fieldInfo = new FieldInfo(e);
            fieldInfo.setInheritanceLevel( inheritanceLevel );
            if( !listFieldInfos.contains( fieldInfo ) ) {
                listFieldInfos.add( fieldInfo);
                System.out.println("field ->" + fieldInfo.getFieldName() + " added." );
            } else {
                System.out.println("field ->" + fieldInfo.getFieldName() + " already added.");
            }
            return super.visitVariableAsField(e, inheritanceLevel);
        }
        
    }

    public List<ClassInfo> getListClassInfo() {
        return listClassInfo;
    }
}
