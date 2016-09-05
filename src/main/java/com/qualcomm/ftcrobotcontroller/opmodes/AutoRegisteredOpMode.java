package com.qualcomm.ftcrobotcontroller.opmodes;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by skaggsm on 9/1/16.
 */
@IndexAnnotated
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoRegisteredOpMode {
    String value();
}
