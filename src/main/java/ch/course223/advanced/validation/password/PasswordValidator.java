package ch.course223.advanced.validation.password;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) throws RuntimeException {
        //TODO validate password according to project-specific standards

        return true;
    }

}
