package org.boundbox.processor;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Scans a given {@link TypeElement} to produce its associated {@link ClassInfo}. This class is
 * based on the visitor design pattern and uses a {@link ScanningContext} to memorize information
 * during tree visit (and avoid using a stack).
 * @author SNI
 */
@Log
public class BoundClassScanner extends ElementKindVisitor6<Void, ScanningContext> {

    private String maxSuperClassName = Object.class.getName();
    private ClassInfo initialclassInfo;
    private List<String> visitiedTypes = new ArrayList<String>();

    public ClassInfo scan(TypeElement boundClass) {
        visitiedTypes.clear();
        initialclassInfo = new ClassInfo(boundClass.getQualifiedName().toString());
        ScanningContext initialScanningContext = new ScanningContext(initialclassInfo);
        boundClass.accept(this, initialScanningContext);
        initialclassInfo.getListImports().remove(boundClass.toString());
        maxSuperClassName = Object.class.getName();
        return initialclassInfo;
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

    //TODO create visitor methods visitTypeAsInnerClass and visitTypeAsSuperClass
    //it will make things more clear. Processing is a in a fuzzy state right now, but I believe
    //most bricks are in place.
    @Override
    public Void visitTypeAsClass(TypeElement e, ScanningContext scanningContext) {
        if( !e.getQualifiedName().toString().equals(initialclassInfo.getClassName()) && ! scanningContext.isInsideEnclosedElements() && !scanningContext.isInsideSuperElements() ) {
            log.info("dropping class ->" + e.getSimpleName());
            return null;
        }
        
        if( visitiedTypes.contains(e.toString()) ) {
            log.info("dropping visitied class ->" + e.getSimpleName());
            return null;
        }
        visitiedTypes.add(e.toString());
        
        log.info("class ->" + e.getSimpleName());

        boolean isInnerClass = e.getNestingKind().isNested();
        log.info("nested ->" + isInnerClass);

        if (isInnerClass) {
            ClassInfo classInfo = scanningContext.getCurrentClassInfo();
            int inheritanceLevel = scanningContext.getInheritanceLevel();
            InnerClassInfo innerClassInfo = new InnerClassInfo(e.getSimpleName().toString());
            innerClassInfo.setInnerClassIndex(classInfo.getListInnerClassInfo().size());
            innerClassInfo.setStaticInnerClass(e.getModifiers().contains(Modifier.STATIC));
            innerClassInfo.getListSuperClassNames().add(e.toString());
            innerClassInfo.setInheritanceLevel(inheritanceLevel);

            if(scanningContext.isInsideEnclosedElements() ) {
                classInfo.getListInnerClassInfo().add(innerClassInfo);
            }
            if( !scanningContext.isInsideSuperElements() ) {
                ScanningContext newScanningContext = new ScanningContext(innerClassInfo);
                newScanningContext.setInheritanceLevel(inheritanceLevel);
                scanningContext = newScanningContext;
            }
        }

        addTypeToImport(scanningContext.getCurrentClassInfo(), e.asType());

        // http://stackoverflow.com/q/7738171/693752
        for (Element enclosedElement : e.getEnclosedElements()) {
            scanningContext.setInsideEnclosedElements(true);
            scanningContext.setInsideSuperElements(false);
            enclosedElement.accept(this, scanningContext);
        }
        scanningContext.setInsideEnclosedElements(false);

        log.info("super class ->" + e.getSuperclass().toString());
        TypeMirror superclassOfBoundClass = e.getSuperclass();
        if (!maxSuperClassName.equals(superclassOfBoundClass.toString()) && !Object.class.getName().equals(superclassOfBoundClass.toString()) && superclassOfBoundClass.getKind() == TypeKind.DECLARED) {
            DeclaredType superClassDeclaredType = (DeclaredType) superclassOfBoundClass;
            Element superClassElement = superClassDeclaredType.asElement();
            scanningContext.getCurrentClassInfo().getListSuperClassNames().add(superClassElement.toString());
            scanningContext.setInheritanceLevel(scanningContext.getInheritanceLevel() + 1);
            scanningContext.setInsideEnclosedElements(false);
            scanningContext.setInsideSuperElements(true);
            superClassElement.accept(BoundClassScanner.this, scanningContext);
            scanningContext.setInsideSuperElements(false);
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
