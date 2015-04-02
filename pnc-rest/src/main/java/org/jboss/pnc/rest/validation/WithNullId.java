package org.jboss.pnc.rest.validation;

import org.jboss.pnc.common.Identifiable;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Require that the object has a field "id" and the value of that field is null.
 */
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = WithNullId.Validator.class)
@Documented
public @interface WithNullId {

    String message() default "{org.jboss.pnc.rest.validation.withnullid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<WithNullId, Identifiable<? extends Serializable>> {

        public void initialize(WithNullId annotation) {
        }

        public boolean isValid(Identifiable<? extends Serializable> identObj, ConstraintValidatorContext constraintContext) {

            if (identObj != null && identObj.getId() == null) {
                return true;
            }
            return false;

        }
    }

}
