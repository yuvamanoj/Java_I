package com.ibm.sec.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Constraint(validatedBy = { JenkinsJobDetailsDetailsValidator.class })
@Target({ TYPE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface JenkinsJobDetails {
    String message() default "Invalid input values";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
