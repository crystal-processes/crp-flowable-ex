package org.crp.flowable.form.impl;

public class FormEngineException extends RuntimeException {

    public FormEngineException() {
        super();
    }

    public FormEngineException(String message) {
        super(message);
    }

    public FormEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormEngineException(Throwable cause) {
        super(cause);
    }

    public FormEngineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
