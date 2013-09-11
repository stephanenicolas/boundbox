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


    //TODO : refactor as 
    //public void writeBoundBox(ClassInfo boundClassInfo, Writer writer) throws IOException {

    @Override
    public void writeBoundBox(ClassInfo classInfo, Writer out) throws IOException {
        String boundClassName = classInfo.getClassName();
        System.out.println( "BoundClassName is "+boundClassName );

        String targetPackageName = classInfo.getTargetPackageName();
        String targetClassName = classInfo.getTargetClassName();
        String boundBoxClassName = classInfo.getBoundBoxClassName();

        try {
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
            for( MethodInfo methodInfo : classInfo.getListConstructorInfos()) {
                writer.emitEmptyLine();
                createMethodWrapper(writer, methodInfo, targetClassName);
            }

            writeCodeDecoration(writer,"Direct access to fields");
            for( FieldInfo fieldInfo: classInfo.getListFieldInfos()) {
                createDirectGetter(writer, fieldInfo, classInfo.getListSuperClassNames());
                writer.emitEmptyLine();
                createDirectSetter(writer, fieldInfo, classInfo.getListSuperClassNames());
            }

            writeCodeDecoration(writer,"Access to methods");
            for( MethodInfo methodInfo : classInfo.getListMethodInfos()) {
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

    private void createDirectSetter(JavaWriter writer, FieldInfo fieldInfo, List<String> listSuperClassNames) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldTypeName();

        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String setterName = createSignatureSetterName(fieldInfo, listSuperClassNames, fieldNameCamelCase);
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

    private void createDirectGetter(JavaWriter writer, FieldInfo fieldInfo, List<String> listSuperClassNames) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getFieldTypeName();

        String fieldNameCamelCase = computeCamelCaseNameStartUpperCase(fieldName);
        String getterName = createSignatureGetterName(fieldInfo, listSuperClassNames, fieldNameCamelCase);
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

    private String createSignatureGetterName(FieldInfo fieldInfo, List<String> listSuperClassNames, String fieldNameCamelCase) {
        String getterName;
        if( fieldInfo.getEffectiveInheritanceLevel() == 0 ) {
            getterName = "boundBox_get" + fieldNameCamelCase;
        } else {
            String superClassName = listSuperClassNames.get(fieldInfo.getEffectiveInheritanceLevel());
            getterName = "boundBox_super_"+superClassName+"_get"+fieldNameCamelCase;
        }
        return getterName;
    }

    private String createSignatureSetterName(FieldInfo fieldInfo, List<String> listSuperClassNames, String fieldNameCamelCase) {
        String getterName;
        if( fieldInfo.getEffectiveInheritanceLevel() == 0 ) {
            getterName = "boundBox_set" + fieldNameCamelCase;
        } else {
            String superClassName = listSuperClassNames.get(fieldInfo.getEffectiveInheritanceLevel());
            getterName = "boundBox_super_"+superClassName+"_set"+fieldNameCamelCase;
        }
        return getterName;
    }

    private void createMethodWrapper(JavaWriter writer, MethodInfo methodInfo, String targetClassName) throws IOException {
        String methodName = methodInfo.getMethodName();
        String returnType = methodInfo.getReturnTypeName();
        List<FieldInfo> parameterTypeList = methodInfo.getParameterTypes();

        List<String> parameters = createListOfParameterTypesAndNames(parameterTypeList);
        List<String> thrownTypesCommaSeparated = methodInfo.getThrownTypeNames();

        //beginBoundInvocationMethod
        boolean isConstructor = methodInfo.isConstructor();
        if( isConstructor ) {
            methodName = "boundBox_new";
            returnType = targetClassName;
        }

        writer.beginMethod(returnType, methodName, newHashSet(Modifier.PUBLIC), parameters, thrownTypesCommaSeparated);


        writer.beginControlFlow("try");

        //emit method retrieval
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

        //emit method invocation
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

    private void addReflectionExceptionCatchClause(JavaWriter writer, Class<? extends Exception> exceptionClass) throws IOException {
        writer.beginControlFlow("catch( "+exceptionClass.getSimpleName() +" e )");
        writer.emitStatement("throw new BoundBoxException(e)");
        writer.endControlFlow();
    }

    //TODO find appropriate wrapper class by code ?
    private String createCastReturnTypeString(String returnType) {
        String castReturnTypeString = "";
        if( "int".equals(returnType) ) {
            castReturnTypeString = "(Integer)";
        }

        if( "long".equals(returnType) ) {
            castReturnTypeString = "(Long)";
        }

        if( "byte".equals(returnType) ) {
            castReturnTypeString = "(Byte)";
        }

        if( "boolean".equals(returnType) ) {
            castReturnTypeString = "(Boolean)";
        }

        if( "double".equals(returnType) ) {
            castReturnTypeString = "(Double)";
        }

        if( "float".equals(returnType) ) {
            castReturnTypeString = "(Float)";
        }

        if( "char".equals(returnType) ) {
            castReturnTypeString = "(Character)";
        }

        if( !castReturnTypeString.isEmpty() ) {
            castReturnTypeString +=" ";
        }
        return castReturnTypeString;
    }

    private String createListOfParametersTypesCommaSeparated(List<FieldInfo> parameterTypeList) {
        List<String> listParameters = new ArrayList<String>();
        for (FieldInfo fieldInfo : parameterTypeList) {
            listParameters.add(fieldInfo.getFieldTypeName()+".class");
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

    //from http://stackoverflow.com/q/2041778/693752
    public static <T> Set<T> newHashSet(T... objs) {
        Set<T> set = new HashSet<T>();
        for (T o : objs) {
            set.add(o);
        }
        return set;
    }

}
