/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * @author Asbjoedt
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {BooleanValidator.class})
@Documented
public @interface ValidateBoolean {
  String message() default "'${validatedValue}' {boolean.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
    
}
