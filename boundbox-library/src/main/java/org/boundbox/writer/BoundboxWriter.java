package org.boundbox.writer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.boundbox.BoundBoxException;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.Inheritable;
import org.boundbox.model.MethodInfo;

import com.squareup.javawriter.JavaWriter;

public class BoundboxWriter implements IBoundboxWriter {

    private static final String CODE_DECORATOR_TITLE_PREFIX = "\t";
    private static final String CODE_DECORATOR = "******************************";

    // TODO : refactor as
    // public void writeBoundBox(ClassInfo boundClassInfo, Writer writer) throws IOException {

    @Override
    public void writeBoundBox(ClassInfo classInfo, Writer out) throws IOException {
        String boundClassName = classInfo.getClassName();
        System.out.println("BoundClassName is " + boundClassName);

        String targetPackageName = classInfo.getTargetPackageName();
        String targetClassName = classInfo.getTargetClassName();
        String boundBoxClassName = classInfo.getBoundBoxClassName();

        try {
            JavaWriter writer = new JavaWriter(out);
            writer.emitPackage(targetPackageName)//
            .emitEmptyLine();

            classInfo.getListImports().add(Field.class.getName());
            classInfo.getListImports().add(Method.class.getName());
            classInfo.getListImports().add(Constructor.class.getName());
            classInfo.getListImports().add(InvocationTargetException.class.getName());
            classInfo.getListImports().add(BoundBoxException.class.getName());
            writer.emitImports(classInfo.getListImports());

            writer.emitAnnotation("SuppressWarnings(\"all\")");
            writer.beginType(boundBoxClassName, "class", newHashSet(Modifier.PUBLIC, Modifier.FINAL), null)
            //
            .emitEmptyLine()
            //
            .emitField(targetClassName, "boundObject", newHashSet(Modifier.PRIVATE))
            //
            .emitField("Class<" + targetClassName + ">", "boundClass", newHashSet(Modifier.PRIVATE, Modifier.STATIC),
                    targetClassName + ".class")//
                    .emitEmptyLine()//
                    .beginMethod(null, boundBoxClassName, newHashSet(Modifier.PUBLIC), targetClassName, "boundObject")//
                    .emitStatement("this.boundObject = boundObject")//
                    .endMethod()//
                    .emitEmptyLine();

            writeCodeDecoration(writer, "Access to constructors");
            for (MethodInfo methodInfo : classInfo.getListConstructorInfos()) {
                writer.emitEmptyLine();
                createMethodWrapper(writer, methodInfo, targetClassName, classInfo.getListSuperClassNames());
            }

            writeCodeDecoration(writer, "Direct access to fields");
            for (FieldInfo fieldInfo : classInfo.getListFieldInfos()) {
                createDirectGetter(writer, fieldInfo, classInfo.getListSuperClassNames());
                writer.emitEmptyLine();
                createDirectSetter(writer, fieldInfo, classInfo.getListSuperClassNames());
            }

            writeCodeDecoration(writer, "Access to methods");
            for (MethodInfo methodInfo : classInfo.getListMethodInfos()) {
                writer.emitEmptyLine();
                createMethodWrapper(writer, methodInfo, targetClassName, classInfo.getListSuperClassNames());
            }

            writer.endType();
            writer.close();
        } finally {
            if (out != null) {
                out.close();
            }
        }
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
        Set<Modifier> modifiers = newHashSet(Modifier.PUBLIC);
        if (fieldInfo.isStaticField()) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginMethod("void", setterName, modifiers, fieldType, fieldName);
        writer.beginControlFlow("try");
        String superClassChain = getSuperClassChain(fieldInfo);
        if (fieldInfo.isStaticField()) {
            writer.emitStatement("Field field = boundClass" + superClassChain + ".getDeclaredField(%s)",
                    JavaWriter.stringLiteral(fieldName));
        } else {
            writer.emitStatement("Field field = boundObject.getClass()" + superClassChain + ".getDeclaredField(%s)",
                    JavaWriter.stringLiteral(fieldName));
        }
        writer.emitStatement("field.setAccessible(true)");
        if (fieldInfo.isStaticField()) {
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
        Set<Modifier> modifiers = newHashSet(Modifier.PUBLIC);
        if (fieldInfo.isStaticField()) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginMethod(fieldType, getterName, modifiers);
        writer.beginControlFlow("try");
        String superClassChain = getSuperClassChain(fieldInfo);
        if (fieldInfo.isStaticField()) {
            writer.emitStatement("Field field = boundClass" + superClassChain + ".getDeclaredField(%s)",
                    JavaWriter.stringLiteral(fieldName));
        } else {
            writer.emitStatement("Field field = boundObject.getClass()" + superClassChain + ".getDeclaredField(%s)",
                    JavaWriter.stringLiteral(fieldName));
        }
        writer.emitStatement("field.setAccessible(true)");
        if (fieldInfo.isStaticField()) {
            writer.emitStatement("return (%s) field.get(null)", fieldType);
        } else {
            writer.emitStatement("return (%s) field.get(boundObject)", fieldType);
        }
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, Exception.class);
        writer.endMethod();
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

    private void createMethodWrapper(JavaWriter writer, MethodInfo methodInfo, String targetClassName,
            List<String> listSuperClassNames) throws IOException {
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

        Set<Modifier> modifiers = newHashSet(Modifier.PUBLIC);
        if (methodInfo.isStaticMethod() || methodInfo.isConstructor()) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginMethod(returnType, signature, modifiers, parameters, thrownTypesCommaSeparated);

        writer.beginControlFlow("try");

        // emit method retrieval
        String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(parameterTypeList);

        String superClassChain = getSuperClassChain(methodInfo);
        if (parameterTypeList.isEmpty()) {
            if (isConstructor || methodInfo.isInstanceInitializer() ) {
                writer.emitStatement("Constructor<? extends %s> method = boundClass.getDeclaredConstructor()", targetClassName);
            } else if (methodInfo.isStaticMethod()) {
                writer.emitStatement("Method method = boundClass" + superClassChain + ".getDeclaredMethod(%s)",
                        JavaWriter.stringLiteral(methodName));
            } else {
                writer.emitStatement("Method method = boundObject.getClass()" + superClassChain + ".getDeclaredMethod(%s)",
                        JavaWriter.stringLiteral(methodName));
            }
        } else {
            if (isConstructor) {
                writer.emitStatement("Constructor<? extends %s> method = boundClass.getDeclaredConstructor(%s)", targetClassName,
                        parametersTypesCommaSeparated);
            } else if (methodInfo.isStaticMethod()) {
                writer.emitStatement("Method method = boundClass" + superClassChain + ".getDeclaredMethod(%s,%s)",
                        JavaWriter.stringLiteral(methodName), parametersTypesCommaSeparated);
            } else {
                writer.emitStatement("Method method = boundObject.getClass()" + superClassChain + ".getDeclaredMethod(%s,%s)",
                        JavaWriter.stringLiteral(methodName), parametersTypesCommaSeparated);
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

    private String getSuperClassChain(Inheritable inheritable) {
        String superClassChain = "";
        for (int inheritanceLevel = 0; inheritanceLevel < inheritable.getInheritanceLevel(); inheritanceLevel++) {
            superClassChain += ".getSuperclass()";
        }
        return superClassChain;
    }

    private void addReflectionExceptionCatchClause(JavaWriter writer, Class<? extends Exception> exceptionClass)
            throws IOException {
        writer.beginControlFlow("catch( " + exceptionClass.getSimpleName() + " e )");
        writer.emitStatement("throw new BoundBoxException(e)");
        writer.endControlFlow();
    }

    //
    private String createCastReturnTypeString(String returnType) {
        String castReturnTypeString = "";
        if ("int".equals(returnType)) {
            castReturnTypeString = "(Integer)";
        } else

            if ("long".equals(returnType)) {
                castReturnTypeString = "(Long)";
            } else

                if ("byte".equals(returnType)) {
                    castReturnTypeString = "(Byte)";
                } else

                    if ("short".equals(returnType)) {
                        castReturnTypeString = "(Short)";
                    } else

                        if ("boolean".equals(returnType)) {
                            castReturnTypeString = "(Boolean)";
                        } else

                            if ("double".equals(returnType)) {
                                castReturnTypeString = "(Double)";
                            } else

                                if ("float".equals(returnType)) {
                                    castReturnTypeString = "(Float)";
                                } else

                                    if ("char".equals(returnType)) {
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
            listParameters.add(fieldInfo.getFieldTypeName() + ".class");
        }
        return StringUtils.join(listParameters, ", ");
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

    // from http://stackoverflow.com/q/2041778/693752
    public static <T> Set<T> newHashSet(T... objs) {
        Set<T> set = new HashSet<T>();
        for (T o : objs) {
            set.add(o);
        }
        return set;
    }

}
