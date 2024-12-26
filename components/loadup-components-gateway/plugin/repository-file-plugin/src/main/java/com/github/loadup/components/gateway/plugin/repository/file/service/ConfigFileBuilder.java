package com.github.loadup.components.gateway.plugin.repository.file.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * configuration file builder
 */
public interface ConfigFileBuilder {

    /**
     * config file header validator
     */
    boolean isValidHeaderFormat(List<String> fileRows);

    /**
     * read content of all files in a directory to string
     */
    Map<String, String> readToStringForDirectory(File file);

    /**
     * read content of all files in a directory to string
     */
    Map<String, String> readToStringForDirectory(String assemblePath, String directory);

    /**
     * read content of all files in a directory to string list
     */
    List<String> readToStringList(String fileRootPath, String filePath);

    /**
     * read content from file to string
     */
    String readToString(File file);

    /**
     * read content from file to string list
     */
    List<String> readLines(File file) throws IOException;

    /**
     * build file path
     */
    String buildFilePath(String fileRootPath, String filePath);
}
