package org.boundbox.writer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.boundbox.FakeMethodInfo;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
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
            FileUtils.deleteDirectory(sandBoxDir);
        }
    }

    // ----------------------------------
    // FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_field() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleField";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add(classUnderTestName);
        
        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        classInfo.setListFieldInfos(listFieldInfos);
        classInfo.setListConstructorInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());

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
    //  CONSTRUCTORS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_constructor() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleConstructor";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add(classUnderTestName);
        
        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("<init>", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();
        listConstructorInfos.add(fakeMethodInfo);
        classInfo.setListFieldInfos(Collections.<FieldInfo>emptyList());
        classInfo.setListConstructorInfos(listConstructorInfos);
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());

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
    }

    // ----------------------------------
    //  METHODS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_method() throws Exception {
        // given
        String classUnderTestName = "TestClassWithSingleMethod";
        List<String> neededClasses = new ArrayList<String>();
        neededClasses.add(classUnderTestName);
        
        ClassInfo classInfo = new ClassInfo(classUnderTestName);
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("foo", "void", new ArrayList<FieldInfo>(), null);
        List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
        listMethodInfos.add(fakeMethodInfo);
        classInfo.setListFieldInfos(Collections.<FieldInfo>emptyList());
        classInfo.setListConstructorInfos(Collections.<MethodInfo>emptyList());
        classInfo.setListMethodInfos(listMethodInfos);

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


    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private Class<?> loadBoundBoxClass(ClassInfo classInfo) throws ClassNotFoundException {
        return new CustomClassLoader().loadClass(classInfo.getBoundBoxClassName());
    }
    
    private CompilationTask createCompileTask(ClassInfo classInfo, List<String> neededClasses) throws URISyntaxException {
        String[] writtenSourceFileNames = new String[] { classNameToJavaFile(classInfo.getBoundBoxClassName()) };
        List<String> neededJavaFiles = new ArrayList<String>();
        for( String neededClass : neededClasses ) {
            neededJavaFiles.add(classNameToJavaFile(neededClass));
        }
        String[] testSourceFileNames = neededJavaFiles.toArray( new String[0]);
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
                byte[] bytes;
                try {
                    bytes = FileUtils.readFileToByteArray(classFile);
                    Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
                    return clazz;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

    }

}
