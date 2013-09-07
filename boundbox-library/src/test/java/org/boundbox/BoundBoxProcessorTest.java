package org.boundbox;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Test;

//https://today.java.net/pub/a/today/2008/04/10/source-code-analysis-using-java-6-compiler-apis.html#invoking-the-compiler-from-code-the-java-compiler-api
public class BoundBoxProcessorTest {

    @Test
    public void testProcessSetOfQextendsTypeElementRoundEnvironment() throws URISyntaxException {
        // given
        String[] testSourceFileNames = new String[] { "Foo.java" };
        BoundBoxProcessor boundBoxProcessor = new BoundBoxProcessor();
        CompilationTask task = processAnnotations(testSourceFileNames, boundBoxProcessor);

        // when
        // Perform the compilation task.
        task.call();

        // then
        List<FieldInfo> listFieldInfos = boundBoxProcessor.getBoundClassVisitor().getListFieldInfos();
        assertFalse(listFieldInfos.isEmpty());

        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        assertContains(listFieldInfos, fakeFieldInfo);
    }

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
        CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits1);

        // Create a list to hold annotation processors
        LinkedList<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();

        // Add an annotation processor to the list
        processors.add(boundBoxProcessor);

        // Set the annotation processor to the compiler task
        task.setProcessors(processors);
        return task;
    }

    private void assertContains(List<FieldInfo> listFieldInfos, FakeFieldInfo fakeFieldInfo) {
        FieldInfo fieldInfo2 = retrieveFieldInfo(listFieldInfos, fakeFieldInfo);
        assertNotNull(fieldInfo2);
        assertEquals(fakeFieldInfo.getFieldTypeName(), fieldInfo2.getFieldType().toString());
    }

    private FieldInfo retrieveFieldInfo(List<FieldInfo> listFieldInfos, FakeFieldInfo fakeFieldInfo) {
        for (FieldInfo fieldInfo : listFieldInfos) {
            if (fieldInfo.equals(fakeFieldInfo)) {
                return fieldInfo;
            }
        }
        return null;
    }

}
