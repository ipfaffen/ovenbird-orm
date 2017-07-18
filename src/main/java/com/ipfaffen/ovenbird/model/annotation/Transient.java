package com.ipfaffen.ovenbird.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the property or field is not persistent. It is used to annotate a property or field of an entity
 * class, mapped superclass, or embeddable class.
 * 
 * @author Isaias Pfaffenseller
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}