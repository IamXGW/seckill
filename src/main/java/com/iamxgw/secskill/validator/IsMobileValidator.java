package com.iamxgw.secskill.validator;

import com.iamxgw.secskill.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {
    private boolean require = true;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        require = constraintAnnotation.require();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (require) {
            return ValidatorUtil.isMobile(value);
        } else {
            if (!StringUtils.isEmpty(value)) {
                return ValidatorUtil.isMobile(value);
            } else {
                return true;
            }
        }
    }
}
