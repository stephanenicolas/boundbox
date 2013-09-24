package org.boundbox.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;

import lombok.extern.java.Log;

import org.boundbox.model.ClassInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.model.MethodInfo;

@Log
public class BoundClassScanner extends ElementKindVisitor6<Void, Integer> {

    private String maxSuperClassName = Object.class.getName();
    private List<MethodInfo> listConstructorInfos = new ArrayList<MethodInfo>();
    private List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
    private List<MethodInfo> listMethodInfos = new ArrayList<MethodInfo>();
    protected List<String> listSuperClassNames = new ArrayList<String>();
    private List<ClassInfo> listClassInfos = new ArrayList<ClassInfo>();
    private Set<String> listImports = new HashSet<String>();

    public ClassInfo scan(TypeElement boundClass) {
        listSuperClassNames.add(boundClass.toString());
        boundClass.accept(this, 0);
        listImports.remove(boundClass.toString());
        ClassInfo classInfo = new ClassInfo(boundClass.getQualifiedName().toString());
        classInfo.setListConstructorInfos(new ArrayList<MethodInfo>(listConstructorInfos));
        classInfo.setListFieldInfos(new ArrayList<FieldInfo>(listFieldInfos));
        classInfo.setListMethodInfos(new ArrayList<MethodInfo>(listMethodInfos));
        classInfo.setListInnerClassInfo(new ArrayList<ClassInfo>(listClassInfos));
        classInfo.setListSuperClassNames(new ArrayList<String>(listSuperClassNames));
        classInfo.setListImports(new HashSet<String>(listImports));
        listConstructorInfos.clear();
        listMethodInfos.clear();
        listFieldInfos.clear();
        listSuperClassNames.clear();
        maxSuperClassName = Object.class.getName();
        return classInfo;
    }

    public void setMaxSuperClass(Class<?> maxSuperClass) {
        this.maxSuperClassName = maxSuperClass.getName();
    }

    public void setMaxSuperClass(String className) {
        this.maxSuperClassName = className;
    }

    public String getMaxSuperClass() {
        return maxSuperClassName;
    }

    @Override
    public Void visitTypeAsClass(TypeElement e, final Integer inheritanceLevel) {
        log.info("class ->" + e.getSimpleName());
        boolean isInnerClass = e.getNestingKind().isNested();
        log.info("nested ->" + isInnerClass);
        if (isInnerClass) {
            return super.visitTypeAsClass(e, inheritanceLevel);
        }

        addTypeToImport(e.asType());

        // http://stackoverflow.com/q/7738171/693752
        for (Element enclosedElement : e.getEnclosedElements()) {
            enclosedElement.accept(this, inheritanceLevel);
        }

        log.info("super class ->" + e.getSuperclass().toString());
        TypeMirror superclassOfBoundClass = e.getSuperclass();
        if (!maxSuperClassName.equals(superclassOfBoundClass.toString()) && superclassOfBoundClass.getKind() == TypeKind.DECLARED) {
            DeclaredType superClassDeclaredType = (DeclaredType) superclassOfBoundClass;
            Element superClassElement = superClassDeclaredType.asElement();
            listSuperClassNames.add(superClassElement.getSimpleName().toString());
            superClassElement.accept(BoundClassScanner.this, inheritanceLevel + 1);
        }
        return super.visitTypeAsClass(e, inheritanceLevel);
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Integer inheritanceLevel) {
        log.info("executable ->" + e.getSimpleName());
        MethodInfo methodInfo = new MethodInfo(e);
        if (methodInfo.isConstructor()) {
            if (inheritanceLevel == 0) {
                listConstructorInfos.add(methodInfo);
            }
        } else {
            methodInfo.setStaticMethod(e.getModifiers().contains(Modifier.STATIC));
            methodInfo.setInheritanceLevel(inheritanceLevel);
            // prevents methods overriden in subclass to be re-added in super class.
            listMethodInfos.add(methodInfo);
        }
        addTypeToImport(e.getReturnType());
        for (VariableElement param : e.getParameters()) {
            addTypeToImport(param.asType());
        }
        for (TypeMirror thrownType : e.getThrownTypes()) {
            addTypeToImport(thrownType);
        }

        return super.visitExecutable(e, inheritanceLevel);
    }

    @Override
    public Void visitVariableAsField(VariableElement e, Integer inheritanceLevel) {
        FieldInfo fieldInfo = new FieldInfo(e);
        fieldInfo.setInheritanceLevel(inheritanceLevel);
        fieldInfo.setStaticField(e.getModifiers().contains(Modifier.STATIC));
        listFieldInfos.add(fieldInfo);
        log.info("field ->" + fieldInfo.getFieldName() + " added.");

        addTypeToImport(e.asType());

        return super.visitVariableAsField(e, inheritanceLevel);
    }

    private void addTypeToImport(DeclaredType declaredType) {
        log.info("Adding to imports " + declaredType.toString().replaceAll("<.*>", ""));
        // removes parameters from type if it has some
        listImports.add(declaredType.toString().replaceAll("<.*>", ""));
        for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
            addTypeToImport(typeArgument);
        }
    }

    private void addTypeToImport(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            addTypeToImport(((DeclaredType) typeMirror));
        }
    }
}
