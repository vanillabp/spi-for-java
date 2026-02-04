package io.vanillabp.spi.process;

public class ProcessDefinitionNotFoundException extends RuntimeException {

    public ProcessDefinitionNotFoundException(String message) {
        super(message);
    }

    public ProcessDefinitionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
