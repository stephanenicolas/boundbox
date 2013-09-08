package org.boundbox;

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

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;
import org.boundbox.BoundBoxProcessor.BoundClassVisitor;

import com.squareup.javawriter.JavaWriter;

/**
 * TODO handle inheritance of bounded class
 * TODO handle imports inside boundbox class
 * TODO static methods
 * TODO static initializers
 * TODO handle inner classes as bounded class ?
 * @author SNI
 *
 */
public class BoundboxWriter implements IBoundboxWriter {

    private static final String CODE_DECORATOR_TITLE_PREFIX = "\t";
    private static final String CODE_DECORATOR = "******************************";

    /* (non-Javadoc)
     * @see org.boundbox.IBoundboxWriter#writeBoundBox(javax.lang.model.element.TypeElement, javax.annotation.processing.Filer, org.boundbox.BoundBoxProcessor.BoundClassVisitor)
     */
    @Override
    public void writeBoundBox(TypeElement boundClass, Filer filer, BoundClassVisitor boundClassVisitor) throws IOException {
        String boundClassName = boundClass.getQualifiedName().toString();
        System.out.println( "BoundClassName is "+boundClassName );

        String targetPackageName = boundClassName.substring(0, boundClassName.lastIndexOf('.'));
        String targetClassName = boundClassName.substring(boundClassName.lastIndexOf('.')+1);
        String boundBoxClassName = "BoundBoxOf"+targetClassName;

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
            .emitImports(InvocationTargetException.class.getName())//
            .emitImports(BoundBoxException.class.getName())//
            .emitEmptyLine();

            writer.beginType(boundBoxClassName, "class", newHashSet(Modifier.PUBLIC, Modifier.FINAL), null) //
            .emitEmptyLine()//
            .emitField(targetClassName, "boundObject", newHashSet(Modifier.PRIVATE))//
            .emitEmptyLine()//
            .beginMethod(null, boundBoxClassName, newHashSet(Modifier.PUBLIC), targetClassName, "boundObject")//
            .emitStatement("this.boundObject = boundObject")//
            .endMethod()//
            .emitEmptyLine();

            writeCodeDecoration(writer,"Access to constructors");
            for( MethodInfo methodInfo : boundClassVisitor.getListConstructorInfos()) {
                writer.emitEmptyLine();
                createMethodWrapper(writer, methodInfo, targetClassName);
            }

            writeCodeDecoration(writer,"Direct access to fields");
            for( FieldInfo fieldInfo: boundClassVisitor.getListFieldInfos()) {
                createDirectGetter(writer, fieldInfo);
                writer.emitEmptyLine();
                createDirectSetter(writer, fieldInfo);
            }

            writeCodeDecoration(writer,"Access to methods");
            for( MethodInfo methodInfo : boundClassVisitor.getListMethodInfos()) {
                writer.emitEmptyLine();
                createMethodWrapper(writer, methodInfo, targetClassName);
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
        writer.emitSingleLineComment(CODE_DECORATOR_TITLE_PREFIX+decorationTitle);
        writer.emitSingleLineComment(CODE_DECORATOR);
        writer.emitEmptyLine();
    }

    private void createDirectSetter(JavaWriter writer, FieldInfo fieldInfo) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldType().toString();

        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String setterName = "boundBox_set" + fieldNameCamelCase;
        writer.beginMethod("void", setterName, newHashSet(Modifier.PUBLIC), fieldType, fieldName);
        writer.beginControlFlow("try");
        String superClassChain = getSuperClassChain(fieldInfo);
        writer.emitStatement("Field field = boundObject.getClass()"+superClassChain+".getDeclaredField(%s)", JavaWriter.stringLiteral(fieldName));
        writer.emitStatement("field.setAccessible(true)");
        writer.emitStatement("field.set(boundObject, %s)", fieldName);
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, Exception.class);
        writer.endMethod();
    }

    private void createDirectGetter(JavaWriter writer, FieldInfo fieldInfo) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldType().toString();

        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String getterName = "boundBox_get" + fieldNameCamelCase;
        writer.beginMethod(fieldType, getterName, newHashSet(Modifier.PUBLIC));
        writer.beginControlFlow("try");
        String superClassChain = getSuperClassChain(fieldInfo);
        writer.emitStatement("Field field = boundObject.getClass()"+superClassChain+".getDeclaredField(%s)", JavaWriter.stringLiteral(fieldName));
        writer.emitStatement("field.setAccessible(true)");
        writer.emitStatement("return (%s) field.get(boundObject)", fieldType);
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, Exception.class);
        writer.endMethod();
    }

    private void createMethodWrapper(JavaWriter writer, MethodInfo methodInfo, String targetClassName) throws IOException {
        String methodName = methodInfo.getMethodName();
        String returnType = methodInfo.getReturnType().toString();
        List<FieldInfo> parameterTypeList = methodInfo.getParameterTypes();
        List<? extends TypeMirror> thrownTypeList = methodInfo.getThrownTypes();

        List<String> parameters = createListOfParameterTypesAndNames(parameterTypeList);
        List<String> thrownTypesCommaSeparated = createListOfThownTypes(thrownTypeList);

        boolean isConstructor = methodInfo.isConstructor();
        if( isConstructor ) {
            methodName = "boundBox_new";
            returnType = targetClassName;
        }

        writer.beginMethod(returnType, methodName, newHashSet(Modifier.PUBLIC), parameters, thrownTypesCommaSeparated);


        writer.beginControlFlow("try");
        String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(parameterTypeList);

        String superClassChain = getSuperClassChain(methodInfo);
        if( parameterTypeList.isEmpty() ) {
            if( isConstructor ) {
                writer.emitStatement("Constructor<? extends %s> method = boundObject.getClass().getDeclaredConstructor()", targetClassName);
            } else {
                writer.emitStatement("Method method = boundObject.getClass()"+superClassChain+".getDeclaredMethod(%s)", JavaWriter.stringLiteral(methodName));
            }
        } else {
            if( isConstructor ) {
                writer.emitStatement("Constructor<? extends %s> method = boundObject.getClass().getDeclaredConstructor(%s)", targetClassName,parametersTypesCommaSeparated);
            } else {
                writer.emitStatement("Method method = boundObject.getClass()"+superClassChain+".getDeclaredMethod(%s,%s)", JavaWriter.stringLiteral(methodName), parametersTypesCommaSeparated);
            }
        }
        writer.emitStatement("method.setAccessible(true)");

        String parametersNamesCommaSeparated = createListOfParametersNamesCommaSeparated(parameterTypeList);

        String returnString = "";
        if( methodInfo.isConstructor() || methodInfo.hasReturnType() ) {
            returnString = "return ";
            String castReturnTypeString = createCastReturnTypeString(returnType);
            returnString += castReturnTypeString;
        }

        if( parameterTypeList.isEmpty() ) {
            if( isConstructor ) {
                writer.emitStatement(returnString+"method.newInstance()");
            } else {
                writer.emitStatement(returnString+"method.invoke(boundObject)");
            }
        } else {
            if( isConstructor ) {
                writer.emitStatement(returnString+"method.newInstance(%s)", parametersNamesCommaSeparated);
            } else {
                writer.emitStatement(returnString+"method.invoke(boundObject, %s)", parametersNamesCommaSeparated);
            }
        }
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, IllegalAccessException.class);
        addReflectionExceptionCatchClause(writer, IllegalArgumentException.class);
        addReflectionExceptionCatchClause(writer, InvocationTargetException.class);
        addReflectionExceptionCatchClause(writer, NoSuchMethodException.class);
        if( methodInfo.isConstructor() ) {
            addReflectionExceptionCatchClause(writer, InstantiationException.class);
        }
        writer.endMethod();
    }


    private String getSuperClassChain(Inheritable inheritable) {
        String superClassChain = "";
        for( int inheritanceLevel = 0; inheritanceLevel< inheritable.getInheritanceLevel(); inheritanceLevel ++ ) {
            superClassChain += ".getSuperclass()";
        }
        return superClassChain;
    }

    private String getSuperChain(Inheritable inheritable) {
        String superClassChain = "";
        for( int inheritanceLevel = 0; inheritanceLevel< inheritable.getInheritanceLevel(); inheritanceLevel ++ ) {
            superClassChain += "_super()";
        }
        return superClassChain;
    }
    private void addReflectionExceptionCatchClause(JavaWriter writer, Class<? extends Exception> exceptionClass) throws IOException {
        writer.beginControlFlow("catch( "+exceptionClass.getSimpleName() +" e )");
        writer.emitStatement("throw new BoundBoxException(e)");
        writer.endControlFlow();
    }

    private String createCastReturnTypeString(String returnType) {
        String castReturnTypeString = "";
        if( "int".equals(returnType) ) {
            castReturnTypeString = "(Integer)";
        }

        if( "boolean".equals(returnType) ) {
            castReturnTypeString = "(Boolean)";
        }

        if( !castReturnTypeString.isEmpty() ) {
            castReturnTypeString +=" ";
        }
        return castReturnTypeString;
    }

    private String createListOfParametersTypesCommaSeparated(List<FieldInfo> parameterTypeList) {
        List<String> listParameters = new ArrayList<String>();
        for (FieldInfo fieldInfo : parameterTypeList) {
            listParameters.add(fieldInfo.getFieldType().toString()+".class");
        }
        return StringUtils.join(listParameters, ", ");
    }

    private List<String> createListOfThownTypes(List<? extends TypeMirror> thrownTypeList) {
        List<String> thrownTypes = new ArrayList<String>();
        for (TypeMirror typeMirror : thrownTypeList) {
            thrownTypes.add(typeMirror.toString());
        }
        return thrownTypes;
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
            listParameters.add(fieldInfo.getFieldType().toString());
            listParameters.add(fieldInfo.getFieldName());
        }
        return listParameters;
    }

    private String computeCamelCaseNameStartUpperCase(String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    //from http://stackoverflow.com/q/2041778/693752
    public static <T> Set<T> newHashSet(T... objs) {
        Set<T> set = new HashSet<T>();
        for (T o : objs) {
            set.add(o);
        }
        return set;
    }

}
