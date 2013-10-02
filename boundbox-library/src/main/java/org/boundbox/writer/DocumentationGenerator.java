package org.boundbox.writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;

/**
 * Creates Javadoc for various meta data.
 * @author SNI
 */
public class DocumentationGenerator {
    private static final String CODE_DECORATOR_TITLE_PREFIX = "\t";
    private static final char CODE_DECORATOR = '*';

    public String generateJavadocForBoundBoxClass(ClassInfo classInfo) throws IOException {
        String className = classInfo.getClassName();
        String javadoc = "BoundBox for the class {@link %s}.";
        javadoc += " \nThis class will let you access all fields, constructors or methods of %s.";
        javadoc += "\n@see <a href='https://github.com/stephanenicolas/boundbox/wiki'>BoundBox's wiki on GitHub</a>";
        javadoc += "\n@see %s";
        return String.format(javadoc, className, className, className);
    }

    public String generateJavadocForBoundBoxConstructor(ClassInfo classInfo) throws IOException {
        String className = classInfo.getClassName();
        String javadoc = "Creates a BoundBox for %s.";
        javadoc += "\n@param %s the instance of {@link %s} that is bound by this BoundBox.";
        return String.format(javadoc, StringUtils.substringAfterLast(className, "."), "boundObject", className);
    }

    public String generateJavadocForBoundConstructor(ClassInfo classInfo, MethodInfo methodInfo, String parametersTypesCommaSeparated) throws IOException {
        String className = classInfo.getClassName();
        String javadoc = "Invokes a constructor of the class {@link %s}.";
        javadoc += " \nThis constructor that will be invoked is the constructor with the exact same signature as this method.";
        javadoc += "\n@see %s#%s(%s)";
        return String.format(javadoc, className, className, className, parametersTypesCommaSeparated);
    }

    public String generateJavadocForBoundSetter(ClassInfo classInfo, FieldInfo fieldInfo) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        List<String> listSuperClassNames = classInfo.getListSuperClassNames();
        String className = listSuperClassNames.get(fieldInfo.getInheritanceLevel());
        String javadoc = "Sets directly the value of %s.";
        javadoc += " \nThis method doesn't invoke the setter but changes the value of the field directly.";
        javadoc += "\n@param %s the new value of the field \"%s\" declared in class {@link %s}.";
        javadoc += "\n@see %s#%s";
        return String.format(javadoc, fieldName, fieldName, fieldName, className, className, fieldName);
    }

    public String generateJavadocForBoundGetter(ClassInfo classInfo, FieldInfo fieldInfo) throws IOException {
        String fieldName = fieldInfo.getFieldName();
        List<String> listSuperClassNames = classInfo.getListSuperClassNames();
        String className = listSuperClassNames.get(fieldInfo.getInheritanceLevel());
        String javadoc = "Returns directly the value of %s.";
        javadoc += " \nThis method doesn't invoke the getter but returns the value of the field directly.";
        javadoc += "\n@return the value of the field \"%s\" declared in class {@link %s}.";
        javadoc += "\n@see %s#%s";
        return String.format(javadoc, fieldName, fieldName, className, className, fieldName);
    }

    public String generateJavadocForBoundMethod(ClassInfo classInfo, MethodInfo methodInfo, String parametersTypesCommaSeparated) throws IOException {
        String className = classInfo.getClassName();
        String methodName = methodInfo.getMethodName();
        List<String> listSuperClassNames = classInfo.getListSuperClassNames();
        String javadoc = "Invokes the method \"%s\"of the class {@link %s}.";
        javadoc += " \nIn case of overloading, the method that will be invoked is the method with the exact same signature as this method.";
        javadoc += "\n@see %s#%s(%s)";
        return String.format(javadoc, methodName, className, listSuperClassNames.get(methodInfo.getInheritanceLevel()), methodName, parametersTypesCommaSeparated);
    }

    public String generateJavadocForBoundInnerClass(InnerClassInfo innerInnerClassInfo) {
        String className = innerInnerClassInfo.getClassName();
        String javadoc = "BoundBox for the inner class {@link %s}.";
        javadoc += " \nThis class will let you access all fields, constructors or methods of %s.";
        javadoc += "\n@see <a href='https://github.com/stephanenicolas/boundbox/wiki'>BoundBox's wiki on GitHub</a>";
        javadoc += "\n@see %s";
        return String.format(javadoc, className, className, className);
    }

    public String generateJavadocForBoundInnerClassAccessor(InnerClassInfo innerClassInfo, MethodInfo methodInfo, String parametersTypesCommaSeparated) {
        String className = innerClassInfo.getClassName();
        String javadoc = "Creates a BoundBox of %s, for the inner class {@link %s}.";
        javadoc += "\n@param %s the instance of {@link %s} that is bound by this BoundBox.";
        javadoc += "\n@see %s#%s(%s)";
        String simpleClassName = className.contains(".") ? StringUtils.substringAfterLast(className, ".") : className;
        return String.format(javadoc, simpleClassName, className, "boundObject", className, className, simpleClassName, parametersTypesCommaSeparated);
    }
    
    public List<String> generateCodeDecoration(String decorationTitle) throws IOException {
        List<String> decoration = new ArrayList<String>();
        String decoratorString = StringUtils.repeat(CODE_DECORATOR, decorationTitle.length());
        decoration.add(CODE_DECORATOR_TITLE_PREFIX + decoratorString);
        decoration.add(CODE_DECORATOR_TITLE_PREFIX + decorationTitle);
        decoration.add(CODE_DECORATOR_TITLE_PREFIX + decoratorString);
        return decoration;
    }
}
