package com.lensflare.svh.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.naturalcli.ICommandExecutor;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SeeAlso {
	Class<? extends ICommandExecutor>[] value() default {};
}