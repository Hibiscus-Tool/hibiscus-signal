package io.github.signal.spring.anno;


import io.github.signal.core.ErrorHandler;
import io.github.signal.core.SignalCallback;
import io.github.signal.core.impl.DefaultErrorHandler;
import io.github.signal.core.impl.DefaultSignalCallback;

import java.lang.annotation.*;

/**
 * Annotation to mark a method or class as a signal emitter.
 * <p>
 * When the annotated method is invoked, a signal will be automatically emitted
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SignalEmitter {

    /**
     * The name of the signal/event to emit.
     *
     * @return signal name
     */
    String value() default "";

    /**
     * The custom error handler class to handle exceptions during signal emission.
     * Defaults to {@link DefaultErrorHandler}.
     *
     * @return the error handler class
     */
    Class<? extends ErrorHandler> errorHandler() default DefaultErrorHandler.class;

    /**
     * The callback to execute after signal emission.
     * Defaults to {@link DefaultSignalCallback}.
     *
     * @return the callback class
     */
    Class<? extends SignalCallback<Object, Object> > callback() default DefaultSignalCallback.class;
}
