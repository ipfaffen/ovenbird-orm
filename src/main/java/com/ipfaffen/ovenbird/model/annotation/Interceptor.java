package com.ipfaffen.ovenbird.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ipfaffen.ovenbird.model.ModelEntity;
import com.ipfaffen.ovenbird.model.ModelInterceptor;

/**
 * @author Isaias Pfaffenseller
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptor {
	Class<? extends ModelInterceptor<? extends ModelEntity<?>>> value();
}