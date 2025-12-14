package org.example.novelreader.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " o ID " + id + " nie został znaleziony");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
