package org.boundbox;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import org.boundbox.BoundBoxProcessor.BoundClassVisitor;

import com.squareup.java.JavaWriter;

public class BoundboxWriter {

    private static final String CODE_DECORATOR_TITLE_PREFIX = "\t";
    private static final String CODE_DECORATOR = "******************************";

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
            .emitEmptyLine();

            writer.beginType(boundBoxClassName, "class", Modifier.PUBLIC | Modifier.FINAL, null) //
            .emitEmptyLine()//
            .emitField(targetClassName, "boundObject", Modifier.PRIVATE)//
            .emitEmptyLine()//
            .beginMethod(null, boundBoxClassName, Modifier.PUBLIC, targetClassName, "boundObject")//
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
        writer.emitEndOfLineComment(CODE_DECORATOR);
        writer.emitEndOfLineComment(CODE_DECORATOR_TITLE_PREFIX+decorationTitle);
        writer.emitEndOfLineComment(CODE_DECORATOR);
        writer.emitEmptyLine();
    }
    
    private void createDirectSetter(JavaWriter writer, FieldInfo fieldInfo) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getType().toString();

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

    private void createDirectGetter(JavaWriter writer, FieldInfo fieldInfo) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        String fieldType = fieldInfo.getType().toString();

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

    private void createMethodWrapper(JavaWriter writer, MethodInfo methodInfo, String targetClassName) throws IOException {
        String methodName = methodInfo.getMethodName();
        String returnType = methodInfo.getReturnType().toString();
        List<TypeMirror> parameterTypeList = methodInfo.getParameterTypes();
        List<? extends TypeMirror> thrownTypeList = methodInfo.getThrownTypes();

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
        boolean isConstructor = methodInfo.isConstructor();
        if( isConstructor ) {
            methodName = "boundBox_new";
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

}
