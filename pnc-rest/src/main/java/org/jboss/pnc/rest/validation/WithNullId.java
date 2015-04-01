package org.jboss.pnc.rest.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.jboss.pnc.common.Identifiable;

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

    public class Validator implements ConstraintValidator<WithNullId, Identifiable<? extends Serializable>> {

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
