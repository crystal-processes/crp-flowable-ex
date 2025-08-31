package org.crp.flowable.ai.delegates;

import org.crp.flowable.ai.AiException;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

abstract class ExpressionsHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ExpressionsHelper.class);

    protected static <T> T getValue(Expression expression, DelegateExecution execution, T defaultValue, Class<T> expectedClass) {
        if (expression == null) {
            return defaultValue;
        }
        Object value = expression.getValue(execution);
        if (value == null) {
            return null;
        }
        if (expectedClass.isInstance(value)) {
            return expectedClass.cast(value);
        }
        throw new ClassCastException("Unable to cast " + value.getClass().getName() + "to expected " + expectedClass.getName());
    }

    protected static <T> T getValue(Expression expression, DelegateExecution execution, Class<T> expectedClass) {
        return getValue(expression, execution, null, expectedClass);
    }

    protected static <T> T getMandatoryValue(String name, Expression expression, DelegateExecution execution, Class<T> expectedClass) {
        T value = getValue(expression, execution, expectedClass);
        if (value == null) {
            LOG.error("{} is mandatory.", name);
            throw new AiException(name + " is mandatory");
        }
        return value;
    }
}
