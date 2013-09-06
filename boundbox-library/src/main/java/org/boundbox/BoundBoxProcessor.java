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
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.squareup.java.JavaWriter;

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

    @Override
    public void init(ProcessingEnvironment env) {
        filer = env.getFiler();
        messager = env.getMessager();
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
            String boundClassName = boundClass.getQualifiedName().toString();
            System.out.println( "BoundClassName is "+boundClassName );

            //BoundBox boundBoxAnnotation = classElement.getAnnotation(BoundBox.class);
            //Class<?> contractClassName = boundBoxAnnotation.boundClass();
            //boundClassName = boundClassName.substring(0, boundClassName.lastIndexOf('.'));
            String targetPackageName = boundClassName.substring(0, boundClassName.lastIndexOf('.'));
            String targetClassName = boundClassName.substring(boundClassName.lastIndexOf('.')+1);
            String boundBoxClassName = "BoundBoxOf"+targetClassName;

            BoundClassVisitor boundClassVisitor = new BoundClassVisitor();
            boundClass.accept(boundClassVisitor, null);

            Writer out = null;
            try {
                JavaFileObject sourceFile = filer.createSourceFile(targetPackageName+"."+boundBoxClassName, (Element[]) null);

                out = sourceFile.openWriter();
                JavaWriter writer = new JavaWriter(out);
                writer.emitPackage(targetPackageName)//
                .emitEmptyLine()//
                .emitImports(Field.class.getName())//
                .emitImports(Method.class.getName())//
                .emitImports(Constructor.class.getName())//
                .emitEmptyLine();

                writer.beginType(boundBoxClassName, "class", Modifier.PUBLIC | Modifier.FINAL, null) //
                .emitEmptyLine()//
                .emitField(targetClassName, "boundObject", Modifier.PRIVATE)//
                .emitEmptyLine()//
                .beginMethod(null, boundBoxClassName, Modifier.PUBLIC, targetClassName, "boundObject")//
                .emitStatement("this.boundObject = boundObject")//
                .endMethod()//
                .emitEmptyLine()//
                .emitEmptyLine();

                for( Entry<String, TypeMirror> entry : boundClassVisitor.getMapFieldNameToType().entrySet()) {
                    String fieldType = entry.getValue().toString();
                    String fieldName = entry.getKey();

                    createDirectGetter(writer, fieldType, fieldName);

                    writer.emitEmptyLine();

                    createDirectSetter(writer, fieldType, fieldName);
                }

                for( Entry<String, TypeMirror> entry : boundClassVisitor.getMapMethodNameToReturnType().entrySet()) {
                    String methodName = entry.getKey();
                    String returnType = entry.getValue().toString();
                    List<TypeMirror> parameterTypeList = boundClassVisitor.getMapMethodNameToParameterTypeList().get( methodName );
                    List<? extends TypeMirror> thrownTypeList = boundClassVisitor.getMapMethodNameToThrownTypeList().get(methodName);

                    writer.emitEmptyLine();
                    createMethodWrapper(writer, methodName, returnType, parameterTypeList, thrownTypeList, targetClassName);
                }

                writer.endType();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                error(classElement, "can't open java file " + targetClassName);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    private void createDirectSetter(JavaWriter writer, String fieldType, String fieldName) throws IOException {
        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String setterName = "boundBox_set" + fieldNameCamelCase;
        writer.beginMethod("void", setterName, Modifier.PUBLIC, fieldType, fieldName);
        writer.beginControlFlow("try");
        writer.emitStatement("Field field = boundObject.getClass().getDeclaredField(%s)", JavaWriter.stringLiteral(fieldName));
        writer.emitStatement("field.setAccessible(true)");
        writer.emitStatement("field.set(boundObject, %s)", fieldName);
        writer.endControlFlow();
        writer.beginControlFlow("catch( Exception e )");
        writer.emitStatement("  e.printStackTrace()", (Object[]) null);
        writer.endControlFlow();
        writer.endMethod();
    }

    private void createDirectGetter(JavaWriter writer, String fieldType, String fieldName) throws IOException {
        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String getterName = "boundBox_get" + fieldNameCamelCase;
        writer.beginMethod(fieldType, getterName, Modifier.PUBLIC);
        writer.beginControlFlow("try");
        writer.emitStatement("Field field = boundObject.getClass().getDeclaredField(%s)", JavaWriter.stringLiteral(fieldName));
        writer.emitStatement("field.setAccessible(true)");
        writer.emitStatement("return (%s) field.get(boundObject)", fieldType);
        writer.endControlFlow();
        writer.beginControlFlow("catch( Exception e )");
        writer.emitStatement("  e.printStackTrace()", (Object[]) null);
        writer.emitStatement("throw new RuntimeException()");
        writer.endControlFlow();
        writer.endMethod();
    }

    private void createMethodWrapper(JavaWriter writer, String methodName, String returnType, List<TypeMirror> parameterTypeList,
            List<? extends TypeMirror> thrownTypeList, String targetClassName) throws IOException {
        List<String> listParameters = new ArrayList<String>();
        int anonymousParamCount = 0;
        for (TypeMirror parameterType : parameterTypeList) {
            listParameters.add(parameterType.toString());
            String paramVariableName = "";
            if( "int".equals(parameterType.toString()) || "Double".equals(parameterType.toString()) ) {
                paramVariableName = "param"+anonymousParamCount++;
            } else {
                paramVariableName = computeCamelCaseNameStartLowerCase(parameterType.toString());
            }
            listParameters.add(paramVariableName);
        }
        boolean isConstructor = false;
        if( methodName.equals("<init>") ) {
            methodName = "boundBox_new";
            isConstructor = true;
            returnType = targetClassName;
        }
        String[] parameters = listParameters.toArray(new String[0]);
        writer.beginMethod(returnType, methodName, Modifier.PUBLIC, parameters);
        writer.beginControlFlow("try");
        if( !isConstructor ) {
            writer.emitStatement("Method method = boundObject.getClass().getDeclaredMethod(%s)", JavaWriter.stringLiteral(methodName));
        } else {
            writer.emitStatement("Constructor<? extends %s> method = boundObject.getClass().getDeclaredConstructor()", targetClassName);
        }
        writer.emitStatement("method.setAccessible(true)");
        
        listParameters = new ArrayList<String>();
        int anonymousParamCount2 = 0;
        for (TypeMirror parameterType : parameterTypeList) {
            String paramVariableName = "";
            if( "int".equals(parameterType.toString()) || "Double".equals(parameterType.toString()) ) {
                paramVariableName = "param"+anonymousParamCount2++;
            } else {
                paramVariableName = computeCamelCaseNameStartLowerCase(parameterType.toString());
            }
            listParameters.add(paramVariableName);
        }
        parameters = listParameters.toArray(new String[0]);
        String paramInvoke = "";
        for (String param : parameters) {
            paramInvoke += param + ", ";
        }
        String returnKeyWord = returnType.equals("void") ? "" : "return ";
        if( "int".equals(returnType) ) {
            returnKeyWord +=   "(Integer) ";
        }
        if( parameters.length > 0 ) {
            paramInvoke = paramInvoke.substring(0, paramInvoke.length() - 2);
            if( isConstructor ) {
                writer.emitStatement("method.newInstance(%s)", paramInvoke);
            } else {
                writer.emitStatement(returnKeyWord+"method.invoke(boundObject, %s)", paramInvoke);
            }
        } else {
            if( isConstructor ) {
                writer.emitStatement(returnKeyWord+"method.newInstance()");
            } else {
                writer.emitStatement(returnKeyWord+"method.invoke(boundObject)");
            }
        }
        writer.endControlFlow();
        writer.beginControlFlow("catch( Exception e )");
        writer.emitStatement("  e.printStackTrace()", (Object[]) null);
        writer.emitStatement("throw new RuntimeException()");
        writer.endControlFlow();
        writer.endMethod();
    }

    private String computeCamelCaseNameStartUpperCase(String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private String computeCamelCaseNameStartLowerCase(String fieldName) {
        return Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
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

    private final class BoundClassVisitor extends ElementKindVisitor6<Void, Void> {

        private Map<String, TypeMirror> mapFieldNameToType = new HashMap<String, TypeMirror>();
        private Map<String, TypeMirror> mapMethodNameToReturnType = new HashMap<String, TypeMirror>();
        private Map<String, List<TypeMirror>> mapMethodNameToParameterTypeList = new HashMap<String, List<TypeMirror>>();
        private Map<String, List<? extends TypeMirror>> mapMethodNameToThrownTypeList = new HashMap<String, List<? extends TypeMirror>>();

        public Map<String, TypeMirror> getMapFieldNameToType() {
            return mapFieldNameToType;
        }

        public Map<String, TypeMirror> getMapMethodNameToReturnType() {
            return mapMethodNameToReturnType;
        }

        public Map<String, List<TypeMirror>> getMapMethodNameToParameterTypeList() {
            return mapMethodNameToParameterTypeList;
        }

        public Map<String, List<? extends TypeMirror>> getMapMethodNameToThrownTypeList() {
            return mapMethodNameToThrownTypeList;
        }

        @Override
        public Void visitTypeAsClass(TypeElement e, Void p) {
            System.out.println("class ->" + e.getSimpleName());
            for (Element enclosedElement : e.getEnclosedElements()) {
                enclosedElement.accept(this, null);
            }
            return super.visitTypeAsClass(e, p);
        }

        @Override
        public Void visitPackage(PackageElement e, Void p) {
            System.out.println("package ->" + e.getSimpleName());
            return super.visitPackage(e, p);
        }

        @Override
        public Void visitType(TypeElement e, Void p) {
            System.out.println("type ->" + e.getSimpleName());
            return super.visitType(e, p);
        }

        @Override
        public Void visitExecutable(ExecutableElement e, Void p) {
            System.out.println("executable ->" + e.getSimpleName());
            mapMethodNameToReturnType.put(e.getSimpleName().toString(), e.getReturnType());
            List<TypeMirror> listTypeMirrors = new ArrayList<TypeMirror>();
            for (VariableElement element : e.getParameters()) {
                listTypeMirrors.add(element.asType());
            }
            mapMethodNameToParameterTypeList.put(e.getSimpleName().toString(), listTypeMirrors);
            mapMethodNameToThrownTypeList.put(e.getSimpleName().toString(), e.getThrownTypes());
            return super.visitExecutable(e, p);
        }

        @Override
        public Void visitVariable(VariableElement e, Void p) {
            System.out.println("variable ->" + e.getSimpleName());
            return super.visitVariable(e, p);
        }

        @Override
        public Void visitVariableAsField(VariableElement e, Void p) {
            mapFieldNameToType.put(e.getSimpleName().toString(), e.asType());
            System.out.println("field ->" + e.getSimpleName());
            return super.visitVariableAsField(e, p);
        }

    }
}
