package org.boundbox.processor;

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
import org.boundbox.model.InnerClassInfo;
import org.boundbox.model.MethodInfo;

@Log
public class BoundClassScanner extends ElementKindVisitor6<Void, ScanningContext> {

    // private ClassInfo classInfo;
    // private Stack<ClassInfo> stackClassInfos = new Stack<ClassInfo>();
    private String maxSuperClassName = Object.class.getName();

    public ClassInfo scan(TypeElement boundClass) {
        ClassInfo classInfo = new ClassInfo(boundClass.getQualifiedName().toString());
        ScanningContext initialScanningContext = new ScanningContext(classInfo);
        boundClass.accept(this, initialScanningContext);
        classInfo.getListImports().remove(boundClass.toString());
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
    public Void visitTypeAsClass(TypeElement e, ScanningContext scanningContext) {
        log.info("class ->" + e.getSimpleName());
        
        boolean isInnerClass = e.getNestingKind().isNested();
        log.info("nested ->" + isInnerClass);

        ScanningContext oldScanningContext = scanningContext;
        if (isInnerClass) {
            ClassInfo classInfo = scanningContext.getCurrentClassInfo();
            int inheritanceLevel = scanningContext.getInheritanceLevel();
            InnerClassInfo innerClassInfo = new InnerClassInfo(e.getSimpleName().toString());
            innerClassInfo.setInnerClassIndex(classInfo.getListInnerClassInfo().size());
            innerClassInfo.setStaticInnerClass(e.getModifiers().contains(Modifier.STATIC));
            innerClassInfo.getListSuperClassNames().add(e.toString());
            innerClassInfo.setInheritanceLevel(inheritanceLevel);

            classInfo.getListInnerClassInfo().add(innerClassInfo);
            ScanningContext newScanningContext = new ScanningContext(innerClassInfo);
            newScanningContext.setInheritanceLevel(inheritanceLevel);
            scanningContext = newScanningContext;
        }

        addTypeToImport(scanningContext.getCurrentClassInfo(), e.asType());

        // http://stackoverflow.com/q/7738171/693752
        for (Element enclosedElement : e.getEnclosedElements()) {
            enclosedElement.accept(this, scanningContext);
        }

        log.info("super class ->" + e.getSuperclass().toString());
        TypeMirror superclassOfBoundClass = e.getSuperclass();
        if (!maxSuperClassName.equals(superclassOfBoundClass.toString()) && !Object.class.getName().equals(superclassOfBoundClass.toString()) && superclassOfBoundClass.getKind() == TypeKind.DECLARED) {
            DeclaredType superClassDeclaredType = (DeclaredType) superclassOfBoundClass;
            Element superClassElement = superClassDeclaredType.asElement();
            scanningContext.getCurrentClassInfo().getListSuperClassNames().add(superClassElement.toString());
            scanningContext.setInheritanceLevel(scanningContext.getInheritanceLevel()+1);
            superClassElement.accept(BoundClassScanner.this, scanningContext);
        }

        if (isInnerClass) {
            scanningContext = oldScanningContext;
        }

        return super.visitTypeAsClass(e, scanningContext);
    }

    @Override
    public Void visitExecutable(ExecutableElement e, ScanningContext scanningContext) {
        log.info("executable ->" + e.getSimpleName());
        MethodInfo methodInfo = new MethodInfo(e);
        if (methodInfo.isConstructor()) {
            if (scanningContext.getInheritanceLevel() == 0) {
                scanningContext.getCurrentClassInfo().getListConstructorInfos().add(methodInfo);
            }
        } else {
            methodInfo.setStaticMethod(e.getModifiers().contains(Modifier.STATIC));
            methodInfo.setInheritanceLevel(scanningContext.getInheritanceLevel());
            // prevents methods overriden in subclass to be re-added in super class.
            scanningContext.getCurrentClassInfo().getListMethodInfos().add(methodInfo);
        }
        addTypeToImport(scanningContext.getCurrentClassInfo(), e.getReturnType());
        for (VariableElement param : e.getParameters()) {
            addTypeToImport(scanningContext.getCurrentClassInfo(), param.asType());
        }
        for (TypeMirror thrownType : e.getThrownTypes()) {
            addTypeToImport(scanningContext.getCurrentClassInfo(), thrownType);
        }

        return super.visitExecutable(e, scanningContext);
    }

    @Override
    public Void visitVariableAsField(VariableElement e, ScanningContext scanningContext) {
        FieldInfo fieldInfo = new FieldInfo(e);
        fieldInfo.setInheritanceLevel(scanningContext.getInheritanceLevel());
        fieldInfo.setStaticField(e.getModifiers().contains(Modifier.STATIC));
        fieldInfo.setFinalField(e.getModifiers().contains(Modifier.FINAL));
        scanningContext.getCurrentClassInfo().getListFieldInfos().add(fieldInfo);
        log.info("field ->" + fieldInfo.getFieldName() + " added.");

        addTypeToImport(scanningContext.getCurrentClassInfo(), e.asType());

        return super.visitVariableAsField(e, scanningContext);
    }

    private void addTypeToImport(ClassInfo classInfo, DeclaredType declaredType) {
        log.info("Adding to imports " + declaredType.toString().replaceAll("<.*>", ""));
        // removes parameters from type if it has some
        classInfo.getListImports().add(declaredType.toString().replaceAll("<.*>", ""));
        for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
            addTypeToImport(classInfo, typeArgument);
        }
    }

    private void addTypeToImport(ClassInfo classInfo, TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            addTypeToImport(classInfo, ((DeclaredType) typeMirror));
        }
    }
}
