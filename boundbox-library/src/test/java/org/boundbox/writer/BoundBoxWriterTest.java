package org.boundbox.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.boundbox.FakeFieldInfo;
import org.boundbox.FakeInnerClassInfo;
import org.boundbox.FakeMethodInfo;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//https://today.java.net/pub/a/today/2008/04/10/source-code-analysis-using-java-6-compiler-apis.html#invoking-the-compiler-from-code-the-java-compiler-api
//http://stackoverflow.com/a/7989365/693752
//TODO compile in memory ? Not sure, it's cool to debug
public class BoundBoxWriterTest {

    private BoundboxWriter writer;
    private File sandBoxDir;

    @Before
    public void setup() throws IOException {
        writer = new BoundboxWriter();
        sandBoxDir = new File("target/sandbox");
        if (sandBoxDir.exists()) {
            FileUtils.deleteDirectory(sandBoxDir);
        }
        sandBoxDir.mkdirs();
    }

    @After
    public void tearDown() throws IOException {
        if (sandBoxDir.exists()) {
            //FileUtils.deleteDirectory(sandBoxDir);
        }
    }
    
    // ----------------------------------
    // JAVADOC
    // ----------------------------------
//    @Test
//    we need this pull request to be accepted : https://github.com/square/javawriter/pull/25/
//    public void testProcess_class_without_javadoc() throws Exception {
//        // given
//        String classUnderTestName = "TestClassWithNothing";
//
//        ClassInfo classInfo = new ClassInfo(classUnderTestName);
//        classInfo.setListFieldInfos(Collections.<FieldInfo>emptyList());
//        classInfo.setListConstructorInfos(Collections.<MethodInfo>emptyList());
//        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
//        classInfo.setListImports(new HashSet<String>());
//
//        JavaWriter javaWriter = EasyMock.createMockBuilder(JavaWriter.class).createMock();
//        javaWriter.emitJavadoc(EasyMock.anyString());
//        EasyMock.expectLastCall().times(0);
//        EasyMock.replay(javaWriter);
//        writer.setWritingJavadoc(false);
//        
//        // when
//        writer.writeBoundBox(classInfo, javaWriter);
//
//        // then
//        EasyMock.verify(javaWriter);
//    }

    // ----------------------------------
    // FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_field() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleField";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("boundBox_getFoo");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setFoo", String.class);
        assertNotNull(method2);
    }
    
    @Test
    public void testProcess_class_with_many_fields() throws Exception {
        // given
        String classUnderTestName = "TestClassWithManyFields";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("a", "int");
        FakeFieldInfo fakeFieldInfo3 = new FakeFieldInfo("array1", "double[]");
        FakeFieldInfo fakeFieldInfo4 = new FakeFieldInfo("array2", "float[][]");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        listFieldInfos.add(fakeFieldInfo2);
        listFieldInfos.add(fakeFieldInfo3);
        listFieldInfos.add(fakeFieldInfo4);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("boundBox_getFoo");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setFoo", String.class);
        assertNotNull(method2);
    }

    // ----------------------------------
    // EXTRA FIELDS
    // ----------------------------------

    @Test
    public void testProcess_class_with_single_extra_field() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleExtraField";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListConstructorInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("boundBox_getFoo");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setFoo", String.class);
        assertNotNull(method2);
    }
    
    @Test
    public void testProcess_class_with_many_extra_fields() throws Exception {
        // given
        String classUnderTestName = "TestClassWithManyExtraFields";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("a", "int");
        FakeFieldInfo fakeFieldInfo3 = new FakeFieldInfo("array1", "double[]");
        FakeFieldInfo fakeFieldInfo4 = new FakeFieldInfo("array2", "float[][]");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        listFieldInfos.add(fakeFieldInfo2);
        listFieldInfos.add(fakeFieldInfo3);
        listFieldInfos.add(fakeFieldInfo4);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListConstructorInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("boundBox_getFoo");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setFoo", String.class);
        assertNotNull(method2);
    }
    
    // ----------------------------------
    // STATIC FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_static_field() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticField";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        fakeFieldInfo.setStaticField(true);
        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("a", "int");
        fakeFieldInfo2.setStaticField(true);
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        listFieldInfos.add(fakeFieldInfo2);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("boundBox_getFoo");
        assertNotNull(method);
        assertTrue((method.getModifiers() & Modifier.STATIC) != 0);
        Method method2 = clazz.getDeclaredMethod("boundBox_setFoo", String.class);
        assertNotNull(method2);
        assertTrue((method2.getModifiers() & Modifier.STATIC) != 0);
        
        Method method3 = clazz.getDeclaredMethod("boundBox_getA");
        assertNotNull(method3);
        assertTrue((method3.getModifiers() & Modifier.STATIC) != 0);
        Method method4 = clazz.getDeclaredMethod("boundBox_setA", int.class);
        assertNotNull(method4);
        assertTrue((method4.getModifiers() & Modifier.STATIC) != 0);

    }

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_constructor() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleConstructor";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();
        listConstructorInfos.add(fakeMethodInfo);
        classInfo.setListConstructorInfos(listConstructorInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("boundBox_new");
        assertNotNull(method);
        assertTrue((method.getModifiers() & Modifier.STATIC) != 0);

    }

    @Test
    public void testProcess_class_with_many_constructors() throws Exception {
        // given
        String classUnderTestName = "TestClassWithManyConstructors";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();

        listConstructorInfos.add(new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null));
        FakeFieldInfo fieldInfo = new FakeFieldInfo("a", "int");
        listConstructorInfos.add(new FakeMethodInfo("<init>", "void", Arrays.<FieldInfo>asList(fieldInfo), null));
        FakeFieldInfo fieldInfo2 = new FakeFieldInfo("a", Object.class.getName());
        listConstructorInfos.add(new FakeMethodInfo("<init>", "void", Arrays.<FieldInfo>asList(fieldInfo2), null));
        FakeFieldInfo fieldInfo3 = new FakeFieldInfo("b", Object.class.getName());
        listConstructorInfos.add(new FakeMethodInfo("<init>", "void", Arrays.<FieldInfo>asList(fieldInfo, fieldInfo3), null));
        FakeFieldInfo fieldInfo4 = new FakeFieldInfo("c", Object.class.getName());
        listConstructorInfos.add(new FakeMethodInfo("<init>", "void",
                Arrays.<FieldInfo>asList(fieldInfo, fieldInfo3, fieldInfo4), Arrays.asList("java.io.IOException",
                        "java.lang.RuntimeException")));

        classInfo.setListConstructorInfos(listConstructorInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method declaredMethod1 = clazz.getDeclaredMethod("boundBox_new");
        assertNotNull(declaredMethod1);
        assertTrue((declaredMethod1.getModifiers() & Modifier.STATIC) != 0);

        Method declaredMethod2 = clazz.getDeclaredMethod("boundBox_new", int.class);
        assertNotNull(declaredMethod2);
        assertTrue((declaredMethod2.getModifiers() & Modifier.STATIC) != 0);

        Method declaredMethod3 = clazz.getDeclaredMethod("boundBox_new", Object.class);
        assertNotNull(declaredMethod3);
        assertTrue((declaredMethod3.getModifiers() & Modifier.STATIC) != 0);

        Method declaredMethod4 = clazz.getDeclaredMethod("boundBox_new", int.class, Object.class);
        assertNotNull(declaredMethod4);
        assertTrue((declaredMethod4.getModifiers() & Modifier.STATIC) != 0);

        Method declaredMethod5 = clazz.getDeclaredMethod("boundBox_new", int.class, Object.class, Object.class);
        assertNotNull(declaredMethod5);
        assertTrue((declaredMethod5.getModifiers() & Modifier.STATIC) != 0);

        boolean containsIOException = false;
        boolean containsRuntimeException = false;
        for (Class<?> exceptionClass : declaredMethod5.getExceptionTypes()) {
            if (exceptionClass.equals(IOException.class)) {
                containsIOException = true;
            }
            if (exceptionClass.equals(RuntimeException.class)) {
                containsRuntimeException = true;
            }
        }
        assertTrue(containsIOException);
        assertTrue(containsRuntimeException);
    }

    // ----------------------------------
    // METHODS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_method() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleMethod";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(fakeMethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("foo");
        assertNotNull(method);
    }

    @Test
    public void testProcess_class_with_many_methods() throws Exception {
        // given
        String classUnderTestName = "TestClassWithManyMethods";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();

        listConstructorInfos.add(new FakeMethodInfo("simple", "void", new ArrayList<FieldInfo>(), null));
        FakeFieldInfo fieldInfo = new FakeFieldInfo("a", "int");
        listConstructorInfos.add(new FakeMethodInfo("withPrimitiveArgument", "void", Arrays.<FieldInfo>asList(fieldInfo), null));
        FakeFieldInfo fieldInfo2 = new FakeFieldInfo("a", Object.class.getName());
        listConstructorInfos.add(new FakeMethodInfo("withObjectArgument", "void", Arrays.<FieldInfo>asList(fieldInfo2), null));
        FakeFieldInfo fieldInfo3 = new FakeFieldInfo("b", Object.class.getName());
        listConstructorInfos.add(new FakeMethodInfo("withManyArguments", "void", Arrays.<FieldInfo>asList(fieldInfo, fieldInfo3),
                null));
        listConstructorInfos.add(new FakeMethodInfo("withPrimitiveIntReturnType", "int", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new FakeMethodInfo("withPrimitiveDoubleReturnType", "double", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos
        .add(new FakeMethodInfo("withPrimitiveBooleanReturnType", "boolean", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new FakeMethodInfo("withSingleThrownType", "void", Arrays.<FieldInfo>asList(), Arrays
                .asList("java.io.IOException")));
        listConstructorInfos.add(new FakeMethodInfo("withManyThrownType", "void", Arrays.<FieldInfo>asList(), Arrays.asList(
                "java.io.IOException", "java.lang.RuntimeException")));

        classInfo.setListConstructorInfos(listConstructorInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        assertNotNull(clazz.getDeclaredMethod("simple"));
        assertNotNull(clazz.getDeclaredMethod("withPrimitiveArgument", int.class));
        assertNotNull(clazz.getDeclaredMethod("withObjectArgument", Object.class));
        assertNotNull(clazz.getDeclaredMethod("withManyArguments", int.class, Object.class));
        Method declaredMethod = clazz.getDeclaredMethod("withPrimitiveIntReturnType");
        assertNotNull(declaredMethod);
        assertTrue(int.class.equals(declaredMethod.getReturnType()));
        Method declaredMethod2 = clazz.getDeclaredMethod("withPrimitiveDoubleReturnType");
        assertNotNull(declaredMethod2);
        assertTrue(double.class.equals(declaredMethod2.getReturnType()));
        Method declaredMethod3 = clazz.getDeclaredMethod("withPrimitiveBooleanReturnType");
        assertNotNull(declaredMethod3);
        assertTrue(boolean.class.equals(declaredMethod3.getReturnType()));
        Method declaredMethod4 = clazz.getDeclaredMethod("withSingleThrownType");
        assertNotNull(declaredMethod4);
        boolean containsIOException = false;
        for (Class<?> exceptionClass : declaredMethod4.getExceptionTypes()) {
            if (exceptionClass.equals(IOException.class)) {
                containsIOException = true;
            }
        }
        assertTrue(containsIOException);
        Method declaredMethod5 = clazz.getDeclaredMethod("withManyThrownType");
        assertNotNull(declaredMethod5);
        containsIOException = false;
        boolean containsRuntimeException = false;
        for (Class<?> exceptionClass : declaredMethod5.getExceptionTypes()) {
            if (exceptionClass.equals(IOException.class)) {
                containsIOException = true;
            }
            if (exceptionClass.equals(RuntimeException.class)) {
                containsRuntimeException = true;
            }
        }
        assertTrue(containsIOException);
        assertTrue(containsRuntimeException);

    }

    // ----------------------------------
    // STATIC METHODS
    // ----------------------------------
    @Test
    public void testProcess_class_with_static_method() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticMethod";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        fakeMethodInfo.setStaticMethod(true);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(fakeMethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("foo");
        assertNotNull(method);
        assertTrue((method.getModifiers() & Modifier.STATIC) != 0);
    }

    // ----------------------------------
    // INHERITANCE OF FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_inherited_field() throws Exception {
        // given
        String classUnderTestName = "TestClassWithInheritedField";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add("TestClassWithSingleField");

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        fakeFieldInfo.setInheritanceLevel(1);
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedField", "TestClassWithSingleField"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("boundBox_super_TestClassWithSingleField_getFoo");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_super_TestClassWithSingleField_setFoo", String.class);
        assertNotNull(method2);
    }

    @Test
    public void testProcess_class_with_inherited_and_hidingfield() throws Exception {
        // given
        String classUnderTestName = "TestClassWithInheritedAndHidingField";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add("TestClassWithSingleField");

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        fakeFieldInfo.setInheritanceLevel(0);
        listFieldInfos.add(fakeFieldInfo);
        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("foo", "java.lang.String");
        fakeFieldInfo2.setInheritanceLevel(1);
        listFieldInfos.add(fakeFieldInfo2);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedField", "TestClassWithSingleField"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        assertNotNull(clazz.getDeclaredMethod("boundBox_getFoo"));
        assertNotNull(clazz.getDeclaredMethod("boundBox_setFoo", String.class));
        assertNotNull(clazz.getDeclaredMethod("boundBox_super_TestClassWithSingleField_getFoo"));
        assertNotNull(clazz.getDeclaredMethod("boundBox_super_TestClassWithSingleField_setFoo", String.class));
    }

    // ----------------------------------
    // INHERITANCE OF METHODS
    // ----------------------------------
    @Test
    public void testProcess_class_with_inherited_method() throws Exception {
        // given
        String classUnderTestName = "TestClassWithInheritedMethod";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add("TestClassWithSingleMethod");

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(fakeMethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedMethod", "TestClassWithSingleMethod"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("foo");
        assertNotNull(method);

        Method method2 = null;
        try {
            method2 = clazz.getDeclaredMethod("boundbox_TestClassWithSingleMethod_foo");
            assertFalse(true);
        } catch (Exception ex) {
            assertNull(method2);
        }
    }

    @Test
    public void testProcess_class_with_overriding_method() throws Exception {
        // given
        String classUnderTestName = "TestClassWithOverridingMethod";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add("TestClassWithSingleMethod");

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        listMethodInfos.add(fakeMethodInfo);
        FakeMethodInfo fakeMethodInfo2 = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        fakeMethodInfo2.setInheritanceLevel(1);
        fakeMethodInfo2.setOverriden(true);
        listMethodInfos.add(fakeMethodInfo2);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedMethod", "TestClassWithSingleMethod"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("foo");
        assertNotNull(method);

        Method method2 = clazz.getDeclaredMethod("boundBox_super_TestClassWithSingleMethod_foo");
        assertNotNull(method2);
    }

    @Test
    public void testProcess_class_with_inherited_overriding_method() throws Exception {
        // given
        String classUnderTestName = "TestClassWithInheritedOverridingMethod";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add("TestClassWithOverridingMethod");
        neededClasses.add("TestClassWithSingleMethod");

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        listMethodInfos.add(fakeMethodInfo);
        FakeMethodInfo fakeMethodInfo2 = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        fakeMethodInfo2.setInheritanceLevel(2);
        listMethodInfos.add(fakeMethodInfo2);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedOverridingMethod", "TestClassWithInheritedMethod",
                "TestClassWithSingleMethod"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("foo");
        assertNotNull(method);

        Method method2 = clazz.getDeclaredMethod("boundBox_super_TestClassWithSingleMethod_foo");
        assertNotNull(method2);
    }

    // ----------------------------------
    // GENERICS
    // ----------------------------------

    // part of TDD for https://github.com/stephanenicolas/boundbox/issues/1
    // proposed by Flavien Laurent
    @Test
    public void testProcess_class_with_generics_parameters_have_raw_types() throws Exception {
        // given
        String classUnderTestName = "TestClassWithGenerics";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        ArrayList<FieldInfo> listParameters = new ArrayList<FieldInfo>();
        FieldInfo fakeParameterInfo = new FakeFieldInfo("strings", "java.util.List");
        listParameters.add(fakeParameterInfo );
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("doIt", "void", listParameters, null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(fakeMethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("doIt", List.class);
        assertNotNull(method);
    }

    // part of TDD for https://github.com/stephanenicolas/boundbox/issues/1
    // proposed by Flavien Laurent
    @Test
    public void testProcess_class_with_generics_parameters_have_generic_types() throws Exception {
        // given
        String classUnderTestName = "TestClassWithGenerics";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        ArrayList<FieldInfo> listParameters = new ArrayList<FieldInfo>();
        FieldInfo fakeParameterInfo = new FakeFieldInfo("strings", "java.util.List<String>");
        listParameters.add(fakeParameterInfo );
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("doIt", "void", listParameters, null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(fakeMethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("doIt", List.class);
        assertNotNull(method);
    }
    
    // ----------------------------------
    //  INNER CLASSES
    // ----------------------------------
    @Test
    public void testProcess_class_with_static_inner_class() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticInnerClass";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("a", "int");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(fakeMethodInfo);
        
        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(fakeInnerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        Method method = clazz.getDeclaredMethod("boundBox_getA");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setA", int.class);
        assertNotNull(method2);
        
        Method method3 = clazz.getDeclaredMethod("foo");
        assertNotNull(method3);
        
        Class<?> class1 = clazz.getDeclaredClasses()[0];
        assertNotNull(class1);
        
        assertEquals("BoundBox_inner_InnerClass",class1.getSimpleName());
    }
    
    // ----------------------------------
    //  INNER CLASSES
    // ----------------------------------
    @Test
    public void testProcess_class_with_static_inner_class_with_constructor() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticInnerClassWithConstructor";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        
        FakeMethodInfo fakeInnerClassConstructorInfo = new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listInnerClassConstructorInfos = new ArrayList<MethodInfo>();
        listInnerClassConstructorInfos.add(fakeInnerClassConstructorInfo);

        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        fakeInnerClassInfo.setListConstructorInfos(listInnerClassConstructorInfos);
        
        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(fakeInnerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        
        Class<?> innerClass = clazz.getDeclaredClasses()[0];
        assertNotNull(innerClass);
        
        assertEquals("BoundBox_inner_InnerClass",innerClass.getSimpleName());
        
        Method innerClassConstructor = clazz.getDeclaredMethod("boundBox_new_InnerClass");
        assertNotNull(innerClassConstructor);
    }
    
    @Test
    public void testProcess_class_with_static_inner_class_with_many_constructors() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticInnerClassWithConstructor";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        
        List<MethodInfo> listInnerClassConstructorInfos = new ArrayList<MethodInfo>();
        FakeMethodInfo fakeInnerClassConstructorInfo = new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        listInnerClassConstructorInfos.add(fakeInnerClassConstructorInfo);
        
        FieldInfo paramInt = new FakeFieldInfo("a", int.class.getName());
        FakeMethodInfo fakeInnerClassConstructorInfo2 = new FakeMethodInfo("<init>", "void", Arrays.asList(paramInt), null);
        listInnerClassConstructorInfos.add(fakeInnerClassConstructorInfo2);

        FieldInfo paramObject = new FakeFieldInfo("a", Object.class.getName());
        FakeMethodInfo fakeInnerClassConstructorInfo3 = new FakeMethodInfo("<init>", "void", Arrays.asList(paramObject), null);
        listInnerClassConstructorInfos.add(fakeInnerClassConstructorInfo3);

        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        fakeInnerClassInfo.setListConstructorInfos(listInnerClassConstructorInfos);

        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(fakeInnerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        
        Class<?> innerClass = clazz.getDeclaredClasses()[0];
        assertNotNull(innerClass);
        
        assertEquals("BoundBox_inner_InnerClass",innerClass.getSimpleName());
        
        Method innerClassConstructor = clazz.getDeclaredMethod("boundBox_new_InnerClass");
        assertNotNull(innerClassConstructor);
        
        Method innerClassConstructor2 = clazz.getDeclaredMethod("boundBox_new_InnerClass", int.class);
        assertNotNull(innerClassConstructor2);
        
        Method innerClassConstructor3 = clazz.getDeclaredMethod("boundBox_new_InnerClass", Object.class);
        assertNotNull(innerClassConstructor3);
    }
    
    @Test
    public void testProcess_class_with_static_inner_class_with_many_fields_and_methods() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticInnerClassWithManyFieldsAndMethods";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        
        List<MethodInfo> listInnerClassConstructorInfos = new ArrayList<MethodInfo>();
        FakeMethodInfo fakeInnerClassConstructorInfo = new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        listInnerClassConstructorInfos.add(fakeInnerClassConstructorInfo);
        
        List<FieldInfo> listInnerClassFieldInfos = new ArrayList<FieldInfo>();
        FieldInfo fakeInnerClassFieldInfo = new FakeFieldInfo("a", int.class.getName());
        listInnerClassFieldInfos.add(fakeInnerClassFieldInfo);

        FieldInfo fakeInnerClassFieldInfo2 = new FakeFieldInfo("b", Object.class.getName());
        listInnerClassFieldInfos.add(fakeInnerClassFieldInfo2);

        List<MethodInfo> listInnerClassMethodInfos = new ArrayList<MethodInfo>();
        FakeMethodInfo fakeInnerClassMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        listInnerClassMethodInfos.add(fakeInnerClassMethodInfo);

        FakeMethodInfo fakeInnerClassMethodInfo2 = new FakeMethodInfo("bar", "void", Arrays.asList(fakeInnerClassFieldInfo), null);
        listInnerClassMethodInfos.add(fakeInnerClassMethodInfo2);

        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        fakeInnerClassInfo.setListConstructorInfos(listInnerClassConstructorInfos);
        fakeInnerClassInfo.setListFieldInfos(listInnerClassFieldInfos);
        fakeInnerClassInfo.setListMethodInfos(listInnerClassMethodInfos);

        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(fakeInnerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(classInfo);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        CompilationTask task = createCompileTask(classInfo, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(classInfo);
        
        Class<?> innerClass = clazz.getDeclaredClasses()[0];
        assertNotNull(innerClass);
        
        assertEquals("BoundBox_inner_InnerClass",innerClass.getSimpleName());
        
        Method innerClassConstructor = clazz.getDeclaredMethod("boundBox_new_InnerClass");
        assertNotNull(innerClassConstructor);
        
        Method innerClassMethod = innerClass.getDeclaredMethod("foo");
        assertNotNull(innerClassMethod);
        
        Method innerClassMethod2 = innerClass.getDeclaredMethod("bar", int.class);
        assertNotNull(innerClassMethod2);
    }
    
    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private Class<?> loadBoundBoxClass(ClassInfo classInfo) throws ClassNotFoundException {
        return new CustomClassLoader().loadClass(classInfo.getBoundBoxClassName());
    }

    private CompilationTask createCompileTask(ClassInfo classInfo, List<String> neededClasses) throws URISyntaxException {
        String[] writtenSourceFileNames = new String[] { classNameToJavaFile(classInfo.getBoundBoxClassName()) };
        List<String> neededJavaFiles = new ArrayList<String>();
        for (String neededClass : neededClasses) {
            neededJavaFiles.add(classNameToJavaFile(neededClass));
        }
        String[] testSourceFileNames = neededJavaFiles.toArray(new String[0]);
        CompilationTask task = processAnnotations(writtenSourceFileNames, testSourceFileNames);
        return task;
    }

    private FileWriter createWriterInSandbox(ClassInfo classInfo) throws IOException {
        return new FileWriter(new File(sandBoxDir, classNameToJavaFile(classInfo.getBoundBoxClassName())));
    }

    private String classNameToJavaFile(String className) {
        return className.replaceAll("\\.", "/").concat(".java");
    }

    private CompilationTask processAnnotations(String[] writtenSourceFileNames, String[] testSourceFileNames)
            throws URISyntaxException {
        // Get an instance of java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // Get a new instance of the standard file manager implementation
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // Get the list of java file objects, in this case we have only
        // one file, TestClass.java
        // http://stackoverflow.com/a/676102/693752
        List<File> listSourceFiles = new ArrayList<File>();
        for (String sourceFileName : writtenSourceFileNames) {
            listSourceFiles.add(new File(sandBoxDir, sourceFileName));
        }

        for (String sourceFileName : testSourceFileNames) {
            listSourceFiles.add(new File(ClassLoader.getSystemResource(sourceFileName).toURI()));
        }
        Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(listSourceFiles);

        Iterable<String> options = Arrays.asList("-d", sandBoxDir.getAbsolutePath());

        // Create the compilation task
        CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits1);

        task.setProcessors(new LinkedList<Processor>());
        return task;
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------
    private final class CustomClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            File classFile = new File(sandBoxDir, name + ".class");
            if (!classFile.exists()) {
                return super.loadClass(name);
            } else {
                try {
                    byte[] bytes = FileUtils.readFileToByteArray(classFile);
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

    }

}
