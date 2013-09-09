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
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.MethodInfo;
import org.junit.Before;
import org.junit.Test;

//https://today.java.net/pub/a/today/2008/04/10/source-code-analysis-using-java-6-compiler-apis.html#invoking-the-compiler-from-code-the-java-compiler-api
//http://stackoverflow.com/a/7989365/693752
//TODO clean generated classes, put in sandbox and clean sandbox.
public class BoundBoxWriterTest {

    private BoundboxWriter writer;

    @Before
    public void setup() {
        writer = new BoundboxWriter();
    }

    // ----------------------------------
    //  FIELDS
    // ----------------------------------
    @Test
    public void testProcess_class_with_single_field() throws URISyntaxException, IOException, ClassNotFoundException, SecurityException, NoSuchFieldException, NoSuchMethodException {
        // given
        ClassInfo classInfo = new ClassInfo("TestClassWithSingleField");
        FakeFieldInfo fakeFieldInfo = new FakeFieldInfo("foo", "java.lang.String");
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        listFieldInfos.add(fakeFieldInfo);
        classInfo.setListFieldInfos( listFieldInfos);
        classInfo.setListConstructorInfos( Collections.<MethodInfo>emptyList() );
        classInfo.setListMethodInfos(Collections.<MethodInfo>emptyList());
        
        File output = new File("BoundBoxOfTestClassWithSingleField.java");
        Writer out = new FileWriter( output);

        // when
        writer.writeBoundBox(classInfo, out);

        // then
        String[] writtenSourceFileNames = new String[] { "BoundBoxOfTestClassWithSingleField.java" };
        String[] testSourceFileNames = new String[] { "TestClassWithSingleField.java" };
        CompilationTask task = processAnnotations(writtenSourceFileNames, testSourceFileNames);
        boolean result = task.call();
        assertTrue( result );
        
        Class<?> clazz = new CustomClassLoader().loadClass("BoundBoxOfTestClassWithSingleField");
        Method method= clazz.getDeclaredMethod("boundBox_getFoo");
        assertNotNull(method);
        Method method2 = clazz.getDeclaredMethod("boundBox_setFoo", String.class);
        assertNotNull(method2);
    }


   
    
    // ----------------------------------
    //  PRIVATE METHODS
    // ----------------------------------

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
            listSourceFiles.add(new File(sourceFileName));
        }
        
        for (String sourceFileName : testSourceFileNames) {
            listSourceFiles.add(new File(ClassLoader.getSystemResource(sourceFileName).toURI()));
        }
        Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(listSourceFiles);

        // Create the compilation task
        CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits1);
        
        task.setProcessors( new LinkedList<Processor>());
        return task;
    }
    
    // ----------------------------------
    //  INNER CLASS
    // ----------------------------------
    private final static class CustomClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            File classFile = new File(name+".class");
            if( !classFile.exists() ) {
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
