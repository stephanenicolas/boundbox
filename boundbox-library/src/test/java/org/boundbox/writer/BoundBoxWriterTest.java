package org.boundbox.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import lombok.Getter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//https://today.java.net/pub/a/today/2008/04/10/source-code-analysis-using-java-6-compiler-apis.html#invoking-the-compiler-from-code-the-java-compiler-api
//http://stackoverflow.com/a/7989365/693752
//TODO compile in memory ? Not sure, it's cool to debug
public class BoundBoxWriterTest {

    @Getter
    private BoundboxWriter writer;
    private File sandBoxDir;
    private FileWriter sandboxWriter;
    private DocumentationGenerator mockDocumentationGenerator;

    @Before
    public void setup() throws IOException {
        writer = new BoundboxWriter();
        sandBoxDir = new File("target/sandbox");
        if (sandBoxDir.exists()) {
            FileUtils.deleteDirectory(sandBoxDir);
        }
        sandBoxDir.mkdirs();

        mockDocumentationGenerator = EasyMock.createMock(DocumentationGenerator.class);
    }

    @After
    public void tearDown() throws IOException {
        if (sandBoxDir.exists()) {
            // FileUtils.deleteDirectory(sandBoxDir);
        }
        closeSandboxWriter();
    }

    private void closeSandboxWriter() throws IOException {
        if (sandboxWriter != null) {
            sandboxWriter.close();
            sandboxWriter = null;
        }
    }

    // ----------------------------------
    // JAVADOC
    // ----------------------------------
    // TODO this test tests the documentation generator, put these in its test class
    // TODO replace by a mock of documentation generator here
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testProcess_class_without_javadoc() throws Exception {
        // given
        String classUnderTestName = "TestClassWithNothing";

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        classInfo.setListFieldInfos(Collections.<FieldInfo>emptyList());
        classInfo.setListConstructorInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListImports(new HashSet<String>());

        final Capture<ClassInfo> captured = new Capture<ClassInfo>();
        EasyMock.expect(mockDocumentationGenerator.generateJavadocForBoundBoxClass(EasyMock.capture(captured))).andReturn(StringUtils.EMPTY);
        EasyMock.expectLastCall().andAnswer(new IAnswer() {
            public Object answer() {
                // used to debug the call
                System.out.println(captured.getValue());
                assertTrue(false);
                return null;
            }
        });
        final Capture<ClassInfo> captured2 = new Capture<ClassInfo>();
        EasyMock.expect(mockDocumentationGenerator.generateJavadocForBoundBoxConstructor(EasyMock.capture(captured2))).andReturn(StringUtils.EMPTY);
        EasyMock.expectLastCall().andAnswer(new IAnswer() {
            public Object answer() {
                // used to debug the call
                System.out.println(captured2.getValue());
                assertTrue(false);
                return null;
            }
        });

        EasyMock.replay(mockDocumentationGenerator);
        writer.setWritingJavadoc(false);

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.setJavadocGenerator(mockDocumentationGenerator);
        writer.writeBoundBox(classInfo, out);

        // then
        // tested by the capture.
    }

    @Test
    public void testProcess_class_with_javadoc() throws Exception {
        // given
        String classUnderTestName = "TestClassWithNothing";

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        classInfo.setListFieldInfos(Collections.<FieldInfo>emptyList());
        classInfo.setListConstructorInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListImports(new HashSet<String>());

        EasyMock.expect(mockDocumentationGenerator.generateJavadocForBoundBoxClass(EasyMock.anyObject(ClassInfo.class))).andReturn(StringUtils.EMPTY);
        EasyMock.expectLastCall().atLeastOnce();
        EasyMock.expect(mockDocumentationGenerator.generateJavadocForBoundBoxConstructor(EasyMock.anyObject(ClassInfo.class))).andReturn(StringUtils.EMPTY);
        EasyMock.expectLastCall().atLeastOnce();

        EasyMock.replay(mockDocumentationGenerator);

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));
        writer.setWritingJavadoc(true);

        // when
        writer.setJavadocGenerator(mockDocumentationGenerator);
        writer.writeBoundBox(classInfo, out);

        // then
        EasyMock.verify(mockDocumentationGenerator);
    }

    // ----------------------------------
    // FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_field() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleField";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo FieldInfo2 = new FieldInfo("a", "int");
        FieldInfo FieldInfo3 = new FieldInfo("array1", "double[]");
        FieldInfo FieldInfo4 = new FieldInfo("array2", "float[][]");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);
        listFieldInfos.add(FieldInfo2);
        listFieldInfos.add(FieldInfo3);
        listFieldInfos.add(FieldInfo4);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
        Method method = clazz.getDeclaredMethod("boundBox_getFoo");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setFoo", String.class);
        assertNotNull(method2);
    }

    // ----------------------------------
    // FINAL FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_final_field() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleFinalField";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo.setFinalField(true);
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
        Method method = clazz.getDeclaredMethod("boundBox_getFoo");
        assertNotNull(method);

        Method method2 = null;
        try {
            method2 = clazz.getDeclaredMethod("boundBox_setFoo", String.class);
            fail();
        } catch (Exception e) {
            assertNull(method2);
        }
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
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo.setStaticField(true);
        FieldInfo FieldInfo2 = new FieldInfo("a", "int");
        FieldInfo2.setStaticField(true);
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);
        listFieldInfos.add(FieldInfo2);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
    // STATIC INITIALIZER
    // ----------------------------------

    // those blocks are not accessible via reflection

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_constructor() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleConstructor";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        MethodInfo MethodInfo = new MethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();
        listConstructorInfos.add(MethodInfo);
        classInfo.setListConstructorInfos(listConstructorInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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

        listConstructorInfos.add(new MethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null));
        FieldInfo fieldInfo = new FieldInfo("a", "int");
        listConstructorInfos.add(new MethodInfo("<init>", "void", Arrays.<FieldInfo>asList(fieldInfo), null));
        FieldInfo fieldInfo2 = new FieldInfo("a", Object.class.getName());
        listConstructorInfos.add(new MethodInfo("<init>", "void", Arrays.<FieldInfo>asList(fieldInfo2), null));
        FieldInfo fieldInfo3 = new FieldInfo("b", Object.class.getName());
        listConstructorInfos.add(new MethodInfo("<init>", "void", Arrays.<FieldInfo>asList(fieldInfo, fieldInfo3), null));
        FieldInfo fieldInfo4 = new FieldInfo("c", Object.class.getName());
        listConstructorInfos.add(new MethodInfo("<init>", "void", Arrays.<FieldInfo>asList(fieldInfo, fieldInfo3, fieldInfo4), Arrays.asList("java.io.IOException", "java.lang.RuntimeException")));

        classInfo.setListConstructorInfos(listConstructorInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(MethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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

        listConstructorInfos.add(new MethodInfo("simple", "void", new ArrayList<FieldInfo>(), null));
        FieldInfo fieldInfo = new FieldInfo("a", "int");
        listConstructorInfos.add(new MethodInfo("withPrimitiveArgument", "void", Arrays.<FieldInfo>asList(fieldInfo), null));
        FieldInfo fieldInfo2 = new FieldInfo("a", Object.class.getName());
        listConstructorInfos.add(new MethodInfo("withObjectArgument", "void", Arrays.<FieldInfo>asList(fieldInfo2), null));
        FieldInfo fieldInfo3 = new FieldInfo("b", Object.class.getName());
        listConstructorInfos.add(new MethodInfo("withManyArguments", "void", Arrays.<FieldInfo>asList(fieldInfo, fieldInfo3), null));
        listConstructorInfos.add(new MethodInfo("withPrimitiveCharReturnType", "char", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new MethodInfo("withPrimitiveIntReturnType", "int", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new MethodInfo("withPrimitiveByteReturnType", "byte", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new MethodInfo("withPrimitiveShortReturnType", "short", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new MethodInfo("withPrimitiveLongReturnType", "long", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new MethodInfo("withPrimitiveDoubleReturnType", "double", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new MethodInfo("withPrimitiveFloatReturnType", "float", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new MethodInfo("withPrimitiveBooleanReturnType", "boolean", Arrays.<FieldInfo>asList(), null));
        listConstructorInfos.add(new MethodInfo("withSingleThrownType", "void", Arrays.<FieldInfo>asList(), Arrays.asList("java.io.IOException")));
        listConstructorInfos.add(new MethodInfo("withManyThrownType", "void", Arrays.<FieldInfo>asList(), Arrays.asList("java.io.IOException", "java.lang.RuntimeException")));

        classInfo.setListConstructorInfos(listConstructorInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
        assertNotNull(clazz.getDeclaredMethod("simple"));
        assertNotNull(clazz.getDeclaredMethod("withPrimitiveArgument", int.class));
        assertNotNull(clazz.getDeclaredMethod("withObjectArgument", Object.class));
        assertNotNull(clazz.getDeclaredMethod("withManyArguments", int.class, Object.class));
        Method declaredMethodChar = clazz.getDeclaredMethod("withPrimitiveCharReturnType");
        assertNotNull(declaredMethodChar);
        assertTrue(char.class.equals(declaredMethodChar.getReturnType()));
        Method declaredMethodInt = clazz.getDeclaredMethod("withPrimitiveIntReturnType");
        assertNotNull(declaredMethodInt);
        assertTrue(int.class.equals(declaredMethodInt.getReturnType()));
        Method declaredMethodLong = clazz.getDeclaredMethod("withPrimitiveLongReturnType");
        assertNotNull(declaredMethodLong);
        assertTrue(long.class.equals(declaredMethodLong.getReturnType()));
        Method declaredMethodByte = clazz.getDeclaredMethod("withPrimitiveByteReturnType");
        assertNotNull(declaredMethodByte);
        assertTrue(byte.class.equals(declaredMethodByte.getReturnType()));
        Method declaredMethodShort = clazz.getDeclaredMethod("withPrimitiveShortReturnType");
        assertNotNull(declaredMethodShort);
        assertTrue(short.class.equals(declaredMethodShort.getReturnType()));
        Method declaredMethodDouble = clazz.getDeclaredMethod("withPrimitiveDoubleReturnType");
        assertNotNull(declaredMethodDouble);
        assertTrue(double.class.equals(declaredMethodDouble.getReturnType()));
        Method declaredMethodFloat = clazz.getDeclaredMethod("withPrimitiveFloatReturnType");
        assertNotNull(declaredMethodFloat);
        assertTrue(float.class.equals(declaredMethodFloat.getReturnType()));
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
        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        MethodInfo.setStaticMethod(true);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(MethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo.setInheritanceLevel(1);
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedField", "TestClassWithSingleField"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo.setInheritanceLevel(0);
        listFieldInfos.add(FieldInfo);
        FieldInfo FieldInfo2 = new FieldInfo("foo", "java.lang.String");
        FieldInfo2.setInheritanceLevel(1);
        listFieldInfos.add(FieldInfo2);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedField", "TestClassWithSingleField"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(MethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedMethod", "TestClassWithSingleMethod"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        listMethodInfos.add(MethodInfo);
        MethodInfo MethodInfo2 = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        MethodInfo2.setInheritanceLevel(1);
        MethodInfo2.setOverriden(true);
        listMethodInfos.add(MethodInfo2);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedMethod", "TestClassWithSingleMethod"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        listMethodInfos.add(MethodInfo);
        MethodInfo MethodInfo2 = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        MethodInfo2.setInheritanceLevel(2);
        listMethodInfos.add(MethodInfo2);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListSuperClassNames(Arrays.asList("TestClassWithInheritedOverridingMethod", "TestClassWithInheritedMethod", "TestClassWithSingleMethod"));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        FieldInfo ParameterInfo = new FieldInfo("strings", "java.util.List");
        listParameters.add(ParameterInfo);
        MethodInfo MethodInfo = new MethodInfo("doIt", "void", listParameters, null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(MethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
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
        FieldInfo ParameterInfo = new FieldInfo("strings", "java.util.List<String>");
        listParameters.add(ParameterInfo);
        MethodInfo MethodInfo = new MethodInfo("doIt", "void", listParameters, null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(MethodInfo);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
        Method method = clazz.getDeclaredMethod("doIt", List.class);
        assertNotNull(method);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    @Test
    public void testProcess_class_with_static_inner_class() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticInnerClass";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);

        FieldInfo FieldInfo = new FieldInfo("a", "int");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);

        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(MethodInfo);

        InnerClassInfo innerClassInfo = new InnerClassInfo("InnerClass");
        innerClassInfo.setStaticInnerClass(true);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(innerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
        Method method = clazz.getDeclaredMethod("boundBox_getA");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setA", int.class);
        assertNotNull(method2);

        Method method3 = clazz.getDeclaredMethod("foo");
        assertNotNull(method3);

        Class<?> class1 = clazz.getDeclaredClasses()[0];
        assertNotNull(class1);

        assertEquals("BoundBoxOfInnerClass", class1.getSimpleName());
    }

    @Test
    public void testProcess_class_with_static_inner_class_with_constructor() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticInnerClassWithConstructor";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);

        MethodInfo InnerClassConstructorInfo = new MethodInfo("<init>", "Object", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listInnerClassConstructorInfos = new ArrayList<MethodInfo>();
        listInnerClassConstructorInfos.add(InnerClassConstructorInfo);

        InnerClassInfo innerClassInfo = new InnerClassInfo("InnerClass");
        innerClassInfo.setStaticInnerClass(true);
        innerClassInfo.setListConstructorInfos(listInnerClassConstructorInfos);

        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(innerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));

        Class<?> innerClass = clazz.getDeclaredClasses()[0];
        assertNotNull(innerClass);

        assertEquals("BoundBoxOfInnerClass", innerClass.getSimpleName());

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
        MethodInfo InnerClassConstructorInfo = new MethodInfo("<init>", "Object", new ArrayList<FieldInfo>(), null);
        listInnerClassConstructorInfos.add(InnerClassConstructorInfo);

        FieldInfo paramInt = new FieldInfo("a", int.class.getName());
        MethodInfo InnerClassConstructorInfo2 = new MethodInfo("<init>", "Object", Arrays.asList(paramInt), null);
        listInnerClassConstructorInfos.add(InnerClassConstructorInfo2);

        FieldInfo paramObject = new FieldInfo("a", Object.class.getName());
        MethodInfo InnerClassConstructorInfo3 = new MethodInfo("<init>", "Object", Arrays.asList(paramObject), null);
        listInnerClassConstructorInfos.add(InnerClassConstructorInfo3);

        InnerClassInfo innerClassInfo = new InnerClassInfo("InnerClass");
        innerClassInfo.setStaticInnerClass(true);
        innerClassInfo.setListConstructorInfos(listInnerClassConstructorInfos);

        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(innerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));

        Class<?> innerClass = clazz.getDeclaredClasses()[0];
        assertNotNull(innerClass);

        assertEquals("BoundBoxOfInnerClass", innerClass.getSimpleName());

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
        MethodInfo InnerClassConstructorInfo = new MethodInfo("<init>", "Object", new ArrayList<FieldInfo>(), null);
        listInnerClassConstructorInfos.add(InnerClassConstructorInfo);

        List<FieldInfo> listInnerClassFieldInfos = new ArrayList<FieldInfo>();
        FieldInfo InnerClassFieldInfo = new FieldInfo("a", int.class.getName());
        listInnerClassFieldInfos.add(InnerClassFieldInfo);

        FieldInfo InnerClassFieldInfo2 = new FieldInfo("b", Object.class.getName());
        InnerClassFieldInfo2.setFinalField(true);
        listInnerClassFieldInfos.add(InnerClassFieldInfo2);

        List<MethodInfo> listInnerClassMethodInfos = new ArrayList<MethodInfo>();
        MethodInfo InnerClassMethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        listInnerClassMethodInfos.add(InnerClassMethodInfo);

        MethodInfo InnerClassMethodInfo2 = new MethodInfo("bar", "void", Arrays.asList(InnerClassFieldInfo), null);
        listInnerClassMethodInfos.add(InnerClassMethodInfo2);

        InnerClassInfo innerClassInfo = new InnerClassInfo("InnerClass");
        innerClassInfo.setStaticInnerClass(true);
        innerClassInfo.setListConstructorInfos(listInnerClassConstructorInfos);
        innerClassInfo.setListFieldInfos(listInnerClassFieldInfos);
        innerClassInfo.setListMethodInfos(listInnerClassMethodInfos);

        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(innerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));

        Class<?> innerClass = clazz.getDeclaredClasses()[0];
        assertNotNull(innerClass);

        assertEquals("BoundBoxOfInnerClass", innerClass.getSimpleName());

        Method innerClassConstructor = clazz.getDeclaredMethod("boundBox_new_InnerClass");
        assertNotNull(innerClassConstructor);

        Method innerClassMethodGetterFieldA = innerClass.getDeclaredMethod("boundBox_getA");
        assertNotNull(innerClassMethodGetterFieldA);

        Method innerClassMethodSetterFieldA = innerClass.getDeclaredMethod("boundBox_setA", int.class);
        assertNotNull(innerClassMethodSetterFieldA);

        Method innerClassMethodGetterFieldB = innerClass.getDeclaredMethod("boundBox_getB");
        assertNotNull(innerClassMethodGetterFieldB);

        Method innerClassMethodSetterFieldB = null;
        try {
            innerClassMethodSetterFieldB = innerClass.getDeclaredMethod("boundBox_setB", Object.class);
            fail();
        } catch (Exception e) {
        }
        assertNull(innerClassMethodSetterFieldB);

        Method innerClassMethod1 = innerClass.getDeclaredMethod("foo");
        assertNotNull(innerClassMethod1);

        Method innerClassMethod2 = innerClass.getDeclaredMethod("bar", int.class);
        assertNotNull(innerClassMethod2);
    }
    
    //issue #18
    @Test
    public void testProcess_class_with_non_static_inner_class_inheriting_static_inner_class() throws Exception {
        // given
        String classUnderTestName = "TestClassWithNonStaticInnerClassInheritingStaticInnerClass";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);

        FieldInfo FieldInfo = new FieldInfo("a", "int");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);

        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(MethodInfo);

        InnerClassInfo innerClassInfo = new InnerClassInfo("InnerClass");
        innerClassInfo.setStaticInnerClass(true);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListMethodInfos(listMethodInfos);
        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(innerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;
        Method method = clazz.getDeclaredMethod("boundBox_getA");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setA", int.class);
        assertNotNull(method2);

        Method method3 = clazz.getDeclaredMethod("foo");
        assertNotNull(method3);

        Class<?> class1 = clazz.getDeclaredClasses()[0];
        assertNotNull(class1);

        assertEquals("BoundBoxOfInnerClass", class1.getSimpleName());
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    @Test
    public void testProcess_class_with_static_inherited_inner_class() throws Exception {
        // given
        String classUnderTestName = "TestClassWithStaticInheritedInnerClass";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add("TestClassWithStaticInnerClass");

        ClassInfo classInfo = new ClassInfo(classUnderTestName);

        InnerClassInfo innerClassInfo = new InnerClassInfo("InnerClass");
        innerClassInfo.setStaticInnerClass(true);
        innerClassInfo.setInheritanceLevel(1);
        classInfo.setListFieldInfos(Collections.<FieldInfo>emptyList());
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(innerClassInfo));
        classInfo.setListImports(new HashSet<String>());

        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(writer.getNamingGenerator().createBoundBoxName(classInfo));
        ;

        Class<?> class1 = clazz.getDeclaredClasses()[0];
        assertNotNull(class1);

        assertEquals("BoundBoxOfInnerClass", class1.getSimpleName());
    }

    // ----------------------------------
    // PREFIXES
    // ----------------------------------
    @Test
    public void testProcess_class_with_prefixes() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleField";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListImports(new HashSet<String>());

        String[] prefixes = { "BB", "" };
        writer.setPrefixes(prefixes);
        Writer out = createWriterInSandbox(writer.getNamingGenerator().createBoundBoxName(classInfo));

        // when

        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(writer.getNamingGenerator().createBoundBoxName(classInfo), neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass("BBTestClassWithSingleField");
        Method method = clazz.getDeclaredMethod("_getFoo");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("_setFoo", String.class);
        assertNotNull(method2);
    }

    // ----------------------------------
    // PACKAGE NAME
    // ----------------------------------
    @Test
    public void testProcess_class_with_package_name() throws Exception {
        // given
        String classUnderTestName = "TestClassWithPackageName";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        classInfo.setListImports(new HashSet<String>());

        String boundBoxPackageName = "foo";
        writer.setBoundBoxPackageName(boundBoxPackageName);
        String boundBoxClassFQN = boundBoxPackageName + "." + writer.getNamingGenerator().createBoundBoxName(classInfo);
        Writer out = createWriterInSandbox(boundBoxClassFQN);

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(boundBoxClassFQN, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(boundBoxClassFQN);
        assertNotNull(clazz);
    }
    
    // ----------------------------------
    // INVISIBILITY
    // ----------------------------------
    
    @Test
    public void testProcess_class_with_invisible_inner_class_and_field_of_that_type() throws Exception {
        // given
        String classUnderTestName = "TestClassWithInvisibleInnerClassAndFieldOfThatType";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        classInfo.setListImports(new HashSet<String>());

        InnerClassInfo innerClassInfo = new InnerClassInfo("B");
        innerClassInfo.setStaticInnerClass(true);
        innerClassInfo.setInheritanceLevel(0);
        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(innerClassInfo));

        FieldInfo fieldInfo = new FieldInfo("foo","Object");
        classInfo.setListFieldInfos(Arrays.<FieldInfo>asList(fieldInfo));
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListImports(new HashSet<String>());
        
        String boundBoxClassFQN = writer.getNamingGenerator().createBoundBoxName(classInfo);
        Writer out = createWriterInSandbox(boundBoxClassFQN);

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(boundBoxClassFQN, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(boundBoxClassFQN);
        assertNotNull(clazz);
        
        Method methodGetFoo = clazz.getMethod("boundBox_getFoo");
        assertNotNull(methodGetFoo);
        assertEquals(Object.class.getName(), methodGetFoo.getReturnType().getName());
    }
    
    @Test
    public void testProcess_class_with_package_invisible_inner_class_and_field_of_that_type() throws Exception {
        // given
        String classUnderTestName = "TestClassWithPackageInvisibleInnerClassAndFieldOfThatType";
        List<String> neededClasses = new ArrayList<String>();

        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        classInfo.setListImports(new HashSet<String>());

        InnerClassInfo innerClassInfo = new InnerClassInfo("B");
        innerClassInfo.setStaticInnerClass(true);
        innerClassInfo.setInheritanceLevel(0);
        MethodInfo constructorOfB = new MethodInfo("<init>", "TestClassWithPackageInvisibleInnerClassAndFieldOfThatType.B", new ArrayList<FieldInfo>(), new ArrayList<String>());
        innerClassInfo.setListConstructorInfos(Arrays.asList(constructorOfB));
        
        classInfo.setListInnerClassInfo(Arrays.<InnerClassInfo>asList(innerClassInfo));

        FieldInfo fieldInfo = new FieldInfo("foo","TestClassWithPackageInvisibleInnerClassAndFieldOfThatType.B");
        classInfo.setListFieldInfos(Arrays.<FieldInfo>asList(fieldInfo));
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListImports(new HashSet<String>());
        
        String boundBoxClassFQN = writer.getNamingGenerator().createBoundBoxName(classInfo);
        Writer out = createWriterInSandbox(boundBoxClassFQN);

        // when
        writer.writeBoundBox(classInfo, out);
        closeSandboxWriter();

        // then
        CompilationTask task = createCompileTask(boundBoxClassFQN, neededClasses);
        boolean result = task.call();
        assertTrue(result);

        Class<?> clazz = loadBoundBoxClass(boundBoxClassFQN);
        assertNotNull(clazz);
        
        Method methodGetFoo = clazz.getMethod("boundBox_getFoo");
        assertNotNull(methodGetFoo);
        assertEquals("TestClassWithPackageInvisibleInnerClassAndFieldOfThatType$B", methodGetFoo.getReturnType().getName());
        
        Method methodNewB = clazz.getMethod("boundBox_new_B");
        assertNotNull(methodNewB);
        assertEquals("TestClassWithPackageInvisibleInnerClassAndFieldOfThatType$B", methodNewB.getReturnType().getName());
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private Class<?> loadBoundBoxClass(String className) throws ClassNotFoundException {
        return new CustomClassLoader().loadClass(className);
    }

    private CompilationTask createCompileTask(String className, List<String> neededClasses) throws URISyntaxException {
        String[] writtenSourceFileNames = new String[] { classNameToJavaFile(className) };
        List<String> neededJavaFiles = new ArrayList<String>();
        for (String neededClass : neededClasses) {
            neededJavaFiles.add(classNameToJavaFile(neededClass));
        }
        String[] testSourceFileNames = neededJavaFiles.toArray(new String[0]);
        CompilationTask task = processAnnotations(writtenSourceFileNames, testSourceFileNames);
        return task;
    }

    private FileWriter createWriterInSandbox(String classFQN) throws IOException {
        createFolders(classNameToPath(classFQN));
        sandboxWriter = new FileWriter(new File(sandBoxDir, classNameToJavaFile(classFQN)));
        return sandboxWriter;
    }

    private void createFolders(String path) {
        if (StringUtils.isEmpty(path)) {
            return;
        }

        new File(sandBoxDir, path).mkdirs();
    }

    private String classNameToPath(String classFQN) {
        return StringUtils.substringBeforeLast(classFQN, ".").replaceAll("\\.", "/");
    }

    private String classNameToJavaFile(String classFQN) {
        return classFQN.replaceAll("\\.", "/").concat(".java");
    }

    private CompilationTask processAnnotations(String[] writtenSourceFileNames, String[] testSourceFileNames) throws URISyntaxException {
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
            String classPath = name.replaceAll("\\.", File.separator);
            File classFile = new File(sandBoxDir, classPath + ".class");
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
