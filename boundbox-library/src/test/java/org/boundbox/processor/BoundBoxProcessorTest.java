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
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
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
        EasyMock.expect(mockBoundBoxWriter.getNamingGenerator()).andReturn(new NamingGenerator());
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
            FileUtils.deleteDirectory(sandBoxDir);
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

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, FieldInfo);
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

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, FieldInfo);
        FieldInfo FieldInfo2 = new FieldInfo("a", "int");
        assertContains(listFieldInfos, FieldInfo2);
        FieldInfo FieldInfo3 = new FieldInfo("array1", "double[]");
        assertContains(listFieldInfos, FieldInfo3);
        FieldInfo FieldInfo4 = new FieldInfo("array2", "float[][]");
        assertContains(listFieldInfos, FieldInfo4);

    }

    // ----------------------------------
    // Final FIELDS
    // ----------------------------------

    @Test
    public void testProcess_class_with_single_final_field() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithSingleFinalField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo.setFinalField(true);
        assertContains(listFieldInfos, FieldInfo);
    }

    // ----------------------------------
    // EXTRA FIELDS
    // ----------------------------------

    @Test
    public void testProcess_class_with_no_extra_field() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithNoExtraField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());
    }

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

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, FieldInfo);
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
        assertEquals(listFieldInfos.size(), 1); // only one field even if foo already exists in the
        // class

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, FieldInfo);
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

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, FieldInfo);
        FieldInfo FieldInfo2 = new FieldInfo("a", "int");
        assertContains(listFieldInfos, FieldInfo2);
        FieldInfo FieldInfo3 = new FieldInfo("array1", "double[]");
        assertContains(listFieldInfos, FieldInfo3);
        FieldInfo FieldInfo4 = new FieldInfo("array2", "float[][]");
        assertContains(listFieldInfos, FieldInfo4);
        FieldInfo FieldInfo5 = new FieldInfo("ss", "java.util.ArrayList");
        assertContains(listFieldInfos, FieldInfo5);

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

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo.setStaticField(true);
        assertContains(listFieldInfos, FieldInfo);

        FieldInfo FieldInfo2 = new FieldInfo("a", "int");
        FieldInfo2.setStaticField(true);
        assertContains(listFieldInfos, FieldInfo2);
    }

    // ----------------------------------
    // STATIC INITIALIZER
    // ----------------------------------

    // We do not deal with this blocks. They are not accessible via reflection anyway
    // https://github.com/stephanenicolas/boundbox/issues/13

    // ----------------------------------
    // INSTANCE INITIALIZER
    // ----------------------------------

    // We do not deal with this blocks. A typical compiler will aggregate them with a constructor.

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

        MethodInfo MethodInfo = new MethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listConstructorInfos, MethodInfo);

        FieldInfo paramInt = new FieldInfo("a", int.class.getName());
        MethodInfo MethodInfo2 = new MethodInfo("<init>", "void", Arrays.asList(paramInt), null);
        assertContains(listConstructorInfos, MethodInfo2);

        FieldInfo paramObject = new FieldInfo("a", Object.class.getName());
        MethodInfo MethodInfo3 = new MethodInfo("<init>", "void", Arrays.asList(paramObject), null);
        assertContains(listConstructorInfos, MethodInfo3);

        FieldInfo paramObject2 = new FieldInfo("b", Object.class.getName());
        MethodInfo MethodInfo4 = new MethodInfo("<init>", "void", Arrays.asList(paramInt, paramObject2), null);
        assertContains(listConstructorInfos, MethodInfo4);

        FieldInfo paramObject3 = new FieldInfo("c", Object.class.getName());
        MethodInfo MethodInfo5 = new MethodInfo("<init>", "void", Arrays.asList(paramInt, paramObject2, paramObject3), Arrays.asList(IOException.class.getName(), RuntimeException.class.getName()));
        assertContains(listConstructorInfos, MethodInfo5);

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

        MethodInfo MethodInfo = new MethodInfo("simple", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfo);

        FieldInfo paramInt = new FieldInfo("a", int.class.getName());
        MethodInfo MethodInfo2 = new MethodInfo("withPrimitiveArgument", "void", Arrays.asList(paramInt), null);
        assertContains(listMethodInfos, MethodInfo2);

        FieldInfo paramObject = new FieldInfo("a", Object.class.getName());
        MethodInfo MethodInfo3 = new MethodInfo("withObjectArgument", "void", Arrays.asList(paramObject), null);
        assertContains(listMethodInfos, MethodInfo3);

        FieldInfo paramObject2 = new FieldInfo("b", Object.class.getName());
        MethodInfo MethodInfo4 = new MethodInfo("withManyArguments", "void", Arrays.asList(paramInt, paramObject2), null);
        assertContains(listMethodInfos, MethodInfo4);

        MethodInfo MethodInfoChar = new MethodInfo("withPrimitiveCharReturnType", char.class.getName(), new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfoChar);

        MethodInfo MethodInfo5 = new MethodInfo("withPrimitiveIntReturnType", int.class.getName(), new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfo5);

        MethodInfo MethodInfoLong = new MethodInfo("withPrimitiveLongReturnType", long.class.getName(), new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfoLong);

        MethodInfo MethodInfoShort = new MethodInfo("withPrimitiveShortReturnType", short.class.getName(), new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfoShort);

        MethodInfo MethodInfoByte = new MethodInfo("withPrimitiveByteReturnType", byte.class.getName(), new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfoByte);

        MethodInfo MethodInfo6 = new MethodInfo("withPrimitiveDoubleReturnType", double.class.getName(), new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfo6);

        MethodInfo MethodInfoFloat = new MethodInfo("withPrimitiveFloatReturnType", float.class.getName(), new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfoFloat);

        MethodInfo MethodInfo7 = new MethodInfo("withPrimitiveBooleanReturnType", boolean.class.getName(), new ArrayList<FieldInfo>(), null);
        assertContains(listMethodInfos, MethodInfo7);

        MethodInfo MethodInfo8 = new MethodInfo("withSingleThrownType", "void", new ArrayList<FieldInfo>(), Arrays.asList(IOException.class.getName()));
        assertContains(listMethodInfos, MethodInfo8);

        MethodInfo MethodInfo9 = new MethodInfo("withManyThrownType", "void", new ArrayList<FieldInfo>(), Arrays.asList(IOException.class.getName(), RuntimeException.class.getName()));
        assertContains(listMethodInfos, MethodInfo9);
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

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo.setInheritanceLevel(1);
        FieldInfo.setEffectiveInheritanceLevel(0);
        assertContains(listFieldInfos, FieldInfo);
    }

    @Test
    public void testProcess_class_with_inherited_and_conflicting_field() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInheritedAndHidingField.java", "TestClassWithSingleField.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        FieldInfo.setInheritanceLevel(0);
        assertContains(listFieldInfos, FieldInfo);

        FieldInfo FieldInfo2 = new FieldInfo("foo", "java.lang.String");
        FieldInfo2.setInheritanceLevel(1);
        assertContains(listFieldInfos, FieldInfo2);
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

        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        MethodInfo.setInheritanceLevel(1);
        assertContains(listMethodInfos, MethodInfo);
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

        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        MethodInfo.setInheritanceLevel(1);
        MethodInfo.setOverriden(true);
        assertContains(listMethodInfos, MethodInfo);

        MethodInfo MethodInfo2 = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        MethodInfo2.setInheritanceLevel(0);
        assertContains(listMethodInfos, MethodInfo2);

    }

    @Test
    public void testProcess_class_with_inherited_overriding_method() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInheritedOverridingMethod.java", "TestClassWithOverridingMethod.java", "TestClassWithSingleMethod.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());

        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        MethodInfo.setInheritanceLevel(2);
        MethodInfo.setOverriden(true);
        assertContains(listMethodInfos, MethodInfo);

        MethodInfo MethodInfo2 = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        MethodInfo2.setInheritanceLevel(1);
        MethodInfo2.setEffectiveInheritanceLevel(0);
        assertContains(listMethodInfos, MethodInfo2);
    }

    // ----------------------------------
    // MAX SUPER CLASS
    // ----------------------------------
    @Test
    public void testProcess_class_with_max_super_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithMaxSuperClass.java", "TestClassWithOverridingMethod.java", "TestClassWithSingleMethod.java" };
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
        FieldInfo ParameterInfo = new FieldInfo("strings", "java.util.List<java.lang.String>");
        listParameters.add(ParameterInfo);
        MethodInfo MethodInfo = new MethodInfo("doIt", "void", listParameters, null);
        assertContains(listMethodInfos, MethodInfo);
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

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        InnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, InnerClassInfo);
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

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        InnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, InnerClassInfo);
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

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        InnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, InnerClassInfo);

        List<MethodInfo> listInnerClassConstructorInfos = classInfo.getListInnerClassInfo().get(0).getListConstructorInfos();
        MethodInfo MethodInfo = new MethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listInnerClassConstructorInfos, MethodInfo);
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

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        InnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, InnerClassInfo);

        List<MethodInfo> listInnerClassConstructorInfos = classInfo.getListInnerClassInfo().get(0).getListConstructorInfos();
        MethodInfo MethodInfo = new MethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listInnerClassConstructorInfos, MethodInfo);

        FieldInfo paramInt = new FieldInfo("a", int.class.getName());
        MethodInfo MethodInfo2 = new MethodInfo("<init>", "void", Arrays.asList(paramInt), null);
        assertContains(listInnerClassConstructorInfos, MethodInfo2);

        FieldInfo paramObject = new FieldInfo("a", Object.class.getName());
        MethodInfo MethodInfo3 = new MethodInfo("<init>", "void", Arrays.asList(paramObject), null);
        assertContains(listInnerClassConstructorInfos, MethodInfo3);

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

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        InnerClassInfo.setStaticInnerClass(true);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, InnerClassInfo);

        List<FieldInfo> listInnerClassFieldsInfos = classInfo.getListInnerClassInfo().get(0).getListFieldInfos();
        FieldInfo FieldInfo = new FieldInfo("a", "int");
        assertContains(listInnerClassFieldsInfos, FieldInfo);

        FieldInfo FieldInfo2 = new FieldInfo("b", Object.class.getName());
        FieldInfo2.setFinalField(true);
        assertContains(listInnerClassFieldsInfos, FieldInfo2);

        List<MethodInfo> listInnerClassMethodInfos = classInfo.getListInnerClassInfo().get(0).getListMethodInfos();
        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        assertContains(listInnerClassMethodInfos, MethodInfo);

        FieldInfo paramInt = new FieldInfo("a", int.class.getName());
        MethodInfo MethodInfo2 = new MethodInfo("bar", "void", Arrays.asList(paramInt), null);
        assertContains(listInnerClassMethodInfos, MethodInfo2);

    }

    @Test
    public void testProcess_class_with_many_inner_classes() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithManyInnerClasses.java" };
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

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        InnerClassInfo.setStaticInnerClass(false);
        assertContains(listInnerClassInfos, InnerClassInfo);

        InnerClassInfo InnerClassInfo2 = new InnerClassInfo("InnerClass2");
        InnerClassInfo2.setStaticInnerClass(false);
        assertContains(listInnerClassInfos, InnerClassInfo2);

        InnerClassInfo StaticInnerClassInfo = new InnerClassInfo("StaticInnerClass");
        StaticInnerClassInfo.setStaticInnerClass(true);
        assertContains(listInnerClassInfos, StaticInnerClassInfo);

        InnerClassInfo StaticInnerClassInfo2 = new InnerClassInfo("StaticInnerClass2");
        StaticInnerClassInfo2.setStaticInnerClass(true);
        assertContains(listInnerClassInfos, StaticInnerClassInfo2);

        assertEquals(Arrays.asList(InnerClassInfo, StaticInnerClassInfo2, StaticInnerClassInfo, InnerClassInfo2), listInnerClassInfos);
    }

    // ----------------------------------
    // INHERITANCE OF INNER CLASSES
    // ----------------------------------

    @Test
    public void testProcess_class_with_inherited_static_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithStaticInheritedInnerClass.java", "TestClassWithStaticInnerClass.java" };
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

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        InnerClassInfo.setStaticInnerClass(true);
        InnerClassInfo.setInheritanceLevel(1);
        InnerClassInfo.setEffectiveInheritanceLevel(0);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, InnerClassInfo);
    }

    @Test
    public void testProcess_class_with_inherited_and_conflicting_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithStaticInheritedAndHiddingInnerClass.java", "TestClassWithStaticInheritedInnerClass.java", "TestClassWithStaticInnerClass.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        InnerClassInfo.setStaticInnerClass(true);
        InnerClassInfo.setInheritanceLevel(2);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos, InnerClassInfo);

        InnerClassInfo InnerClassInfo2 = new InnerClassInfo("InnerClass");
        InnerClassInfo2.setStaticInnerClass(true);
        InnerClassInfo2.setInheritanceLevel(0);
        assertContains(listInnerClassInfos, InnerClassInfo2);
    }

    // ----------------------------------
    // TEST CLASS IS INNER CLASS
    // ----------------------------------

    @Test
    public void testProcess_class_is_static_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassIsStaticInnerClass.java" };
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
        FieldInfo FieldInfo = new FieldInfo("foo", int.class.getSimpleName());
        FieldInfo.setStaticField(true);
        assertContains(listFieldInfos, FieldInfo);

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertTrue(listInnerClassInfos.isEmpty());
    }

    // TDD for issue #18
    @Test
    public void testProcess_class_is_non_static_inner_class_that_extends_static_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassIsNonStaticInnerClassThatExtendsStaticInnerClass.java" };
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
        FieldInfo FieldInfo = new FieldInfo("foo", int.class.getSimpleName());
        FieldInfo.setStaticField(false);
        FieldInfo.setInheritanceLevel(1);
        FieldInfo.setEffectiveInheritanceLevel(0);
        assertContains(listFieldInfos, FieldInfo);

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertTrue(listInnerClassInfos.isEmpty());
    }

    @Test
    public void testProcess_class_is_static_inner_class_that_extends_non_static_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassIsStaticInnerClassThatExtendsNonStaticInnerClass.java" };
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
        FieldInfo FieldInfo = new FieldInfo("foo", int.class.getSimpleName());
        FieldInfo.setStaticField(false);
        FieldInfo.setInheritanceLevel(1);
        FieldInfo.setEffectiveInheritanceLevel(0);
        assertContains(listFieldInfos, FieldInfo);

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertTrue(listInnerClassInfos.isEmpty());
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
        EasyMock.expect(mockBoundBoxWriter.getNamingGenerator()).andReturn(new NamingGenerator("BB", "bb"));
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

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        assertContains(classInfo.getListFieldInfos(), FieldInfo);

        EasyMock.verify(mockBoundBoxWriter);
        assertEquals(2, capturedPrefixes.getValue().length);
        assertEquals("BB", capturedPrefixes.getValue()[0]);
        assertEquals("bb", capturedPrefixes.getValue()[1]);
    }

    @Test
    public void testProcess_class_with_prefix() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithPrefix.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        BoundboxWriter mockBoundBoxWriter = EasyMock.createNiceMock(BoundboxWriter.class);
        boundBoxProcessor.setBoundboxWriter(mockBoundBoxWriter);
        EasyMock.expect(mockBoundBoxWriter.getNamingGenerator()).andReturn(new NamingGenerator("BB", "bb"));
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

        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        assertContains(classInfo.getListFieldInfos(), FieldInfo);

        EasyMock.verify(mockBoundBoxWriter);
        assertEquals(2, capturedPrefixes.getValue().length);
        assertEquals("BB", capturedPrefixes.getValue()[0]);
        assertEquals("bb", capturedPrefixes.getValue()[1]);
    }

    // ----------------------------------
    // PACKAGE NAME
    // ----------------------------------

    @Test
    public void testProcess_class_with_package_name() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithPackageName.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        BoundboxWriter mockBoundBoxWriter = EasyMock.createNiceMock(BoundboxWriter.class);
        boundBoxProcessor.setBoundboxWriter(mockBoundBoxWriter);
        EasyMock.expect(mockBoundBoxWriter.getNamingGenerator()).andReturn(new NamingGenerator());
        EasyMock.expectLastCall().anyTimes();
        mockBoundBoxWriter.setBoundBoxPackageName("foo");
        EasyMock.replay(mockBoundBoxWriter);
        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());

        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);
        assertEquals("", classInfo.getBoundClassPackageName());

        EasyMock.verify(mockBoundBoxWriter);
    }

    // ----------------------------------
    // TDD ISSSUE #18 : accessing static field in super class of non static inner class
    // ----------------------------------

    @Test
    public void testProcess_class_with_non_static_inner_class_inheriting_fields_from_a_static_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithNonStaticInnerClassInheritingStaticInnerClass.java", "TestClassWithStaticInnerClass.java" };
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

        InnerClassInfo InnerClassInfo = new InnerClassInfo("InnerClass");
        FieldInfo FieldInfoFoo = new FieldInfo("foo", int.class.getName());
        FieldInfoFoo.setStaticField(true);
        FieldInfoFoo.setInheritanceLevel(1);
        FieldInfoFoo.setEffectiveInheritanceLevel(1);
        InnerClassInfo.getListFieldInfos().add(FieldInfoFoo);
        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertContains(listInnerClassInfos.get(0).getListFieldInfos(), FieldInfoFoo);
    }

    @Test
    public void testProcess_class_with_composition_of_static_inner_class() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithCompositionOfStaticInnerClass.java" };
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
        assertFalse(listFieldInfos.isEmpty());

        FieldInfo FieldInfoFoo = new FieldInfo("a", "TestClassWithStaticInnerClass.InnerClass");
        assertContains(listFieldInfos, FieldInfoFoo);
    }

    // ----------------------------------
    // TDD for issue #15
    // ----------------------------------

    @Test
    public void testProcess_class_with_invisble_inner_class_and_field_of_that_type() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInvisibleInnerClassAndFieldOfThatType.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);
        assertTrue(boundBoxProcessor.getListOfInvisibleTypes().contains("TestClassWithInvisibleInnerClassAndFieldOfThatType.B"));
        assertEquals(1, boundBoxProcessor.getListOfInvisibleTypes().size());

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());
        FieldInfo fieldInfo = new FieldInfo("foo", Object.class.getName());
        assertContains(listFieldInfos, fieldInfo);
        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertFalse(listInnerClassInfos.isEmpty());
    }

    @Test
    public void testProcess_class_with_invisble_inner_class_and_field_of_that_type2() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInvisibleInnerClassAndFieldOfThatType2.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);
        assertTrue(boundBoxProcessor.getListOfInvisibleTypes().contains("TestClassWithInvisibleInnerClassAndFieldOfThatType2.B"));
        assertTrue(boundBoxProcessor.getListOfInvisibleTypes().contains("TestClassWithInvisibleInnerClassAndFieldOfThatType2.B.C"));
        assertEquals(2, boundBoxProcessor.getListOfInvisibleTypes().size());

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());
        FieldInfo fieldInfo = new FieldInfo("foo", Object.class.getName());
        assertContains(listFieldInfos, fieldInfo);
        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertFalse(listInnerClassInfos.isEmpty());
    }

    @Test
    public void testProcess_class_with_invisble_inner_class_and_method_returning_it() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInvisibleInnerClassAndMethodReturningIt.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);
        assertTrue(boundBoxProcessor.getListOfInvisibleTypes().contains("TestClassWithInvisibleInnerClassAndMethodReturningIt.B"));
        assertEquals(1, boundBoxProcessor.getListOfInvisibleTypes().size());

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());
        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertFalse(listMethodInfos.isEmpty());
        MethodInfo methodInfo = new MethodInfo("foo", Object.class.getName(), new ArrayList<FieldInfo>(), new ArrayList<String>());
        assertContains(listMethodInfos, methodInfo);

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertFalse(listInnerClassInfos.isEmpty());
    }

    @Test
    public void testProcess_class_with_invisble_inner_class_and_method_with_param_of_that_type() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInvisibleInnerClassAndMethodWithParamOfThatType.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);
        assertTrue(boundBoxProcessor.getListOfInvisibleTypes().contains("TestClassWithInvisibleInnerClassAndMethodWithParamOfThatType.B"));
        assertEquals(1, boundBoxProcessor.getListOfInvisibleTypes().size());

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());
        FieldInfo fieldInfo = new FieldInfo("b", Object.class.getName());
        MethodInfo methodInfo = new MethodInfo("foo", "void", Arrays.asList(fieldInfo), new ArrayList<String>());
        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertContains(listMethodInfos, methodInfo);

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertFalse(listInnerClassInfos.isEmpty());
    }

    @Test
    public void testProcess_class_with_invisble_inner_class_and_method_with_exception_of_that_type() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithInvisibleInnerClassAndMethodWithExceptionOfThatType.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);
        assertTrue(boundBoxProcessor.getListOfInvisibleTypes().contains("TestClassWithInvisibleInnerClassAndMethodWithExceptionOfThatType.B"));

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertTrue(listFieldInfos.isEmpty());
        MethodInfo methodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), Arrays.asList(Exception.class.getName()));
        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertContains(listMethodInfos, methodInfo);

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertFalse(listInnerClassInfos.isEmpty());
    }

    @Test
    public void testProcess_class_with_package_invisble_inner_class_and_field_of_that_type() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "TestClassWithPackageInvisibleInnerClassAndFieldOfThatType.java" };
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        assertFalse(boundBoxProcessor.getListClassInfo().isEmpty());
        ClassInfo classInfo = boundBoxProcessor.getListClassInfo().get(0);
        assertTrue(boundBoxProcessor.getListOfInvisibleTypes().isEmpty());

        List<FieldInfo> listFieldInfos = classInfo.getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());
        FieldInfo fieldInfo = new FieldInfo("foo", "TestClassWithPackageInvisibleInnerClassAndFieldOfThatType.B");
        assertContains(listFieldInfos, fieldInfo);
        List<MethodInfo> listMethodInfos = classInfo.getListMethodInfos();
        assertTrue(listMethodInfos.isEmpty());

        List<InnerClassInfo> listInnerClassInfos = classInfo.getListInnerClassInfo();
        assertFalse(listInnerClassInfos.isEmpty());
    }


    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private CompilationTask processAnnotations(String[] testSourceFileNames, BoundBoxProcessor boundBoxProcessor) throws URISyntaxException {
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

        if (boundBoxProcessor != null) {
            // Create a list to hold annotation processors
            LinkedList<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();

            // Add an annotation processor to the list
            processors.add(boundBoxProcessor);
            // Set the annotation processor to the compiler task
            task.setProcessors(processors);
        }

        return task;
    }

    private void assertContains(List<FieldInfo> listFieldInfos, FieldInfo FieldInfo) {
        FieldInfo fieldInfo2 = retrieveFieldInfo(listFieldInfos, FieldInfo);
        assertNotNull(fieldInfo2);
        assertEquals(FieldInfo.getFieldTypeName(), fieldInfo2.getFieldTypeName());
        assertEquals(FieldInfo.getInheritanceLevel(), fieldInfo2.getInheritanceLevel());
        assertEquals(FieldInfo.isStaticField(), fieldInfo2.isStaticField());
        assertEquals(FieldInfo.getEffectiveInheritanceLevel(), fieldInfo2.getEffectiveInheritanceLevel());
    }

    private void assertContains(List<MethodInfo> listMethodInfos, MethodInfo MethodInfo) {
        MethodInfo methodInfo2 = retrieveMethodInfo(listMethodInfos, MethodInfo);
        assertNotNull(methodInfo2);
        if( MethodInfo.isConstructor()) {
            assertTrue(methodInfo2.isConstructor());
        } else {
            assertEquals(MethodInfo.getReturnTypeName(), methodInfo2.getReturnTypeName());
        } 
        assertEquals(MethodInfo.getInheritanceLevel(), methodInfo2.getInheritanceLevel());
        assertEquals(MethodInfo.isStaticMethod(), methodInfo2.isStaticMethod());

        for (int indexParameter = 0; indexParameter < methodInfo2.getParameterTypes().size(); indexParameter++) {
            FieldInfo fieldInfo = methodInfo2.getParameterTypes().get(indexParameter);
            assertEquals(MethodInfo.getParameterTypes().get(indexParameter).getFieldName(), fieldInfo.getFieldName());
            assertEquals(MethodInfo.getParameterTypes().get(indexParameter).getFieldTypeName(), fieldInfo.getFieldTypeName());
        }

        for (int indexThrownType = 0; indexThrownType < methodInfo2.getThrownTypeNames().size(); indexThrownType++) {
            String thrownTypeName = methodInfo2.getThrownTypeNames().get(indexThrownType);
            assertEquals(MethodInfo.getThrownTypeNames().get(indexThrownType), thrownTypeName);
        }
    }

    private void assertContains(List<InnerClassInfo> listInnerClassInfos, InnerClassInfo InnerClassInfo) {
        InnerClassInfo innerClassInfo2 = retrieveInnerClassInfo(listInnerClassInfos, InnerClassInfo);
        assertNotNull(innerClassInfo2);
        assertEquals(InnerClassInfo.getInheritanceLevel(), innerClassInfo2.getInheritanceLevel());
        assertEquals(InnerClassInfo.isStaticInnerClass(), innerClassInfo2.isStaticInnerClass());
        assertEquals(InnerClassInfo.getEffectiveInheritanceLevel(), innerClassInfo2.getEffectiveInheritanceLevel());

        for (FieldInfo fieldInfo : InnerClassInfo.getListFieldInfos()) {
            assertContains(innerClassInfo2.getListFieldInfos(), (FieldInfo) fieldInfo);
        }
    }

    private FieldInfo retrieveFieldInfo(List<FieldInfo> listFieldInfos, FieldInfo FieldInfo) {
        for (FieldInfo fieldInfo : listFieldInfos) {
            if (fieldInfo.equals(FieldInfo)) {
                return fieldInfo;
            }
        }
        return null;
    }

    private MethodInfo retrieveMethodInfo(List<MethodInfo> listMethodInfos, MethodInfo MethodInfo) {
        for (MethodInfo methodInfo : listMethodInfos) {
            if (methodInfo.equals(MethodInfo)) {
                boolean haveSameParams = true;
                for (int indexParam = 0; indexParam < methodInfo.getParameterTypes().size() && haveSameParams; indexParam++) {
                    FieldInfo paramInfo = methodInfo.getParameterTypes().get(indexParam);
                    FieldInfo ParamInfo = MethodInfo.getParameterTypes().get(indexParam);
                    if (!paramInfo.getFieldName().equals(ParamInfo.getFieldName()) || !paramInfo.getFieldTypeName().equals(ParamInfo.getFieldTypeName())) {
                        haveSameParams = false;
                    }
                }
                if (haveSameParams) {
                    return methodInfo;
                }
            }
        }
        return null;
    }

    private InnerClassInfo retrieveInnerClassInfo(List<InnerClassInfo> listInnerClassInfos, InnerClassInfo InnerClassInfo) {
        for (InnerClassInfo innerClassInfo : listInnerClassInfos) {
            if (innerClassInfo.equals(InnerClassInfo)) {
                return innerClassInfo;
            }
        }
        return null;
    }

}
