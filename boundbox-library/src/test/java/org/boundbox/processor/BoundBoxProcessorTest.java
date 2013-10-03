package org.boundbox.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.type.TypeMirror;
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
import org.boundbox.writer.BoundboxWriter;
import org.boundbox.writer.NamingGenerator;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//https://today.java.net/pub/a/today/2008/04/10/source-code-analysis-using-java-6-compiler-apis.html#invoking-the-compiler-from-code-the-java-compiler-api
public class BoundBoxProcessorTest {

    private BoundBoxProcessor boundBoxProcessor;
    private File sandBoxDir;

    @Before
    public void setup() throws IOException {
        boundBoxProcessor = new BoundBoxProcessor();
        BoundboxWriter mockBoundBoxWriter = EasyMock.createNiceMock(BoundboxWriter.class);
        boundBoxProcessor.setBoundboxWriter(mockBoundBoxWriter);
        EasyMock.expect(mockBoundBoxWriter.getNamingGenerator()).andReturn( new NamingGenerator());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockBoundBoxWriter);
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
    // FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_field() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithSingleField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, fakeFieldInfo);
    }

    @Test
    public void testProcess_class_with_many_fields() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithManyFields.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, fakeFieldInfo);
        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("a", "int");
        assertContains(listFieldInfos, fakeFieldInfo2);
        FakeFieldInfo fakeFieldInfo3 = new FakeFieldInfo("array1", "double[]");
        assertContains(listFieldInfos, fakeFieldInfo3);
        FakeFieldInfo fakeFieldInfo4 = new FakeFieldInfo("array2", "float[][]");
        assertContains(listFieldInfos, fakeFieldInfo4);

    }

    // ----------------------------------
    // EXTRA FIELDS
    // ----------------------------------

    @Test
    public void testProcess_class_with_single_extra_field() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithSingleExtraField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, fakeFieldInfo);
    }


    @Test
    public void testProcess_class_with_single_extra_field_already_exists() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithSingleExtraFieldAlreadyExists.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());
        assertEquals(listFieldInfos.size(), 1); // only one field even if foo already exists in the class

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, fakeFieldInfo);
    }

    @Test
    public void testProcess_class_with_many_extra_fields() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithManyExtraFields.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();
        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, fakeFieldInfo);
        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("a", "int");
        assertContains(listFieldInfos, fakeFieldInfo2);
        FakeFieldInfo fakeFieldInfo3 = new FakeFieldInfo("array1", "double[]");
        assertContains(listFieldInfos, fakeFieldInfo3);
        FakeFieldInfo fakeFieldInfo4 = new FakeFieldInfo("array2", "float[][]");
        assertContains(listFieldInfos, fakeFieldInfo4);
        FakeFieldInfo fakeFieldInfo5 = new FakeFieldInfo("ss", "java.util.ArrayList");
        assertContains(listFieldInfos, fakeFieldInfo5);

    }

    // ----------------------------------
    // STATIC FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_static_field() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithStaticField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        fakeFieldInfo.setStaticField(true);
        assertContains(listFieldInfos, fakeFieldInfo);

        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("a", "int");
        fakeFieldInfo2.setStaticField(true);
        assertContains(listFieldInfos, fakeFieldInfo2);
    }

    // ----------------------------------
    // STATIC INITIALIZER
    // ----------------------------------

    //We do not deal with this blocks. They are not accessible via reflection anyway
    //https://github.com/stephanenicolas/boundbox/issues/13


    // ----------------------------------
    // INSTANCE INITIALIZER
    // ----------------------------------

    //We do not deal with this blocks. A typical compiler will aggregate them with a constructor.

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_constructor() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithSingleConstructor.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listConstructorInfos = classInfo.getListConstructorInfos();
        assertFalse(listConstructorInfos.isEmpty());
        assertEquals(1, listConstructorInfos.size());
    }

    @Test
    public void testProcess_class_with_many_constructors() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithManyConstructors.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listConstructorInfos = classInfo.getListConstructorInfos();
        assertFalse(listConstructorInfos.isEmpty());

        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listConstructorInfos, fakeMethodInfo);

        FieldInfo paramInt = new FakeFieldInfo("a", int.class.getName());
        FakeMethodInfo fakeMethodInfo2 = new FakeMethodInfo("<init>", "void", Arrays.asList(paramInt), null);
        assertContains(listConstructorInfos, fakeMethodInfo2);

        FieldInfo paramObject = new FakeFieldInfo("a", Object.class.getName());
        FakeMethodInfo fakeMethodInfo3 = new FakeMethodInfo("<init>", "void", Arrays.asList(paramObject), null);
        assertContains(listConstructorInfos, fakeMethodInfo3);

        FieldInfo paramObject2 = new FakeFieldInfo("b", Object.class.getName());
        FakeMethodInfo fakeMethodInfo4 = new FakeMethodInfo("<init>", "void", Arrays.asList(paramInt, paramObject2), null);
        assertContains(listConstructorInfos, fakeMethodInfo4);

        FieldInfo paramObject3 = new FakeFieldInfo("c", Object.class.getName());
        FakeMethodInfo fakeMethodInfo5 = new FakeMethodInfo("<init>", "void",
                Arrays.asList(paramInt, paramObject2, paramObject3), Arrays.asList(IOException.class.getName(),
                        RuntimeException.class.getName()));
        assertContains(listConstructorInfos, fakeMethodInfo5);

    }

    // ----------------------------------
    // METHODS
    // ----------------------------------

    @Test
    public void testProcess_class_with_single_method() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithSingleMethod.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());
        assertEquals(1, listMethodInfos.size());
    }

    @Test
    public void testProcess_class_with_many_methods() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithManyMethods.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());

        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("simple", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfo);

        FieldInfo paramInt = new FakeFieldInfo("a", int.class.getName());
        FakeMethodInfo fakeMethodInfo2 = new FakeMethodInfo("withPrimitiveArgument", "void", Arrays.asList(paramInt), null);
        assertContains(listMethodInfos, fakeMethodInfo2);

        FieldInfo paramObject = new FakeFieldInfo("a", Object.class.getName());
        FakeMethodInfo fakeMethodInfo3 = new FakeMethodInfo("withObjectArgument", "void", Arrays.asList(paramObject), null);
        assertContains(listMethodInfos, fakeMethodInfo3);

        FieldInfo paramObject2 = new FakeFieldInfo("b", Object.class.getName());
        FakeMethodInfo fakeMethodInfo4 = new FakeMethodInfo("withManyArguments", "void", Arrays.asList(paramInt, paramObject2),
                null);
        assertContains(listMethodInfos, fakeMethodInfo4);

        FakeMethodInfo fakeMethodInfoChar = new FakeMethodInfo("withPrimitiveCharReturnType", char.class.getName(),
                new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfoChar);

        FakeMethodInfo fakeMethodInfo5 = new FakeMethodInfo("withPrimitiveIntReturnType", int.class.getName(),
                new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfo5);

        FakeMethodInfo fakeMethodInfoLong = new FakeMethodInfo("withPrimitiveLongReturnType", long.class.getName(),
                new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfoLong);

        FakeMethodInfo fakeMethodInfoShort = new FakeMethodInfo("withPrimitiveShortReturnType", short.class.getName(),
                new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfoShort);

        FakeMethodInfo fakeMethodInfoByte = new FakeMethodInfo("withPrimitiveByteReturnType", byte.class.getName(),
                new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfoByte);

        FakeMethodInfo fakeMethodInfo6 = new FakeMethodInfo("withPrimitiveDoubleReturnType", double.class.getName(),
                new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfo6);

        FakeMethodInfo fakeMethodInfoFloat = new FakeMethodInfo("withPrimitiveFloatReturnType", float.class.getName(),
                new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfoFloat);

        FakeMethodInfo fakeMethodInfo7 = new FakeMethodInfo("withPrimitiveBooleanReturnType", boolean.class.getName(),
                new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, fakeMethodInfo7);

        FakeMethodInfo fakeMethodInfo8 = new FakeMethodInfo("withSingleThrownType", "void", new ArrayList<FieldInfo>(),
                Arrays.asList(IOException.class.getName()));
        assertContains(listMethodInfos, fakeMethodInfo8);

        FakeMethodInfo fakeMethodInfo9 = new FakeMethodInfo("withManyThrownType", "void", new ArrayList<FieldInfo>(),
                Arrays.asList(IOException.class.getName(), RuntimeException.class.getName()));
        assertContains(listMethodInfos, fakeMethodInfo9);
    }

    // ----------------------------------
    // STATIC METHODS
    // ----------------------------------

    @Test
    public void testProcess_class_with_static_method() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithStaticMethod.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());
        assertEquals(1, listMethodInfos.size());
        assertTrue(listMethodInfos.get(0).isStaticMethod());
    }

    // ----------------------------------
    // INHERITANCE
    // ----------------------------------
    @Test
    public void testProcess_class_with_inherited_field_for_inheritance() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInheritedField.java", "TestClassWithSingleField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<String> listSuperClassNames = classInfo.getListSuperClassNames();
        assertFalse(listSuperClassNames.isEmpty());
        assertEquals("TestClassWithInheritedField", listSuperClassNames.get(0));
        assertEquals("TestClassWithSingleField", listSuperClassNames.get(1));
    }

    // ----------------------------------
    // INHERITANCE OF FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_inherited_field() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInheritedField.java", "TestClassWithSingleField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        fakeFieldInfo.setInheritanceLevel(1);
        fakeFieldInfo.setEffectiveInheritanceLevel(0);
        assertContains(listFieldInfos, fakeFieldInfo);
    }

    @Test
    public void testProcess_class_with_inherited_and_conflicting_field() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInheritedAndHidingField.java",
        "TestClassWithSingleField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        fakeFieldInfo.setInheritanceLevel(0);
        assertContains(listFieldInfos, fakeFieldInfo);

        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("foo", "java.lang.String");
        fakeFieldInfo2.setInheritanceLevel(1);
        assertContains(listFieldInfos, fakeFieldInfo2);
    }

    // ----------------------------------
    // INHERITANCE OF METHODS
    // ----------------------------------
    @Test
    public void testProcess_class_with_inherited_method() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInheritedMethod.java", "TestClassWithSingleMethod.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());

        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        fakeMethodInfo.setInheritanceLevel(1);
        assertContains(listMethodInfos, fakeMethodInfo);
    }

    @Test
    public void testProcess_class_with_overriding_method() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithOverridingMethod.java", "TestClassWithSingleMethod.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());

        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        fakeMethodInfo.setInheritanceLevel(1);
        fakeMethodInfo.setOverriden(true);
        assertContains(listMethodInfos, fakeMethodInfo);

        FakeMethodInfo fakeMethodInfo2 = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        fakeMethodInfo2.setInheritanceLevel(0);
        assertContains(listMethodInfos, fakeMethodInfo2);

    }

    @Test
    public void testProcess_class_with_inherited_overriding_method() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInheritedOverridingMethod.java",
                "TestClassWithOverridingMethod.java", "TestClassWithSingleMethod.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());

        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        fakeMethodInfo.setInheritanceLevel(2);
        fakeMethodInfo.setOverriden(true);
        assertContains(listMethodInfos, fakeMethodInfo);

        FakeMethodInfo fakeMethodInfo2 = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        fakeMethodInfo2.setInheritanceLevel(1);
        fakeMethodInfo2.setEffectiveInheritanceLevel(0);
        assertContains(listMethodInfos, fakeMethodInfo2);
    }

    // ----------------------------------
    // MAX SUPER CLASS
    // ----------------------------------
    @Test
    public void testProcess_class_with_max_super_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithMaxSuperClass.java",
                "TestClassWithOverridingMethod.java", "TestClassWithSingleMethod.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<String> listSuperClassNames = classInfo.getListSuperClassNames();
        assertFalse(listSuperClassNames.isEmpty());
        assertEquals("TestClassWithMaxSuperClass", listSuperClassNames.get(0));
        assertEquals(1, listSuperClassNames.size());

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());

    }

    // ----------------------------------
    // GENERICS
    // ----------------------------------

    @Test
    public void testProcess_class_with_generics() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithGenerics.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());

        ArrayList<FieldInfo> listParameters = new ArrayList<FieldInfo>();
        FieldInfo fakeParameterInfo = new FakeFieldInfo("strings", "java.util.List<java.lang.String>");
        listParameters.add(fakeParameterInfo);
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("doIt", "void", listParameters, null);
        assertContains(listMethodInfos, fakeMethodInfo);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    @Test
    public void testProcess_class_with_static_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithStaticInnerClass.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());
        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());

        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, fakeInnerClassInfo);
    }

    @Test
    public void testProcess_class_with_private_static_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithPrivateStaticInnerClass.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());
        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());

        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, fakeInnerClassInfo);
    }

    @Test
    public void testProcess_class_with_static_inner_class_with_constructor() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithStaticInnerClassWithConstructor.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());
        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());

        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, fakeInnerClassInfo);

        List<MethodInfo> listInnerClassConstructorInfos = classInfo.getListInnerClassInfo().get(0).getListConstructorInfos();
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listInnerClassConstructorInfos, fakeMethodInfo);
    }

    @Test
    public void testProcess_class_with_static_inner_class_with_many_constructors() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithStaticInnerClassWithManyConstructors.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());
        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());

        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, fakeInnerClassInfo);

        List<MethodInfo> listInnerClassConstructorInfos = classInfo.getListInnerClassInfo().get(0).getListConstructorInfos();
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listInnerClassConstructorInfos, fakeMethodInfo);

        FieldInfo paramInt = new FakeFieldInfo("a", int.class.getName());
        FakeMethodInfo fakeMethodInfo2 = new FakeMethodInfo("<init>", "void", Arrays.asList(paramInt), null);
        assertContains(listInnerClassConstructorInfos, fakeMethodInfo2);

        FieldInfo paramObject = new FakeFieldInfo("a", Object.class.getName());
        FakeMethodInfo fakeMethodInfo3 = new FakeMethodInfo("<init>", "void", Arrays.asList(paramObject), null);
        assertContains(listInnerClassConstructorInfos, fakeMethodInfo3);

    }

    @Test
    public void testProcess_class_with_static_inner_class_with_many_fields_and_methods() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithStaticInnerClassWithManyFieldsAndMethods.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());
        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());

        FakeInnerClassInfo fakeInnerClassInfo = new FakeInnerClassInfo("InnerClass");
        fakeInnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, fakeInnerClassInfo);

        List<FieldInfo> listInnerClassFieldsInfos = classInfo.getListInnerClassInfo().get(0).getListFieldInfos();
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("a", "int");
        assertContains(listInnerClassFieldsInfos, fakeFieldInfo);

        FakeFieldInfo fakeFieldInfo2 = new FakeFieldInfo("b", Object.class.getName());
        assertContains(listInnerClassFieldsInfos, fakeFieldInfo2);

        List<MethodInfo> listInnerClassMethodInfos = classInfo.getListInnerClassInfo().get(0).getListMethodInfos();
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listInnerClassMethodInfos, fakeMethodInfo);

        FieldInfo paramInt = new FakeFieldInfo("a", int.class.getName());
        FakeMethodInfo fakeMethodInfo2 = new FakeMethodInfo("bar", "void", Arrays.asList(paramInt), null);
        assertContains(listInnerClassMethodInfos, fakeMethodInfo2);

    }

    // ----------------------------------
    // IMPORTS
    // ----------------------------------

    @Test
    public void testProcess_class_with_imports() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "foo/TestClassWithImports.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        assertFalse(classInfo.getListImports().contains("foo.TestClassWithImports"));
        assertTrue(classInfo.getListImports().contains(IOException.class.getName()));
        assertTrue(classInfo.getListImports().contains(File.class.getName()));
        assertTrue(classInfo.getListImports().contains(CountDownLatch.class.getName()));
    }

    @Test
    public void testProcess_class_with_parametrized_imports() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "foo/TestClassWithParametrizedImports.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        assertFalse(classInfo.getListImports().contains("foo.TestClassWithParametrizedImports"));
        assertTrue(classInfo.getListImports().contains(IOException.class.getName()));
        assertTrue(classInfo.getListImports().contains(File.class.getName()));
        assertTrue(classInfo.getListImports().contains(CountDownLatch.class.getName()));
        assertTrue(classInfo.getListImports().contains(Set.class.getName()));
        assertTrue(classInfo.getListImports().contains(List.class.getName()));
        assertTrue(classInfo.getListImports().contains(HashMap.class.getName()));
    }

    // ----------------------------------
    // NAMING PREFIXES
    // ----------------------------------
    @Test
    public void testProcess_class_with_prefixes() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithPrefixes.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        BoundboxWriter mockBoundBoxWriter = EasyMock.createNiceMock(BoundboxWriter.class);
        boundBoxProcessor.setBoundboxWriter(mockBoundBoxWriter);
        EasyMock.expect(mockBoundBoxWriter.getNamingGenerator()).andReturn( new NamingGenerator("BB","bb"));
        EasyMock.expectLastCall().anyTimes();
        Capture<String[]> capturedPrefixes = new Capture<String[]>();
        mockBoundBoxWriter.setPrefixes(EasyMock.capture(capturedPrefixes));
        EasyMock.replay(mockBoundBoxWriter);
        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        assertContains(classInfo.getListFieldInfos(), fakeFieldInfo);

        EasyMock.verify(mockBoundBoxWriter);
        assertEquals(2,capturedPrefixes.getValue().length);
        assertEquals("BB",capturedPrefixes.getValue()[0]);
        assertEquals("bb",capturedPrefixes.getValue()[1]);
    }

    @Test
    public void testProcess_class_with_prefix() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithPrefix.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        BoundboxWriter mockBoundBoxWriter = EasyMock.createNiceMock(BoundboxWriter.class);
        boundBoxProcessor.setBoundboxWriter(mockBoundBoxWriter);
        EasyMock.expect(mockBoundBoxWriter.getNamingGenerator()).andReturn( new NamingGenerator("BB","bb"));
        EasyMock.expectLastCall().anyTimes();
        Capture<String[]> capturedPrefixes = new Capture<String[]>();
        mockBoundBoxWriter.setPrefixes(EasyMock.capture(capturedPrefixes));
        EasyMock.replay(mockBoundBoxWriter);
        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        assertContains(classInfo.getListFieldInfos(), fakeFieldInfo);

        EasyMock.verify(mockBoundBoxWriter);
        assertEquals(2,capturedPrefixes.getValue().length);
        assertEquals("BB",capturedPrefixes.getValue()[0]);
        assertEquals("bb",capturedPrefixes.getValue()[1]);
    }
    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private CompilationTask processAnnotations(String[] testSourceFileNames, BoundBoxProcessor boundBoxProcessor)
            throws URISyntaxException {
        // Get an instance of java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // Get a new instance of the standard file manager implementation
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // Get the list of java file objects, in this case we have only
        // one file, TestClass.java
        // http://stackoverflow.com/a/676102/693752
        List<File> listSourceFiles = new ArrayList<File>();
        for (String sourceFileName : testSourceFileNames) {
            listSourceFiles.add(new File(ClassLoader.getSystemResource(sourceFileName).toURI()));
        }
        Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(listSourceFiles);

        // Create the compilation task
        List<String> options = new ArrayList<String>(Arrays.asList("-d", sandBoxDir.getAbsolutePath()));
        CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits1);

        if( boundBoxProcessor != null ) {
            // Create a list to hold annotation processors
            LinkedList<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();

            // Add an annotation processor to the list
            processors.add(boundBoxProcessor);
            // Set the annotation processor to the compiler task
            task.setProcessors(processors);
        }

        return task;
    }

    private void assertContains(List<FieldInfo> listFieldInfos, FakeFieldInfo fakeFieldInfo) {
        FieldInfo fieldInfo2 = retrieveFieldInfo(listFieldInfos, fakeFieldInfo);
        assertNotNull(fieldInfo2);
        assertEquals(fakeFieldInfo.getFieldTypeName(), fieldInfo2.getFieldTypeName());
        assertEquals(fakeFieldInfo.getInheritanceLevel(), fieldInfo2.getInheritanceLevel());
        assertEquals(fakeFieldInfo.isStaticField(), fieldInfo2.isStaticField());
        assertEquals(fakeFieldInfo.getEffectiveInheritanceLevel(), fieldInfo2.getEffectiveInheritanceLevel());
    }

    private void assertContains(List<MethodInfo> listMethodInfos, FakeMethodInfo fakeMethodInfo) {
        MethodInfo methodInfo2 = retrieveMethodInfo(listMethodInfos, fakeMethodInfo);
        assertNotNull(methodInfo2);
        assertEquals(fakeMethodInfo.getReturnTypeName(), methodInfo2.getReturnType().toString());
        assertEquals(fakeMethodInfo.getInheritanceLevel(), methodInfo2.getInheritanceLevel());
        assertEquals(fakeMethodInfo.isStaticMethod(), methodInfo2.isStaticMethod());

        for (int indexParameter = 0; indexParameter < methodInfo2.getParameterTypes().size(); indexParameter++) {
            FieldInfo fieldInfo = methodInfo2.getParameterTypes().get(indexParameter);
            assertEquals(fakeMethodInfo.getParameterTypes().get(indexParameter).getFieldName(), fieldInfo.getFieldName());
            assertEquals(fakeMethodInfo.getParameterTypes().get(indexParameter).getFieldTypeName(), fieldInfo.getFieldTypeName());
        }

        for (int indexThrownType = 0; indexThrownType < methodInfo2.getThrownTypes().size(); indexThrownType++) {
            TypeMirror thrownType = methodInfo2.getThrownTypes().get(indexThrownType);
            assertEquals(fakeMethodInfo.getThrownTypeNames().get(indexThrownType), thrownType.toString());
        }
    }

    private void assertContains(List<InnerClassInfo> listInnerClassInfos, FakeInnerClassInfo fakeInnerClassInfo) {
        InnerClassInfo innerClassInfo2 = retrieveInnerClassInfo(listInnerClassInfos, fakeInnerClassInfo);
        assertNotNull(innerClassInfo2);
        assertEquals(fakeInnerClassInfo.getInheritanceLevel(), innerClassInfo2.getInheritanceLevel());
        assertEquals(fakeInnerClassInfo.isStaticInnerClass(), innerClassInfo2.isStaticInnerClass());
        assertEquals(fakeInnerClassInfo.getEffectiveInheritanceLevel(), innerClassInfo2.getEffectiveInheritanceLevel());
    }

    private FieldInfo retrieveFieldInfo(List<FieldInfo> listFieldInfos, FakeFieldInfo fakeFieldInfo) {
        for (FieldInfo fieldInfo : listFieldInfos) {
            if (fieldInfo.equals(fakeFieldInfo)) {
                return fieldInfo;
            }
        }
        return null;
    }

    private MethodInfo retrieveMethodInfo(List<MethodInfo> listMethodInfos, FakeMethodInfo fakeMethodInfo) {
        for (MethodInfo methodInfo : listMethodInfos) {
            if (methodInfo.equals(fakeMethodInfo)) {
                boolean haveSameParams = true;
                for(int indexParam = 0; indexParam < methodInfo.getParameterTypes().size() && haveSameParams ; indexParam ++) {
                    FieldInfo paramInfo = methodInfo.getParameterTypes().get(indexParam);
                    FieldInfo fakeParamInfo = fakeMethodInfo.getParameterTypes().get(indexParam);
                    if( !paramInfo.getFieldName().equals(fakeParamInfo.getFieldName()) || !paramInfo.getFieldTypeName().equals(fakeParamInfo.getFieldTypeName()) ) {
                        haveSameParams = false;
                    }
                }
                if( haveSameParams ) {
                    return methodInfo;
                }
            }
        }
        return null;
    }

    private InnerClassInfo retrieveInnerClassInfo(List<InnerClassInfo> listInnerClassInfos, FakeInnerClassInfo fakeInnerClassInfo) {
        for (InnerClassInfo innerClassInfo : listInnerClassInfos) {
            if (innerClassInfo.equals(fakeInnerClassInfo)) {
                return innerClassInfo;
            }
        }
        return null;
    }


}
