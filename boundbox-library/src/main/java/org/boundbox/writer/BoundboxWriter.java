package org.boundbox.writer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import org.apache.commons.lang3.StringUtils;
import org.boundbox.BoundBoxException;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.Inheritable;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;

import com.squareup.javawriter.JavaWriter;

@Log
public class BoundboxWriter implements IBoundboxWriter {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final String SUPPRESS_WARNINGS_ALL = "SuppressWarnings(\"all\")";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Setter
    @Getter
    private boolean isWritingJavadoc = true;

    private DocumentationGenerator javadocGenerator = new DocumentationGenerator();
    private NamingGenerator namingGenerator = new NamingGenerator();

    // ----------------------------------
    // METHODS
    // ----------------------------------

    @Override
    public void writeBoundBox(ClassInfo classInfo, Writer out) throws IOException {
        JavaWriter writer = new JavaWriter(out);
        //TODO javawriter doesn't handle imports properly. V3.0.0 should change this
        //but for now just don't use imports, except a few.
        writer.setCompressingTypes(false);
        writeBoundBox(classInfo, writer);
    }

    protected void writeBoundBox(ClassInfo classInfo, JavaWriter writer) throws IOException {
        String boundClassName = classInfo.getClassName();
        log.info("BoundClassName is " + boundClassName);

        String targetPackageName = classInfo.getTargetPackageName();
        String targetClassName = classInfo.getTargetClassName();
        String boundBoxClassName = createBoundBoxName(classInfo);

        writer.emitPackage(targetPackageName)//
        .emitEmptyLine();

        //TODO javawriter doesn't handle imports properly. V3.0.0 should change this
        //but for now just don't use imports, except a few.
        classInfo.getListImports().clear();
        classInfo.getListImports().add(Field.class.getName());
        classInfo.getListImports().add(Method.class.getName());
        classInfo.getListImports().add(Constructor.class.getName());
        classInfo.getListImports().add(InvocationTargetException.class.getName());
        classInfo.getListImports().add(BoundBoxException.class.getName());
        writer.emitImports(classInfo.getListImports());

        // TODO search the inner class tree for imports

        writer.emitEmptyLine();
        writeJavadocForBoundBoxClass(writer, classInfo);
        writer.emitAnnotation(SUPPRESS_WARNINGS_ALL);
        createClassWrapper(writer, classInfo, targetClassName, boundBoxClassName);
    }

    private void createClassWrapper(JavaWriter writer, ClassInfo classInfo, String targetClassName, String boundBoxClassName) throws IOException {
        writer.beginType(boundBoxClassName, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), null)
        //
        .emitEmptyLine()
        //
        .emitField(Object.class.getName(), "boundObject", EnumSet.of(Modifier.PRIVATE))
        //
        .emitField("Class<?>", "boundClass", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC), targetClassName + ".class")//
        .emitEmptyLine();//

        writeJavadocForBoundBoxConstructor(writer, classInfo);
        writer.beginMethod(null, boundBoxClassName, EnumSet.of(Modifier.PUBLIC), Object.class.getName(), "boundObject")//
        .emitStatement("this.boundObject = boundObject")//
        .endMethod()//
        .emitEmptyLine();

        if( !classInfo.getListConstructorInfos().isEmpty() ) {
            writeCodeDecoration(writer, "Access to constructors");
            for (MethodInfo methodInfo : classInfo.getListConstructorInfos()) {
                writer.emitEmptyLine();
                writeJavadocForBoundConstructor(writer, classInfo, methodInfo);
                createMethodWrapper(writer, methodInfo, targetClassName, classInfo.getListSuperClassNames());
            }
        }

        if( !classInfo.getListFieldInfos().isEmpty() ) {
            writeCodeDecoration(writer, "Direct access to fields");
            for (FieldInfo fieldInfo : classInfo.getListFieldInfos()) {
                writeJavadocForBoundGetter(writer, fieldInfo, classInfo);
                createDirectGetter(writer, fieldInfo, classInfo.getListSuperClassNames());
                writer.emitEmptyLine();
                writeJavadocForBoundSetter(writer, fieldInfo, classInfo);
                createDirectSetter(writer, fieldInfo, classInfo.getListSuperClassNames());
            }
        }

        if( !classInfo.getListMethodInfos().isEmpty() ) {
            writeCodeDecoration(writer, "Access to methods");
            for (MethodInfo methodInfo : classInfo.getListMethodInfos()) {
                writer.emitEmptyLine();
                writeJavadocForBoundMethod(writer, classInfo, methodInfo);
                createMethodWrapper(writer, methodInfo, targetClassName, classInfo.getListSuperClassNames());
            }
        }

        if( !classInfo.getListInnerClassInfo().isEmpty() ) {
            writeCodeDecoration(writer, "Access to instances of inner classes");
            for (InnerClassInfo innerClassInfo : classInfo.getListInnerClassInfo()) {
                writer.emitEmptyLine();
                for (MethodInfo methodInfo : innerClassInfo.getListConstructorInfos()) {
                    writeJavadocForBoundInnerClassAccessor(writer, innerClassInfo, methodInfo);
                    createInnerClassAccessor(writer, innerClassInfo, methodInfo);
                }
            }

            writeCodeDecoration(writer, "Access to boundboxes of inner classes");
            for (InnerClassInfo innerClassInfo : classInfo.getListInnerClassInfo()) {
                writer.emitEmptyLine();
                writeJavadocForBoundInnerClass(writer, innerClassInfo);
                createInnerClassWrapper(writer, classInfo, innerClassInfo);
            }
        }


        writer.endType();
    }

    private void createInnerClassWrapper(JavaWriter writer, ClassInfo classInfo, InnerClassInfo innerClassInfo) throws IOException {
        EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.FINAL);
        if (innerClassInfo.isStaticInnerClass()) {
            modifiers.add(Modifier.STATIC);
        }
        String boundBoxClassName = createBoundBoxName(innerClassInfo);
        String enclosingBoundBoxClassName = createBoundBoxName(classInfo);
        String thisOrNot = ((classInfo instanceof InnerClassInfo) && !((InnerClassInfo) classInfo).isStaticInnerClass()) ? ".this" : "";
        EnumSet<Modifier> boundClassFieldModifiers = EnumSet.of(Modifier.PRIVATE);
        if (innerClassInfo.isStaticInnerClass()) {
            boundClassFieldModifiers.add(Modifier.STATIC);
        }
        writer.beginType(boundBoxClassName, "class", modifiers, null)
        //
        .emitEmptyLine()
        //
        .emitField(Object.class.getName(), "boundObject", EnumSet.of(Modifier.PRIVATE))
        //
        .emitField("Class<?>", "boundClass", boundClassFieldModifiers, enclosingBoundBoxClassName + thisOrNot + ".boundClass.getDeclaredClasses()[" + innerClassInfo.getInnerClassIndex() + "]")//
        .emitEmptyLine();//

        writeJavadocForBoundBoxConstructor(writer, innerClassInfo);
        writer.beginMethod(null, boundBoxClassName, EnumSet.of(Modifier.PUBLIC), Object.class.getName(), "boundObject")//
        .emitStatement("this.boundObject = boundObject")//
        .endMethod()//
        .emitEmptyLine();

        if( !innerClassInfo.getListFieldInfos().isEmpty() ) {
            writeCodeDecoration(writer, "Direct access to fields");
            for (FieldInfo fieldInfo : innerClassInfo.getListFieldInfos()) {
                writeJavadocForBoundGetter(writer, fieldInfo, innerClassInfo);
                createDirectGetterForInnerClass(writer, fieldInfo, innerClassInfo.getListSuperClassNames());
                writer.emitEmptyLine();
                writeJavadocForBoundSetter(writer, fieldInfo, innerClassInfo);
                createDirectSetterForInnerClass(writer, fieldInfo, innerClassInfo.getListSuperClassNames());
            }
        }

        if( !innerClassInfo.getListMethodInfos().isEmpty() ) {
            writeCodeDecoration(writer, "Access to methods");
            for (MethodInfo methodInfo : innerClassInfo.getListMethodInfos()) {
                writer.emitEmptyLine();
                writeJavadocForBoundMethod(writer, innerClassInfo, methodInfo);
                createMethodWrapperForInnerClass(writer, methodInfo, innerClassInfo.getListSuperClassNames());
            }
        }

        if( !innerClassInfo.getListInnerClassInfo().isEmpty() ) {
            writeCodeDecoration(writer, "Access to instances of inner classes");
            for (InnerClassInfo innerInnerClassInfo : innerClassInfo.getListInnerClassInfo()) {
                writer.emitEmptyLine();
                for (MethodInfo methodInfo : innerInnerClassInfo.getListConstructorInfos()) {
                    writeJavadocForBoundInnerClassAccessor(writer, innerInnerClassInfo, methodInfo);
                    createInnerClassAccessor(writer, innerInnerClassInfo, methodInfo);
                }
            }

            writeCodeDecoration(writer, "Access to boundboxes of inner classes");
            for (InnerClassInfo innerInnerClassInfo : innerClassInfo.getListInnerClassInfo()) {
                writer.emitEmptyLine();
                writeJavadocForBoundInnerClass(writer, innerInnerClassInfo);
                createInnerClassWrapper(writer, innerClassInfo, innerInnerClassInfo);
            }
        }

        writer.endType();
    }

    private String createBoundBoxName(ClassInfo classInfo) {
        return namingGenerator.createBoundBoxName(classInfo);
    }

    private void createDirectSetter(JavaWriter writer, FieldInfo fieldInfo, List<String> listSuperClassNames) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldTypeName();

        String fieldNameCamelCase = namingGenerator.computeCamelCaseNameStartUpperCase(fieldName);
        String setterName = namingGenerator.createSetterName(fieldInfo, listSuperClassNames, fieldNameCamelCase);
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (fieldInfo.isStaticField()) {
            modifiers.add(Modifier.STATIC);
        }

        String nameOfClassThatOwnsField = getSuperClassName(fieldInfo, listSuperClassNames);
        boolean isStaticField = fieldInfo.isStaticField();
        createSetterInvocation(writer, fieldName, fieldType, isStaticField, setterName, modifiers, nameOfClassThatOwnsField);
    }

    private void createDirectSetterForInnerClass(JavaWriter writer, FieldInfo fieldInfo, List<String> listSuperClassNames) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldTypeName();

        String fieldNameCamelCase = namingGenerator.computeCamelCaseNameStartUpperCase(fieldName);
        String setterName = namingGenerator.createSetterName(fieldInfo, listSuperClassNames, fieldNameCamelCase);
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (fieldInfo.isStaticField()) {
            modifiers.add(Modifier.STATIC);
        }

        String nameOfClassThatOwnsField = getSuperClassChain(fieldInfo);
        boolean isStaticField = fieldInfo.isStaticField();
        createSetterInvocation(writer, fieldName, fieldType, isStaticField, setterName, modifiers, nameOfClassThatOwnsField);
    }

    private void createSetterInvocation(JavaWriter writer, String fieldName, String fieldType, boolean isStaticField, String setterName, Set<Modifier> modifiers, String nameOfClassThatOwnsField)
            throws IOException {
        writer.beginMethod("void", setterName, modifiers, fieldType, fieldName);
        writer.beginControlFlow("try");
        writer.emitStatement("Field field = " + nameOfClassThatOwnsField + ".getDeclaredField(%s)", JavaWriter.stringLiteral(fieldName));
        writer.emitStatement("field.setAccessible(true)");
        String invocationTarget = isStaticField ? "null" : "boundObject";
        writer.emitStatement("field.set(%s, %s)", invocationTarget, fieldName);
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, Exception.class);
        writer.endMethod();
    }

    private void createDirectGetter(JavaWriter writer, FieldInfo fieldInfo, List<String> listSuperClassNames) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldTypeName();

        String fieldNameCamelCase = namingGenerator.computeCamelCaseNameStartUpperCase(fieldName);
        String getterName = namingGenerator.createGetterName(fieldInfo, listSuperClassNames, fieldNameCamelCase);
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (fieldInfo.isStaticField()) {
            modifiers.add(Modifier.STATIC);
        }

        boolean isStaticField = fieldInfo.isStaticField();
        String nameOfClassThatOwnsField = getSuperClassName(fieldInfo, listSuperClassNames);
        createGetterInvocation(writer, fieldName, fieldType, isStaticField, getterName, modifiers, nameOfClassThatOwnsField);
    }

    private void createDirectGetterForInnerClass(JavaWriter writer, FieldInfo fieldInfo, List<String> listSuperClassNames) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldTypeName();

        String fieldNameCamelCase = namingGenerator.computeCamelCaseNameStartUpperCase(fieldName);
        String getterName = namingGenerator.createGetterName(fieldInfo, listSuperClassNames, fieldNameCamelCase);
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (fieldInfo.isStaticField()) {
            modifiers.add(Modifier.STATIC);
        }

        boolean isStaticField = fieldInfo.isStaticField();
        String nameOfClassThatOwnsField = getSuperClassChain(fieldInfo);
        createGetterInvocation(writer, fieldName, fieldType, isStaticField, getterName, modifiers, nameOfClassThatOwnsField);
    }

    private void createGetterInvocation(JavaWriter writer, String fieldName, String fieldType, boolean isStaticField, String getterName, Set<Modifier> modifiers, String nameOfClassThatOwnsField)
            throws IOException {
        writer.beginMethod(fieldType, getterName, modifiers);
        writer.beginControlFlow("try");
        writer.emitStatement("Field field = " + nameOfClassThatOwnsField + ".getDeclaredField(%s)", JavaWriter.stringLiteral(fieldName));
        writer.emitStatement("field.setAccessible(true)");
        String castReturnType = createCastReturnTypeString(fieldType);

        String invocationTarget = isStaticField ? "null" : "boundObject";
        writer.emitStatement("return %s field.get(%s)", castReturnType, invocationTarget);
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, Exception.class);
        writer.endMethod();
    }

    private void createInnerClassAccessor(JavaWriter writer, InnerClassInfo innerClassInfo, MethodInfo methodInfo) throws IOException {
        String returnType = "Object";
        List<FieldInfo> parameterTypeList = methodInfo.getParameterTypes();

        List<String> parameters = createListOfParameterTypesAndNames(parameterTypeList);
        List<String> thrownTypesCommaSeparated = methodInfo.getThrownTypeNames();

        // beginBoundInvocationMethod
        String signature = namingGenerator.createInnerClassAccessorName(innerClassInfo);

        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (innerClassInfo.isStaticInnerClass()) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginMethod(returnType, signature, modifiers, parameters, thrownTypesCommaSeparated);

        writer.beginControlFlow("try");

        // emit method retrieval
        String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(parameterTypeList);

        if (innerClassInfo.isStaticInnerClass()) {
            if (parameterTypeList.isEmpty()) {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredClasses()[%d].getDeclaredConstructor()", innerClassInfo.getInnerClassIndex());
            } else {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredClasses()[%d].getDeclaredConstructor(%s)", innerClassInfo.getInnerClassIndex(), parametersTypesCommaSeparated);
            }
        } else {
            if (parameterTypeList.isEmpty()) {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredClasses()[%d].getDeclaredConstructor(boundClass)", innerClassInfo.getInnerClassIndex());
            } else {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredClasses()[%d].getDeclaredConstructor(boundClass, %s)", innerClassInfo.getInnerClassIndex(),
                        parametersTypesCommaSeparated);
            }
        }
        writer.emitStatement("method.setAccessible(true)");

        // emit method invocation
        String parametersNamesCommaSeparated = createListOfParametersNamesCommaSeparated(parameterTypeList);

        String returnString = "return ";

        if (parameterTypeList.isEmpty()) {
            if (innerClassInfo.isStaticInnerClass()) {
                writer.emitStatement(returnString + "method.newInstance()");
            } else {
                // TODO boundObject de la class externe
                writer.emitStatement(returnString + "method.newInstance(boundObject)");
            }
        } else {
            if (innerClassInfo.isStaticInnerClass()) {
                writer.emitStatement(returnString + "method.newInstance(%s)", parametersNamesCommaSeparated);
            } else {
                // TODO boundObject de la class externe
                writer.emitStatement(returnString + "method.newInstance(boundObject, %s)", parametersNamesCommaSeparated);
            }
        }
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, IllegalAccessException.class);
        addReflectionExceptionCatchClause(writer, IllegalArgumentException.class);
        addReflectionExceptionCatchClause(writer, InvocationTargetException.class);
        addReflectionExceptionCatchClause(writer, NoSuchMethodException.class);
        if (methodInfo.isConstructor()) {
            addReflectionExceptionCatchClause(writer, InstantiationException.class);
        }
        writer.endMethod();
    }

    private void createMethodWrapper(JavaWriter writer, MethodInfo methodInfo, String targetClassName, List<String> listSuperClassNames) throws IOException {
        String methodName = methodInfo.getMethodName();
        String returnType = methodInfo.getReturnTypeName();
        List<FieldInfo> parameterTypeList = methodInfo.getParameterTypes();

        List<String> parameters = createListOfParameterTypesAndNames(parameterTypeList);
        List<String> thrownTypesCommaSeparated = methodInfo.getThrownTypeNames();

        // beginBoundInvocationMethod
        boolean isConstructor = methodInfo.isConstructor();
        String signature = "";
        if (isConstructor) {
            signature = "boundBox_new";
            returnType = targetClassName;
        } else if (methodInfo.isStaticInitializer()) {
            signature = "boundBox_static_init";
            returnType = "void";
        } else if (methodInfo.isInstanceInitializer()) {
            signature = "boundBox_init";
            returnType = "void";
        } else {
            signature = namingGenerator.createMethodName(methodInfo, listSuperClassNames);
        }

        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (methodInfo.isStaticMethod() || methodInfo.isConstructor()) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginMethod(returnType, signature, modifiers, parameters, thrownTypesCommaSeparated);

        writer.beginControlFlow("try");

        // emit method retrieval
        String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(parameterTypeList);

        String superClassChain = getSuperClassName(methodInfo, listSuperClassNames);
        if (parameterTypeList.isEmpty()) {
            if (isConstructor || methodInfo.isInstanceInitializer()) {
                writer.emitStatement("Constructor<?> methodToInvoke = boundClass.getDeclaredConstructor()");
            } else {
                writer.emitStatement("Method methodToInvoke = " + superClassChain + ".getDeclaredMethod(%s)", JavaWriter.stringLiteral(methodName));
            }
        } else {
            if (isConstructor) {
                writer.emitStatement("Constructor<?> methodToInvoke = boundClass.getDeclaredConstructor(%s)", parametersTypesCommaSeparated);
            } else {
                writer.emitStatement("Method methodToInvoke = " + superClassChain + ".getDeclaredMethod(%s,%s)", JavaWriter.stringLiteral(methodName), parametersTypesCommaSeparated);
            }
        }
        writer.emitStatement("methodToInvoke.setAccessible(true)");

        // emit method invocation

        String returnString = "";
        if (methodInfo.isConstructor() || methodInfo.hasReturnType()) {
            returnString = "return " + createCastReturnTypeString(returnType);
        }

        String invocationTarget = methodInfo.isStaticMethod() ? "null" : "boundObject";
        if (parameterTypeList.isEmpty()) {
            if (isConstructor) {
                writer.emitStatement(returnString + "methodToInvoke.newInstance()");
            } else {
                writer.emitStatement(returnString + "methodToInvoke.invoke(%s)",invocationTarget);
            }
        } else {
            String parametersNamesCommaSeparated = createListOfParametersNamesCommaSeparated(parameterTypeList);
            if (isConstructor) {
                writer.emitStatement(returnString + "methodToInvoke.newInstance(%s)", parametersNamesCommaSeparated);
            } else {
                writer.emitStatement(returnString + "methodToInvoke.invoke(%s, %s)",invocationTarget, parametersNamesCommaSeparated);
            }
        }
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, IllegalAccessException.class);
        addReflectionExceptionCatchClause(writer, IllegalArgumentException.class);
        addReflectionExceptionCatchClause(writer, InvocationTargetException.class);
        addReflectionExceptionCatchClause(writer, NoSuchMethodException.class);
        if (methodInfo.isConstructor()) {
            addReflectionExceptionCatchClause(writer, InstantiationException.class);
        }
        writer.endMethod();
    }

    private void createMethodWrapperForInnerClass(JavaWriter writer, MethodInfo methodInfo, List<String> listSuperClassNames) throws IOException {
        String methodName = methodInfo.getMethodName();
        String returnType = methodInfo.getReturnTypeName();
        List<FieldInfo> parameterTypeList = methodInfo.getParameterTypes();

        List<String> parameters = createListOfParameterTypesAndNames(parameterTypeList);
        List<String> thrownTypesCommaSeparated = methodInfo.getThrownTypeNames();

        // beginBoundInvocationMethod
        String wrapperMethodName = namingGenerator.createMethodName(methodInfo, listSuperClassNames);
        if (methodInfo.isStaticInitializer()) {
            wrapperMethodName = "boundBox_static_init";
            if( methodInfo.getInheritanceLevel() >0 ) {
                wrapperMethodName += "_"+extractSimpleName(listSuperClassNames.get(methodInfo.getInheritanceLevel()));
            }
            returnType = "void";
        } else if (methodInfo.isInstanceInitializer()) {
            wrapperMethodName = "boundBox_init";
            returnType = "void";
        }

        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (methodInfo.isStaticMethod() || methodInfo.isConstructor()) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginMethod(returnType, wrapperMethodName, modifiers, parameters, thrownTypesCommaSeparated);

        writer.beginControlFlow("try");

        // emit method retrieval
        String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(parameterTypeList);

        String superClassChain = getSuperClassChain(methodInfo);
        if (parameterTypeList.isEmpty()) {
            writer.emitStatement("Method methodToInvoke = " + superClassChain + ".getDeclaredMethod(%s)", JavaWriter.stringLiteral(methodName));
        } else {
            writer.emitStatement("Method methodToInvoke = " + superClassChain + ".getDeclaredMethod(%s,%s)", JavaWriter.stringLiteral(methodName), parametersTypesCommaSeparated);
        }
        writer.emitStatement("methodToInvoke.setAccessible(true)");

        // emit method invocation

        String returnString = "";
        if (methodInfo.isConstructor() || methodInfo.hasReturnType()) {
            returnString = "return ";
            String castReturnTypeString = createCastReturnTypeString(returnType);
            returnString += castReturnTypeString;
        }

        String invocationTarget = methodInfo.isStaticMethod() ? "null" : "boundObject";
        if (parameterTypeList.isEmpty()) {
            writer.emitStatement(returnString + "methodToInvoke.invoke(%s)",invocationTarget);
        } else {
            String parametersNamesCommaSeparated = createListOfParametersNamesCommaSeparated(parameterTypeList);
            writer.emitStatement(returnString + "methodToInvoke.invoke(%s,%s)",invocationTarget, parametersNamesCommaSeparated);
        }

        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, IllegalAccessException.class);
        addReflectionExceptionCatchClause(writer, IllegalArgumentException.class);
        addReflectionExceptionCatchClause(writer, InvocationTargetException.class);
        addReflectionExceptionCatchClause(writer, NoSuchMethodException.class);
        if (methodInfo.isConstructor()) {
            addReflectionExceptionCatchClause(writer, InstantiationException.class);
        }
        writer.endMethod();
    }

    private void writeCodeDecoration(JavaWriter writer, String decorationTitle) throws IOException {
        for( String commentLine : javadocGenerator.generateCodeDecoration(decorationTitle)) {
            writer.emitSingleLineComment(commentLine);
        }
        writer.emitEmptyLine();
    }

    private void writeJavadocForBoundBoxClass(JavaWriter writer, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.generateJavadocForBoundBoxClass(classInfo));
        }
    }

    private void writeJavadocForBoundBoxConstructor(JavaWriter writer, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.generateJavadocForBoundBoxConstructor(classInfo));
        }
    }

    private void writeJavadocForBoundConstructor(JavaWriter writer, ClassInfo classInfo, MethodInfo methodInfo) throws IOException {
        if (isWritingJavadoc) {
            String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(methodInfo.getParameterTypes());
            writer.emitJavadoc(javadocGenerator.generateJavadocForBoundConstructor(classInfo, methodInfo, parametersTypesCommaSeparated));
        }
    }

    private void writeJavadocForBoundSetter(JavaWriter writer, FieldInfo fieldInfo, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.generateJavadocForBoundSetter(fieldInfo, classInfo));
        }
    }

    private void writeJavadocForBoundGetter(JavaWriter writer, FieldInfo fieldInfo, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.generateJavadocForBoundGetter(fieldInfo, classInfo));
        }
    }

    private void writeJavadocForBoundMethod(JavaWriter writer, ClassInfo classInfo, MethodInfo methodInfo) throws IOException {
        if (isWritingJavadoc) {
            String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(methodInfo.getParameterTypes());
            writer.emitJavadoc(javadocGenerator.generateJavadocForBoundMethod(classInfo, methodInfo, parametersTypesCommaSeparated));
        }
    }

    private void writeJavadocForBoundInnerClass(JavaWriter writer, InnerClassInfo innerClassInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.generateJavadocForBoundInnerClass(innerClassInfo));
        }
    }

    private void writeJavadocForBoundInnerClassAccessor(JavaWriter writer, InnerClassInfo innerInnerClassInfo, MethodInfo methodInfo) throws IOException {
        if (isWritingJavadoc) {
            String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(methodInfo.getParameterTypes());
            writer.emitJavadoc(javadocGenerator.generateJavadocForBoundInnerClassAccessor(innerInnerClassInfo, methodInfo, parametersTypesCommaSeparated));
        }
    }



    private String getSuperClassName(Inheritable inheritable, List<String> listSuperClassNames) {
        return listSuperClassNames.get(inheritable.getInheritanceLevel()) + ".class";
    }

    private String getSuperClassChain(Inheritable inheritable) {
        StringBuilder superClassChain = new StringBuilder("boundClass");
        for (int inheritanceLevel = 0; inheritanceLevel < inheritable.getInheritanceLevel(); inheritanceLevel++) {
            superClassChain.append(".getSuperclass()");
        }
        return superClassChain.toString();
    }

    private void addReflectionExceptionCatchClause(JavaWriter writer, Class<? extends Exception> exceptionClass) throws IOException {
        writer.beginControlFlow("catch( " + exceptionClass.getSimpleName() + " e )");
        writer.emitStatement("throw new BoundBoxException(e)");
        writer.endControlFlow();
    }

    // TODO use Types from processing environment ?
    private String createCastReturnTypeString(String returnType) {
        String castReturnTypeString = "";
        if ("int".equals(returnType)) {
            castReturnTypeString = "(Integer)";
        } else if ("long".equals(returnType)) {
            castReturnTypeString = "(Long)";
        } else if ("byte".equals(returnType)) {
            castReturnTypeString = "(Byte)";
        } else if ("short".equals(returnType)) {
            castReturnTypeString = "(Short)";
        } else if ("boolean".equals(returnType)) {
            castReturnTypeString = "(Boolean)";
        } else if ("double".equals(returnType)) {
            castReturnTypeString = "(Double)";
        } else if ("float".equals(returnType)) {
            castReturnTypeString = "(Float)";
        } else if ("char".equals(returnType)) {
            castReturnTypeString = "(Character)";
        } else {
            castReturnTypeString = "(" + returnType + ")";
        }

        if (!castReturnTypeString.isEmpty()) {
            castReturnTypeString += " ";
        }
        return castReturnTypeString;
    }

    private String createListOfParametersTypesCommaSeparated(List<FieldInfo> parameterTypeList) {
        List<String> listParameters = new ArrayList<String>();
        for (FieldInfo fieldInfo : parameterTypeList) {
            listParameters.add(extractRawType(fieldInfo.getFieldTypeName()) + ".class");
        }
        return StringUtils.join(listParameters, ", ");
    }

    private String extractRawType(String fieldTypeName) {
        return fieldTypeName.replaceAll("<.*>", "");
    }

    private String extractSimpleName(String className) {
        return className.contains(".") ? StringUtils.substringAfterLast(className, ".") : className;
    }

    private String createListOfParametersNamesCommaSeparated(List<FieldInfo> parameterTypeList) {
        List<String> listParameters = new ArrayList<String>();
        for (FieldInfo fieldInfo : parameterTypeList) {
            listParameters.add(fieldInfo.getFieldName());
        }
        return StringUtils.join(listParameters, ", ");
    }

    private List<String> createListOfParameterTypesAndNames(List<FieldInfo> parameterTypeList) {
        List<String> listParameters = new ArrayList<String>();
        for (FieldInfo fieldInfo : parameterTypeList) {
            listParameters.add(fieldInfo.getFieldTypeName());
            listParameters.add(fieldInfo.getFieldName());
        }
        return listParameters;
    }

}
