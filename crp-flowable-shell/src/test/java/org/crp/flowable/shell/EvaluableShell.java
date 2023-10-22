package org.crp.flowable.shell;

import org.springframework.shell.Input;
import org.springframework.shell.Shell;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EvaluableShell {
    private final Shell shell;

    public EvaluableShell(Shell shell) {
        this.shell = shell;

    }

    public Object evaluate(Input input) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method evaluateMethod = shell.getClass().getDeclaredMethod("evaluate", Input.class);
        if (evaluateMethod.canAccess(shell)) {
            return evaluateMethod.invoke(shell, input);
        } else {
            evaluateMethod.setAccessible(true);
            Object result = evaluateMethod.invoke(shell, input);
            evaluateMethod.setAccessible(false);
            return result;
        }
    }
}
