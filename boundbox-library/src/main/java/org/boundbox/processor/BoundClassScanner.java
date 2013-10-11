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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import net.sf.cglib.core.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
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

    @Getter
    @Setter
    private String maxSuperClassName = Object.class.getName();
    private ClassInfo initialclassInfo;
    private List<String> visitiedTypes = new ArrayList<String>();
    @Setter
    private String boundBoxPackageName;
    @Getter
    private List<String> listOfInvisibleTypes = new ArrayList<String>();

    public ClassInfo scan(TypeElement boundClass) {
        visitiedTypes.clear();
        listOfInvisibleTypes.clear();
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

    // TODO create visitor methods visitTypeAsInnerClass and visitTypeAsSuperClass
    // it will make things more clear. Processing is a in a fuzzy state right now, but I believe
    // most bricks are in place.
    @Override
    public Void visitTypeAsClass(TypeElement e, ScanningContext scanningContext) {
        boolean isBoundClass = e.getQualifiedName().toString().equals(initialclassInfo.getClassName());

        if (!isBoundClass && !scanningContext.isInsideEnclosedElements() && !scanningContext.isInsideSuperElements()) {
            log.info("dropping class ->" + e.getSimpleName());
            return null;
        }

        if (visitiedTypes.contains(e.toString())) {
            log.info("dropping visitied class ->" + e.getSimpleName());
            return null;
        }

        visitiedTypes.add(e.toString());
        if( !computeVisibility(e)) {
            listOfInvisibleTypes.add(e.getQualifiedName().toString());
        }

        log.info("class ->" + e.getSimpleName());

        boolean isInnerClass = e.getNestingKind().isNested();
        log.info("nested ->" + isInnerClass);

        boolean isStaticElement = e.getModifiers().contains(Modifier.STATIC);
        scanningContext.setStatic((!isInnerClass || isStaticElement) && scanningContext.isStatic());

        // boundboxes around inner classes should not be considered as inner classes
        // but otherwise, if we are an inner class
        if (isInnerClass && !isBoundClass) {
            ClassInfo classInfo = scanningContext.getCurrentClassInfo();
            int inheritanceLevel = scanningContext.getInheritanceLevel();
            InnerClassInfo innerClassInfo = new InnerClassInfo(e.getSimpleName().toString());
            innerClassInfo.setInnerClassIndex(classInfo.getListInnerClassInfo().size());
            innerClassInfo.setStaticInnerClass(e.getModifiers().contains(Modifier.STATIC));
            innerClassInfo.getListSuperClassNames().add(e.toString());
            innerClassInfo.setInheritanceLevel(inheritanceLevel);

            // Current element is an inner class and we are currently scanning elements of someone
            // so we add innerclassinfo to that someone : the current class info.
            if (scanningContext.isInsideEnclosedElements()) {
                classInfo.getListInnerClassInfo().add(innerClassInfo);
            }

            // inside super classes we don't change the classInfo being scanned (inheritance
            // flatenning)
            // but outside, we do, to scan the inner class itself.
            if (!scanningContext.isInsideSuperElements()) {
                ScanningContext newScanningContext = new ScanningContext(innerClassInfo);
                newScanningContext.setInheritanceLevel(inheritanceLevel);
                scanningContext = newScanningContext;
            }
        }

        addTypeToImport(scanningContext.getCurrentClassInfo(), e.asType());

        log.info("super class ->" + e.getSuperclass().toString());
        TypeMirror superclassOfBoundClass = e.getSuperclass();
        boolean hasValidSuperClass = !maxSuperClassName.equals(superclassOfBoundClass.toString()) && !Object.class.getName().equals(superclassOfBoundClass.toString())
                && superclassOfBoundClass.getKind() == TypeKind.DECLARED;

        // if we have a valid inner class, let's scan it
        if (hasValidSuperClass) {
            DeclaredType superClassDeclaredType = (DeclaredType) superclassOfBoundClass;
            Element superClassElement = superClassDeclaredType.asElement();
            scanningContext.getCurrentClassInfo().getListSuperClassNames().add(superClassElement.toString());

            ClassInfo classInfo = scanningContext.getCurrentClassInfo();
            ScanningContext newScanningContext = new ScanningContext(classInfo);
            newScanningContext.setInheritanceLevel(scanningContext.getInheritanceLevel() + 1);
            newScanningContext.setStatic(scanningContext.isStatic());
            newScanningContext.setInsideEnclosedElements(false);
            newScanningContext.setInsideSuperElements(true);
            superClassElement.accept(BoundClassScanner.this, newScanningContext);
        }

        // and finally visit all elements of current class if :
        // it is an inner class of a bound class, or a super class
        // also, root bound class is scanned in current scanning context
        // otherwise, we don't scan
        if (isBoundClass || scanningContext.isInsideSuperElements() || isInnerClass) {
            // http://stackoverflow.com/q/7738171/693752
            for (Element enclosedElement : e.getEnclosedElements()) {
                scanningContext.setInsideEnclosedElements(true);
                scanningContext.setInsideSuperElements(false);
                enclosedElement.accept(this, scanningContext);
            }
            scanningContext.setInsideEnclosedElements(false);
        }

        return super.visitTypeAsClass(e, scanningContext);
    }

    @Override
    public Void visitExecutable(ExecutableElement e, ScanningContext scanningContext) {
        log.info("executable ->" + e.getSimpleName());
        MethodInfo methodInfo = new MethodInfo(e);
        
        //visibility of types in signature
        //TODO should go inside a computer
        if( ! computeVisibility(e.getReturnType()) ) {
            TypeElement visibleType = findSuperType(e.getReturnType());
            methodInfo.setReturnTypeName(visibleType.getQualifiedName().toString());
        }
        
        for( int indexParam =0; indexParam<e.getParameters().size();indexParam++ ) {
            VariableElement param = e.getParameters().get(indexParam);
            if( !computeVisibility(param.asType())) {
                TypeElement visibleType = findSuperType(param.asType());
                FieldInfo fieldInfo = methodInfo.getParameterTypes().get(indexParam);
                fieldInfo.setFieldTypeName(visibleType.getQualifiedName().toString());
            }
        }
        
        for( int indexThrownTypes =0; indexThrownTypes<e.getThrownTypes().size();indexThrownTypes++ ) {
            TypeMirror typeMirrorOfException = e.getThrownTypes().get(indexThrownTypes);
            if( !computeVisibility(typeMirrorOfException)) {
                TypeElement visibleType = findSuperType(typeMirrorOfException);
                String visibleTypeName = visibleType.getQualifiedName().toString();
                methodInfo.getThrownTypeNames().set(indexThrownTypes, visibleTypeName);
            }
        }
        
        //other properties
        if (methodInfo.isConstructor()) {
            if (scanningContext.getInheritanceLevel() == 0) {
                scanningContext.getCurrentClassInfo().getListConstructorInfos().add(methodInfo);
            }
        } else {
            methodInfo.setStaticMethod(e.getModifiers().contains(Modifier.STATIC) && scanningContext.isStatic());
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
        
        //TODO should go inside a computer
        TypeMirror typeOfField = e.asType();
        if( ! computeVisibility(typeOfField) ) {
            TypeElement visibleType = findSuperType(typeOfField);
            fieldInfo.setFieldTypeName(visibleType.getQualifiedName().toString());
        }
        fieldInfo.setInheritanceLevel(scanningContext.getInheritanceLevel());
        fieldInfo.setStaticField(e.getModifiers().contains(Modifier.STATIC) && scanningContext.isStatic());
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
    
    //TODO should go inside a computer
    private TypeElement findSuperType(TypeMirror typeMirror) {
        if( typeMirror.getKind() == TypeKind.DECLARED ) {
            DeclaredType declaredTypeOfField = (DeclaredType) typeMirror;
            TypeElement typeElementOfTypeOfField = (TypeElement) declaredTypeOfField.asElement();
            return findSuperType(typeElementOfTypeOfField);
        } else {
            throw new RuntimeException("Type mirror " + typeMirror + " is not a class.");
        }
    }
    
    //TODO should go inside a computer
    private TypeElement findSuperType(TypeElement e) {
        TypeElement typeElement = e;
        while( !computeVisibility(typeElement)) {
            if( typeElement.asType().getKind() == TypeKind.DECLARED ) {
                TypeMirror typeMirrorOfSuperClass = typeElement.getSuperclass();
                if(typeMirrorOfSuperClass.getKind() == TypeKind.DECLARED ) {
                    typeElement = (TypeElement) ((DeclaredType)typeMirrorOfSuperClass).asElement();
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException();
            }
        }
        return typeElement;
    }
    
    //TODO should go inside a computer
    private boolean computeVisibility(TypeMirror typeMirror) {
        if( typeMirror.getKind() == TypeKind.DECLARED ) {
            DeclaredType declaredTypeOfField = (DeclaredType) typeMirror;
            TypeElement typeElementOfTypeOfField = (TypeElement) declaredTypeOfField.asElement();
            return computeVisibility(typeElementOfTypeOfField);
        } else {
            return true;
        }
    }
    
    //TODO should go inside a computer
    private boolean computeVisibility(TypeElement e) {
        boolean isPublic = e.getModifiers().contains(Modifier.PUBLIC);
        if (isPublic) {
            return true;
        }
        boolean isPrivate = e.getModifiers().contains(Modifier.PRIVATE);
        boolean isProtectedOrPackage = !isPrivate;
        String elementFQN = e.getQualifiedName().toString();
        if (isProtectedOrPackage && isInBoundBoxPackage(elementFQN)) {
            return true;
        }

        if (e.getNestingKind().isNested()) {
//            if(e.getEnclosingElement().getKind() == ElementKind.CLASS) {
//                //TODO handle static
//                TypeMirror outerType =  e.getEnclosingElement().asType();
//                while (outerType.getKind() == TypeKind.DECLARED && !outerType.toString().equals(Object.class)) {
//                    if (!computeVisibility((TypeElement) ((DeclaredType)outerType).asElement())) {
//                        return false;
//                    }
//                    outerType = (TypeElement) ((DeclaredType)outerType).asElement());
//                }
//            }
        }
        return false;
    }

    private boolean isInBoundBoxPackage(String elementFQN) {
        String packageOfElement = "";
        if (elementFQN.contains(".")) {
            packageOfElement = StringUtils.substringBefore(elementFQN, ".");
        }
        return packageOfElement.equals(boundBoxPackageName);
    }

}
