package org.boundbox.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import lombok.Getter;
import lombok.Setter;


public class VisbilityComputer {
    
    @Getter
    @Setter
    private String boundBoxPackageName;

    public TypeElement findVisibleSuperType(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredTypeOfField = (DeclaredType) typeMirror;
            TypeElement typeElementOfTypeOfField = (TypeElement) declaredTypeOfField.asElement();
            return findVisibleSuperType(typeElementOfTypeOfField);
        } else {
            throw new RuntimeException("Type mirror " + typeMirror + " is not a class.");
        }
    }

    public TypeElement findVisibleSuperType(TypeElement e) {
        TypeElement typeElement = e;
        while (!computeVisibility(typeElement)) {
            if (typeElement.asType().getKind() == TypeKind.DECLARED) {
                TypeMirror typeMirrorOfSuperClass = typeElement.getSuperclass();
                if (typeMirrorOfSuperClass.getKind() == TypeKind.DECLARED) {
                    typeElement = (TypeElement) ((DeclaredType) typeMirrorOfSuperClass).asElement();
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException();
            }
        }
        return typeElement;
    }

    public boolean computeVisibility(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredTypeOfField = (DeclaredType) typeMirror;
            TypeElement typeElementOfTypeOfField = (TypeElement) declaredTypeOfField.asElement();
            return computeVisibility(typeElementOfTypeOfField);
        } else {
            return true;
        }
    }

    public boolean computeVisibility(TypeElement e) {

        // for nested classes, all outer classes must be visible too
        TypeMirror outerType = e.asType();
        TypeElement outerTypeElement = e;
        while (outerTypeElement.getNestingKind().isNested()) {
            if (outerTypeElement.getEnclosingElement().getKind() == ElementKind.CLASS || outerTypeElement.getEnclosingElement().getKind() == ElementKind.INTERFACE) {
                outerType = outerTypeElement.getEnclosingElement().asType();
                if (outerType.getKind() == TypeKind.DECLARED) {
                    outerTypeElement = (TypeElement) ((DeclaredType) outerType).asElement();
                    if (!computeVisibility(outerTypeElement)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        boolean isPublic = e.getModifiers().contains(Modifier.PUBLIC);
        if (isPublic) {
            return true;
        }
        boolean isPrivate = e.getModifiers().contains(Modifier.PRIVATE);
        boolean isProtectedOrPackage = !isPrivate;
        if (isProtectedOrPackage && isInBoundBoxPackage(outerTypeElement)) {
            return true;
        }

        return false;
    }

    private boolean isInBoundBoxPackage(Element e) {
        PackageElement packageOfOuterElement = (PackageElement) e.getEnclosingElement();
        String packageOfElement = packageOfOuterElement.getQualifiedName().toString();
        return packageOfElement.equals(boundBoxPackageName);
    }
}
