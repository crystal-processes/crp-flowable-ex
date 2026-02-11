package org.crp.flowable.ai;

public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable e) { super(message, e); }
}
