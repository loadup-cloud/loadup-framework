package com.github.loadup.components.testify.verifier;

public class DataVerificationException extends RuntimeException {
    private final String fieldName;
    private final int rowIndex;

    public DataVerificationException(String message, String fieldName, int rowIndex) {
        super(message);
        this.fieldName = fieldName;
        this.rowIndex = rowIndex;
    }

    public String getFieldName() { return fieldName; }
    public int getRowIndex() { return rowIndex; }
}
