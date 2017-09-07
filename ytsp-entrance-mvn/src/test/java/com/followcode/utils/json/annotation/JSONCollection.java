package com.followcode.utils.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * JSON 集合注解
 * @author tan
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })

public @interface JSONCollection {

}
