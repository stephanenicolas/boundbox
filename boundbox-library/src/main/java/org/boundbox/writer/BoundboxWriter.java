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
        String boundBoxClassName = classInfo.getBoundBoxClassName();

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

        // TODO process inner classes
        writeCodeDecoration(writer, "Access to boundboxes of inner classes");
        for (InnerClassInfo innerClassInfo : classInfo.getListInnerClassInfo()) {
            writer.emitEmptyLine();
            String targetInnerClassName = innerClassInfo.getClassName();
            //TODO write javadoc generation method for inner classes.
            //writeJavadocForBoundMethod(writer, classInfo, methodInfo);
            createInnerClassWrapper(writer, innerClassInfo, targetInnerClassName, "BoundBox_inner_"+targetInnerClassName);
        }
        
        writer.endType();
    }
    
    private void createInnerClassWrapper(JavaWriter writer, InnerClassInfo innerClassInfo, String targetClassName, String boundBoxClassName) throws IOException {
        EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.FINAL);
        if( innerClassInfo.isStaticField() ) {
            modifiers.add(Modifier.STATIC);
        }
        writer.beginType(boundBoxClassName, "class", modifiers, null)
        //
        .emitEmptyLine()
        //
        .emitField(Object.class.getName(), "boundObject", EnumSet.of(Modifier.PRIVATE))
        //
        .emitField("Class<?>", "boundClass", EnumSet.of(Modifier.PRIVATE))//
        .emitEmptyLine();//
        
        writeJavadocForBoundBoxConstructor(writer, innerClassInfo);
        writer.beginMethod(null, boundBoxClassName, EnumSet.of(Modifier.PUBLIC), Object.class.getName(), "boundObject", "Class<?>", "boundClass")//
        .emitStatement("this.boundObject = boundObject")//
        .emitStatement("this.boundClass = boundClass")//
        .endMethod()//
        .emitEmptyLine();

        writeCodeDecoration(writer, "Access to constructors");
        for (MethodInfo methodInfo : innerClassInfo.getListConstructorInfos()) {
            writer.emitEmptyLine();
            writeJavadocForBoundConstructor(writer, innerClassInfo, methodInfo);
            createMethodWrapper(writer, methodInfo, targetClassName, innerClassInfo.getListSuperClassNames());
        }

        writeCodeDecoration(writer, "Direct access to fields");
        for (FieldInfo fieldInfo : innerClassInfo.getListFieldInfos()) {
            writeJavadocForBoundGetter(writer, fieldInfo, innerClassInfo);
            createDirectGetter(writer, fieldInfo, innerClassInfo.getListSuperClassNames());
            writer.emitEmptyLine();
            writeJavadocForBoundSetter(writer, fieldInfo, innerClassInfo);
            createDirectSetter(writer, fieldInfo, innerClassInfo.getListSuperClassNames());
        }

        writeCodeDecoration(writer, "Access to methods");
        for (MethodInfo methodInfo : innerClassInfo.getListMethodInfos()) {
            writer.emitEmptyLine();
            writeJavadocForBoundMethod(writer, innerClassInfo, methodInfo);
            createMethodWrapper(writer, methodInfo, targetClassName, innerClassInfo.getListSuperClassNames());
        }

        // TODO process inner classes
        writeCodeDecoration(writer, "Access to boundboxes of inner classes");
        for (InnerClassInfo innerInnerClassInfo : innerClassInfo.getListInnerClassInfo()) {
            writer.emitEmptyLine();
            String targetInnerClassName = innerInnerClassInfo.getClassName();
            //TODO write javadoc generation method for inner classes.
            //writeJavadocForBoundMethod(writer, classInfo, methodInfo);
            createInnerClassWrapper(writer, innerInnerClassInfo, targetInnerClassName, "BoundBox_inner_"+targetInnerClassName);
        }
        
        writer.endType();
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

        String superClassChain = getSuperClassChain(fieldInfo, listSuperClassNames);
        writer.beginMethod("void", setterName, modifiers, fieldType, fieldName);
        writer.beginControlFlow("try");
        writer.emitStatement("Field field = " + superClassChain + ".getDeclaredField(%s)", JavaWriter.stringLiteral(fieldName));
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
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        if (fieldInfo.isStaticField()) {
            modifiers.add(Modifier.STATIC);
        }
        
        writer.beginMethod(fieldType, getterName, modifiers);
        writer.beginControlFlow("try");
        String superClassChain = getSuperClassChain(fieldInfo, listSuperClassNames);
        writer.emitStatement("Field field = " + superClassChain + ".getDeclaredField(%s)", JavaWriter.stringLiteral(fieldName));
        writer.emitStatement("field.setAccessible(true)");
        String castReturnType = createCastReturnTypeString(fieldType);

        if (fieldInfo.isStaticField()) {
            writer.emitStatement("return %s field.get(null)", castReturnType);
        } else {
            writer.emitStatement("return %s field.get(boundObject)", castReturnType);
        }
        writer.endControlFlow();
        addReflectionExceptionCatchClause(writer, Exception.class);
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

        String superClassChain = getSuperClassChain(methodInfo, listSuperClassNames);
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

    private void writeJavadocForBoundBoxClass(JavaWriter writer, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            String className = classInfo.getClassName();
            String javadoc = "BoundBox for the class {@link %s}.";
            javadoc += " \nThis class will let you access all fields, constructors or methods of %s.";
            javadoc += "\n@see <a href='https://github.com/stephanenicolas/boundbox/wiki'>BoundBox's wiki on GitHub</a>";
            javadoc += "\n@see %s";
            writer.emitJavadoc(javadoc, className, className, className);
        }
    }
    
    private void writeJavadocForBoundBoxConstructor(JavaWriter writer, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            String className = classInfo.getClassName();
            String javadoc = "Creates a BoundBoxOf%s.";
            javadoc += "\n@param %s the instance of {@link %s} that is bound by this BoundBox.";
            writer.emitJavadoc(javadoc, StringUtils.substringAfterLast(className,"."), "boundObject",className);
        }
    }
    
    private void writeJavadocForBoundConstructor(JavaWriter writer, ClassInfo classInfo, MethodInfo methodInfo) throws IOException {
        if (isWritingJavadoc) {
            String className = classInfo.getClassName();
            String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(methodInfo.getParameterTypes());
            String javadoc = "Invokes a constructor of the class {@link %s}.";
            javadoc += " \nThis constructor that will be invoked is the constructor with the exact same signature as this method.";
            javadoc += "\n@see %s#%s(%s)";
            writer.emitJavadoc(javadoc, className, className,className, parametersTypesCommaSeparated);
        }
    }

    private void writeJavadocForBoundSetter(JavaWriter writer, FieldInfo fieldInfo, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            String fieldName = fieldInfo.getFieldName();
            List<String> listSuperClassNames = classInfo.getListSuperClassNames();
            String className = listSuperClassNames.get(fieldInfo.getInheritanceLevel());
            String javadoc = "Sets directly the value of %s.";
            javadoc += " \nThis method doesn't invoke the setter but changes the value of the field directly.";
            javadoc += "\n@param %s the new value of the field \"%s\" declared in class {@link %s}.";
            javadoc += "\n@see %s#%s";
            writer.emitJavadoc(javadoc, fieldName, fieldName, fieldName, className, className, fieldName);
        }
    }

    private void writeJavadocForBoundGetter(JavaWriter writer, FieldInfo fieldInfo, ClassInfo classInfo) throws IOException {
        if (isWritingJavadoc) {
            String fieldName = fieldInfo.getFieldName();
            List<String> listSuperClassNames = classInfo.getListSuperClassNames();
            String className = listSuperClassNames.get(fieldInfo.getInheritanceLevel());
            String javadoc = "Returns directly the value of %s.";
            javadoc += " \nThis method doesn't invoke the getter but returns the value of the field directly.";
            javadoc += "\n@return the value of the field \"%s\" declared in class {@link %s}.";
            javadoc += "\n@see %s#%s";
            writer.emitJavadoc(javadoc, fieldName, fieldName, className, className, fieldName);
        }
    }

    private void writeJavadocForBoundMethod(JavaWriter writer, ClassInfo classInfo, MethodInfo methodInfo) throws IOException {
        if (isWritingJavadoc) {
            String className = classInfo.getClassName();
            String parametersTypesCommaSeparated = createListOfParametersTypesCommaSeparated(methodInfo.getParameterTypes());
            String methodName = methodInfo.getMethodName();
            List<String> listSuperClassNames = classInfo.getListSuperClassNames();
            String javadoc = "Invokes the method \"%s\"of the class {@link %s}.";
            javadoc += " \nIn case of overloading, the method that will be invoked is the method with the exact same signature as this method.";
            javadoc += "\n@see %s#%s(%s)";
            writer.emitJavadoc(javadoc, methodName, className, listSuperClassNames.get(methodInfo.getInheritanceLevel()),methodName, parametersTypesCommaSeparated);
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

    private String getSuperClassChain(Inheritable inheritable, List<String> listSuperClassNames) {
        return listSuperClassNames.get(inheritable.getInheritanceLevel()) + ".class";
    }

    private void addReflectionExceptionCatchClause(JavaWriter writer, Class<? extends Exception> exceptionClass) throws IOException {
        writer.beginControlFlow("catch( " + exceptionClass.getSimpleName() + " e )");
        writer.emitStatement("throw new BoundBoxException(e)");
        writer.endControlFlow();
    }

    //
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
