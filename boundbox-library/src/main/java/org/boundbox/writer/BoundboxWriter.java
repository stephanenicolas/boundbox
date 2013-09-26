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
    private static final String CODE_DECORATOR_TITLE_PREFIX = "\t";
    private static final String CODE_DECORATOR = "******************************";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Setter
    @Getter
    private boolean isWritingJavadoc = true;

    private JavadocGenerator javadocGenerator = new JavadocGenerator();
    // ----------------------------------
    // METHODS
    // ----------------------------------

    @Override
    public void writeBoundBox(ClassInfo classInfo, Writer out) throws IOException {
        JavaWriter writer = new JavaWriter(out);
        writeBoundBox(classInfo, writer);
    }

    protected void writeBoundBox(ClassInfo classInfo, JavaWriter writer) throws IOException {
        String boundClassName = classInfo.getClassName();
        log.info("BoundClassName is " + boundClassName);

        String targetPackageName = classInfo.getTargetPackageName();
        String targetClassName = classInfo.getTargetClassName();
        String boundBoxClassName = createBoundBoxName(classInfo);

        try {
            writer.emitPackage(targetPackageName)//
            .emitEmptyLine();

            classInfo.getListImports().add(Field.class.getName());
            classInfo.getListImports().add(Method.class.getName());
            classInfo.getListImports().add(Constructor.class.getName());
            classInfo.getListImports().add(InvocationTargetException.class.getName());
            classInfo.getListImports().add(BoundBoxException.class.getName());
            writer.emitImports(classInfo.getListImports());

            //TODO search the inner class tree for imports

            writer.emitEmptyLine();
            writeJavadocForBoundBoxClass(writer, classInfo);
            writer.emitAnnotation(SUPPRESS_WARNINGS_ALL);
            createClassWrapper(writer, classInfo, targetClassName, boundBoxClassName);

        } finally {
            if (writer != null) {
                writer.close();
            }
        }
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

        writeCodeDecoration(writer, "Access to constructors");
        for (MethodInfo methodInfo : classInfo.getListConstructorInfos()) {
            writer.emitEmptyLine();
            writeJavadocForBoundConstructor(writer, classInfo, methodInfo);
            createMethodWrapper(writer, methodInfo, targetClassName, classInfo.getListSuperClassNames());
        }

        writeCodeDecoration(writer, "Direct access to fields");
        for (FieldInfo fieldInfo : classInfo.getListFieldInfos()) {
            writeJavadocForBoundGetter(writer, fieldInfo, classInfo);
            createDirectGetter(writer, fieldInfo, classInfo.getListSuperClassNames());
            writer.emitEmptyLine();
            writeJavadocForBoundSetter(writer, fieldInfo, classInfo);
            createDirectSetter(writer, fieldInfo, classInfo.getListSuperClassNames());
        }

        writeCodeDecoration(writer, "Access to methods");
        for (MethodInfo methodInfo : classInfo.getListMethodInfos()) {
            writer.emitEmptyLine();
            writeJavadocForBoundMethod(writer, classInfo, methodInfo);
            createMethodWrapper(writer, methodInfo, targetClassName, classInfo.getListSuperClassNames());
        }

        writeCodeDecoration(writer, "Access to instances of inner classes");
        for (InnerClassInfo innerClassInfo : classInfo.getListInnerClassInfo()) {
            writer.emitEmptyLine();
            for (MethodInfo methodInfo : innerClassInfo.getListConstructorInfos()) {
                //TODO write javadoc generation method for inner classes.
                //writeJavadocForBoundMethod(writer, classInfo, methodInfo);
                createInnerClassAccessor(writer, innerClassInfo, methodInfo);
            }
        }

        writeCodeDecoration(writer, "Access to boundboxes of inner classes");
        for (InnerClassInfo innerClassInfo : classInfo.getListInnerClassInfo()) {
            writer.emitEmptyLine();
            //TODO write javadoc generation method for inner classes.
            //writeJavadocForBoundMethod(writer, classInfo, methodInfo);
            createInnerClassWrapper(writer, classInfo, innerClassInfo);
        }

        writer.endType();
    }

    private void createInnerClassWrapper(JavaWriter writer, ClassInfo classInfo, InnerClassInfo innerClassInfo) throws IOException {
        EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.FINAL);
        if( innerClassInfo.isStaticInnerClass() ) {
            modifiers.add(Modifier.STATIC);
        }
        String boundBoxClassName = createBoundBoxName(innerClassInfo);
        String enclosingBoundBoxClassName = createBoundBoxName(classInfo);
        String thisOrNot = ((classInfo instanceof InnerClassInfo) && !((InnerClassInfo)classInfo).isStaticInnerClass()) ? ".this" : "";
        EnumSet<Modifier> boundClassFieldModifiers = EnumSet.of(Modifier.PRIVATE);
        if( innerClassInfo.isStaticInnerClass() ) {
            boundClassFieldModifiers.add(Modifier.STATIC);
        }
        writer.beginType(boundBoxClassName, "class", modifiers, null)
        //
        .emitEmptyLine()
        //
        .emitField(Object.class.getName(), "boundObject", EnumSet.of(Modifier.PRIVATE))
        //
        .emitField("Class<?>", "boundClass", boundClassFieldModifiers, enclosingBoundBoxClassName+thisOrNot+".boundClass.getDeclaredClasses()["+innerClassInfo.getInnerClassIndex()+"]")//
        .emitEmptyLine();//

        writeJavadocForBoundBoxConstructor(writer, innerClassInfo);
        writer.beginMethod(null, boundBoxClassName, EnumSet.of(Modifier.PUBLIC), Object.class.getName(), "boundObject")//
        .emitStatement("this.boundObject = boundObject")//
        .endMethod()//
        .emitEmptyLine();

        writeCodeDecoration(writer, "Direct access to fields");
        for (FieldInfo fieldInfo : innerClassInfo.getListFieldInfos()) {
            writeJavadocForBoundGetter(writer, fieldInfo, innerClassInfo);
            createDirectGetterForInnerClass(writer, fieldInfo, innerClassInfo.getListSuperClassNames());
            writer.emitEmptyLine();
            writeJavadocForBoundSetter(writer, fieldInfo, innerClassInfo);
            createDirectSetterForInnerClass(writer, fieldInfo, innerClassInfo.getListSuperClassNames());
        }

        writeCodeDecoration(writer, "Access to methods");
        for (MethodInfo methodInfo : innerClassInfo.getListMethodInfos()) {
            writer.emitEmptyLine();
            writeJavadocForBoundMethod(writer, innerClassInfo, methodInfo);
            createMethodWrapperForInnerClass(writer, methodInfo, innerClassInfo.getListSuperClassNames());
        }

        writeCodeDecoration(writer, "Access to instances of inner classes");
        for (InnerClassInfo innerInnerClassInfo : innerClassInfo.getListInnerClassInfo()) {
            writer.emitEmptyLine();
            for (MethodInfo methodInfo : innerInnerClassInfo.getListConstructorInfos()) {
                //TODO write javadoc generation method for inner classes.
                //writeJavadocForBoundMethod(writer, classInfo, methodInfo);
                createInnerClassAccessor(writer, innerInnerClassInfo, methodInfo);
            }
        }

        writeCodeDecoration(writer, "Access to boundboxes of inner classes");
        for (InnerClassInfo innerInnerClassInfo : innerClassInfo.getListInnerClassInfo()) {
            writer.emitEmptyLine();
            //TODO write javadoc generation method for inner classes.
            //writeJavadocForBoundMethod(writer, classInfo, methodInfo);
            createInnerClassWrapper(writer, innerClassInfo, innerInnerClassInfo);
        }

        writer.endType();
    }


    private String createBoundBoxName(ClassInfo classInfo) {
        String className = classInfo.getClassName().contains(".") ? StringUtils.substringAfterLast(classInfo.getClassName(),".") : classInfo.getClassName();
        return "BoundBoxOf"+className;
    }

    private void writeCodeDecoration(JavaWriter writer, String decorationTitle) throws IOException {
        writer.emitSingleLineComment(CODE_DECORATOR);
        writer.emitSingleLineComment(CODE_DECORATOR_TITLE_PREFIX + decorationTitle);
        writer.emitSingleLineComment(CODE_DECORATOR);
        writer.emitEmptyLine();
    }

    private void createDirectSetter(JavaWriter writer, FieldInfo fieldInfo, List<String> listSuperClassNames) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldTypeName();

        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String setterName = createSignatureSetter(fieldInfo, listSuperClassNames, fieldNameCamelCase);
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

        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String setterName = createSignatureSetter(fieldInfo, listSuperClassNames, fieldNameCamelCase);
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
        if (isStaticField) {
            writer.emitStatement("field.set(null, %s)", fieldName);
        } else {
            writer.emitStatement("field.set(boundObject, %s)", fieldName);
        }
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, Exception.class);
        writer.endMethod();
    }

    private void createDirectGetter(JavaWriter writer, FieldInfo fieldInfo, List<String> listSuperClassNames) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldTypeName();

        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String getterName = createSignatureGetter(fieldInfo, listSuperClassNames, fieldNameCamelCase);
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

        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String getterName = createSignatureGetter(fieldInfo, listSuperClassNames, fieldNameCamelCase);
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

        if (isStaticField) {
            writer.emitStatement("return %s field.get(null)", castReturnType);
        } else {
            writer.emitStatement("return %s field.get(boundObject)", castReturnType);
        }
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
        String signature = "boundBox_new_"+innerClassInfo.getClassName();

        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (innerClassInfo.isStaticInnerClass()) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginMethod(returnType, signature, modifiers, parameters, thrownTypesCommaSeparated);

        writer.beginControlFlow("try");

        // emit method retrieval
        String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(parameterTypeList);

        if( innerClassInfo.isStaticInnerClass() ) {
            if (parameterTypeList.isEmpty()) {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredClasses()[%d].getDeclaredConstructor()", innerClassInfo.getInnerClassIndex());
            } else {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredClasses()[%d].getDeclaredConstructor(%s)",innerClassInfo.getInnerClassIndex(), parametersTypesCommaSeparated);
            }
        } else {
            if (parameterTypeList.isEmpty()) {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredClasses()[%d].getDeclaredConstructor(boundClass)", innerClassInfo.getInnerClassIndex());
            } else {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredClasses()[%d].getDeclaredConstructor(boundClass, %s)",innerClassInfo.getInnerClassIndex(), parametersTypesCommaSeparated);
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
                //TODO boundObject de la class externe
                writer.emitStatement(returnString + "method.newInstance(boundObject)");
            }
        } else {
            if (innerClassInfo.isStaticInnerClass()) {
                writer.emitStatement(returnString + "method.newInstance(%s)", parametersNamesCommaSeparated);
            } else {
                //TODO boundObject de la class externe
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
            signature = createSignatureMethod(methodInfo, listSuperClassNames);
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
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredConstructor()");
            } else {
                writer.emitStatement("Method method = " + superClassChain + ".getDeclaredMethod(%s)", JavaWriter.stringLiteral(methodName));
            }
        } else {
            if (isConstructor) {
                writer.emitStatement("Constructor<?> method = boundClass.getDeclaredConstructor(%s)", parametersTypesCommaSeparated);
            } else {
                writer.emitStatement("Method method = " + superClassChain + ".getDeclaredMethod(%s,%s)", JavaWriter.stringLiteral(methodName), parametersTypesCommaSeparated);
            }
        }
        writer.emitStatement("method.setAccessible(true)");

        // emit method invocation
        String parametersNamesCommaSeparated = createListOfParametersNamesCommaSeparated(parameterTypeList);

        String returnString = "";
        if (methodInfo.isConstructor() || methodInfo.hasReturnType()) {
            returnString = "return ";
            String castReturnTypeString = createCastReturnTypeString(returnType);
            returnString += castReturnTypeString;
        }

        if (parameterTypeList.isEmpty()) {
            if (isConstructor) {
                writer.emitStatement(returnString + "method.newInstance()");
            } else if (methodInfo.isStaticMethod()) {
                writer.emitStatement(returnString + "method.invoke(null)");
            } else {
                writer.emitStatement(returnString + "method.invoke(boundObject)");
            }
        } else {
            if (isConstructor) {
                writer.emitStatement(returnString + "method.newInstance(%s)", parametersNamesCommaSeparated);
            } else if (methodInfo.isStaticMethod()) {
                writer.emitStatement(returnString + "method.invoke(null, %s)", parametersNamesCommaSeparated);
            } else {
                writer.emitStatement(returnString + "method.invoke(boundObject, %s)", parametersNamesCommaSeparated);
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
        String signature = createSignatureMethod(methodInfo, listSuperClassNames);
        if (methodInfo.isStaticInitializer()) {
            signature = "boundBox_static_init";
            returnType = "void";
        } else if (methodInfo.isInstanceInitializer()) {
            signature = "boundBox_init";
            returnType = "void";
        }

        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (methodInfo.isStaticMethod() || methodInfo.isConstructor()) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginMethod(returnType, signature, modifiers, parameters, thrownTypesCommaSeparated);

        writer.beginControlFlow("try");

        // emit method retrieval
        String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(parameterTypeList);

        String superClassChain = getSuperClassChain(methodInfo);
        if (parameterTypeList.isEmpty()) {
            writer.emitStatement("Method method = " + superClassChain + ".getDeclaredMethod(%s)", JavaWriter.stringLiteral(methodName));
        } else {
            writer.emitStatement("Method method = " + superClassChain + ".getDeclaredMethod(%s,%s)", JavaWriter.stringLiteral(methodName), parametersTypesCommaSeparated);
        }
        writer.emitStatement("method.setAccessible(true)");

        // emit method invocation
        String parametersNamesCommaSeparated = createListOfParametersNamesCommaSeparated(parameterTypeList);

        String returnString = "";
        if (methodInfo.isConstructor() || methodInfo.hasReturnType()) {
            returnString = "return ";
            String castReturnTypeString = createCastReturnTypeString(returnType);
            returnString += castReturnTypeString;
        }

        if (parameterTypeList.isEmpty()) {
            if (methodInfo.isStaticMethod()) {
                writer.emitStatement(returnString + "method.invoke(null)");
            } else {
                writer.emitStatement(returnString + "method.invoke(boundObject)");
            }
        } else {
            if (methodInfo.isStaticMethod()) {
                writer.emitStatement(returnString + "method.invoke(null, %s)", parametersNamesCommaSeparated);
            } else {
                writer.emitStatement(returnString + "method.invoke(boundObject, %s)", parametersNamesCommaSeparated);
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

    private void writeJavadocForBoundBoxClass(JavaWriter writer, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.writeJavadocForBoundBoxClass(classInfo));
        }
    }

    private void writeJavadocForBoundBoxConstructor(JavaWriter writer, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.writeJavadocForBoundBoxConstructor(classInfo));
        }
    }

    private void writeJavadocForBoundConstructor(JavaWriter writer, ClassInfo classInfo, MethodInfo methodInfo) throws IOException {
        if (isWritingJavadoc) {
            String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(methodInfo.getParameterTypes());
            writer.emitJavadoc(javadocGenerator.writeJavadocForBoundConstructor(classInfo, methodInfo, parametersTypesCommaSeparated));
        }
    }

    private void writeJavadocForBoundSetter(JavaWriter writer, FieldInfo fieldInfo, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.writeJavadocForBoundSetter(fieldInfo, classInfo));
        }
    }

    private void writeJavadocForBoundGetter(JavaWriter writer, FieldInfo fieldInfo, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            writer.emitJavadoc(javadocGenerator.writeJavadocForBoundGetter(fieldInfo, classInfo));
        }
    }

    private void writeJavadocForBoundMethod(JavaWriter writer, ClassInfo classInfo, MethodInfo methodInfo) throws IOException {
        if (isWritingJavadoc) {
            String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(methodInfo.getParameterTypes());
            writer.emitJavadoc(javadocGenerator.writeJavadocForBoundMethod(classInfo, methodInfo, parametersTypesCommaSeparated));
        }
    }

    private String createSignatureGetter(FieldInfo fieldInfo, List<String> listSuperClassNames, String fieldNameCamelCase) {
        String getterName;
        if (fieldInfo.getEffectiveInheritanceLevel() == 0) {
            getterName = "boundBox_get" + fieldNameCamelCase;
        } else {
            String superClassName = listSuperClassNames.get(fieldInfo.getEffectiveInheritanceLevel());
            getterName = "boundBox_super_" + superClassName + "_get" + fieldNameCamelCase;
        }
        return getterName;
    }

    private String createSignatureSetter(FieldInfo fieldInfo, List<String> listSuperClassNames, String fieldNameCamelCase) {
        String getterName;
        if (fieldInfo.getEffectiveInheritanceLevel() == 0) {
            getterName = "boundBox_set" + fieldNameCamelCase;
        } else {
            String superClassName = listSuperClassNames.get(fieldInfo.getEffectiveInheritanceLevel());
            getterName = "boundBox_super_" + superClassName + "_set" + fieldNameCamelCase;
        }
        return getterName;
    }

    private String createSignatureMethod(MethodInfo methodInfo, List<String> listSuperClassNames) {
        String getterName;
        if (methodInfo.getEffectiveInheritanceLevel() == 0) {
            getterName = methodInfo.getMethodName();
        } else {
            String superClassName = listSuperClassNames.get(methodInfo.getEffectiveInheritanceLevel());
            getterName = "boundBox_super_" + superClassName + "_" + methodInfo.getMethodName();
        }
        return getterName;
    }

    private String getSuperClassName(Inheritable inheritable, List<String> listSuperClassNames) {
        return listSuperClassNames.get(inheritable.getInheritanceLevel()) + ".class";
    }

    private String getSuperClassChain(Inheritable inheritable) {
        StringBuilder superClassChain = new StringBuilder("boundClass");
        for( int inheritanceLevel=0; inheritanceLevel<inheritable.getInheritanceLevel(); inheritanceLevel++) {
            superClassChain.append(".getSuperClass()");
        }
        return superClassChain.toString();
    }


    private void addReflectionExceptionCatchClause(JavaWriter writer, Class<? extends Exception> exceptionClass) throws IOException {
        writer.beginControlFlow("catch( " + exceptionClass.getSimpleName() + " e )");
        writer.emitStatement("throw new BoundBoxException(e)");
        writer.endControlFlow();
    }

    //TODO use Types from processing environment ?
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

    private String computeCamelCaseNameStartUpperCase(String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

}
