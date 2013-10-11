package org.boundbox.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;
import org.junit.Before;
import org.junit.Test;

public class DocumentationGeneratorTest {

    private DocumentationGenerator documentationGenerator;

    @Before
    public void setup() {
        documentationGenerator = new DocumentationGenerator();
    }

    @Test
    public void testGenerateJavadocForBoundBoxClass() throws IOException {
        // given
        String classUnderTestName = "TestClassWithNothing";
        ClassInfo classInfo = new ClassInfo(classUnderTestName);

        // when
        String javadoc = documentationGenerator.generateJavadocForBoundBoxClass(classInfo);

        // then
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("https://github.com/stephanenicolas/boundbox/wiki"));
        assertTrue(javadoc.contains("{@link TestClassWithNothing}"));
        assertTrue(javadoc.contains("@see TestClassWithNothing"));
    }

    @Test
    public void testGenerateJavadocForBoundBoxConstructor() throws IOException {
        // given
        String classUnderTestName = "TestClassWithNothing";
        ClassInfo classInfo = new ClassInfo(classUnderTestName);

        // when
        String javadoc = documentationGenerator.generateJavadocForBoundBoxConstructor(classInfo);

        // then
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("@param boundObject"));
    }

    @Test
    public void testGenerateJavadocForBoundConstructor() throws IOException {
        // given
        String classUnderTestName = "TestClassWithNothing";
        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        MethodInfo MethodInfo = new MethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();
        listConstructorInfos.add(MethodInfo);
        classInfo.setListConstructorInfos(listConstructorInfos);

        // when
        String javadoc = documentationGenerator.generateJavadocForBoundConstructor(classInfo, MethodInfo, "");

        // then
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("{@link TestClassWithNothing}"));
        assertTrue(javadoc.contains("@see TestClassWithNothing#TestClassWithNothing()"));
    }

    @Test
    public void testGenerateJavadocForBoundSetter() throws IOException {
        // given
        String classUnderTestName = "TestClassWithNothing";
        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);

        // when
        String javadoc = documentationGenerator.generateJavadocForBoundSetter(classInfo, FieldInfo);

        // then
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("@param foo"));
        assertTrue(javadoc.contains("@see TestClassWithNothing#foo"));
    }

    @Test
    public void testGenerateJavadocForBoundGetter() throws IOException {
        // given
        String classUnderTestName = "TestClassWithNothing";
        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FieldInfo FieldInfo = new FieldInfo("foo", "java.lang.String");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(FieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);

        // when
        String javadoc = documentationGenerator.generateJavadocForBoundGetter(classInfo, FieldInfo);

        // then
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("@return "));
        assertTrue(javadoc.contains("@see TestClassWithNothing#foo"));
    }

    @Test
    public void testGenerateJavadocForBoundMethod() throws IOException {
        // given
        String classUnderTestName = "TestClassWithNothing";
        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();
        listConstructorInfos.add(MethodInfo);
        classInfo.setListConstructorInfos(listConstructorInfos);

        // when
        String javadoc = documentationGenerator.generateJavadocForBoundMethod(classInfo, MethodInfo, "");

        // then
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("@see TestClassWithNothing#foo()"));
    }

    @Test
    public void testGenerateJavadocForBoundInnerClass() throws IOException {
        // given
        String classUnderTestName = "TestClassWithNothing";
        InnerClassInfo classInfo = new InnerClassInfo(classUnderTestName);

        // when
        String javadoc = documentationGenerator.generateJavadocForBoundInnerClass(classInfo);

        // then
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("https://github.com/stephanenicolas/boundbox/wiki"));
        assertTrue(javadoc.contains("{@link TestClassWithNothing}"));
    }

    @Test
    public void testGenerateJavadocForBoundInnerClassAccessor() throws IOException {
        // given
        String classUnderTestName = "TestClassWithNothing";
        InnerClassInfo classInfo = new InnerClassInfo(classUnderTestName);
        MethodInfo MethodInfo = new MethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();
        listConstructorInfos.add(MethodInfo);
        classInfo.setListConstructorInfos(listConstructorInfos);
        
        // when
        String javadoc = documentationGenerator.generateJavadocForBoundInnerClassAccessor(classInfo, MethodInfo, "");
        
        // then
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("{@link TestClassWithNothing}"));
        assertTrue(StringUtils.isNotEmpty(javadoc));
        assertTrue(javadoc.contains("@see TestClassWithNothing#TestClassWithNothing()"));
    }

    @Test
    public void testGenerateCodeDecoration() throws IOException {
        // given

        // when
        List<String> decoratorList = documentationGenerator.generateCodeDecoration("foo");

        // then
        assertFalse(decoratorList.isEmpty());
        assertEquals(3, decoratorList.size());
        assertEquals(decoratorList.get(0), decoratorList.get(2));
        assertEquals(decoratorList.get(0).length(), decoratorList.get(1).length());
    }

}
