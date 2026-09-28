package io.github.loadup.components.dfs.model;

/** ETag returned for one uploaded multipart part. */
public record MultipartPart(int partNumber, String eTag) {

    public MultipartPart {
        if (partNumber < 1 || partNumber > 10_000) {
            throw new IllegalArgumentException("partNumber must be between 1 and 10000");
        }
    }
}
