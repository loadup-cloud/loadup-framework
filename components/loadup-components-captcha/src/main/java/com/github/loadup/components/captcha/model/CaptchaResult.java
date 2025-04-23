package com.github.loadup.components.captcha.model;

import com.github.loadup.capability.common.model.DTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import lombok.Data;
import org.apache.commons.io.IOUtils;

/**
 * @author Lise
 */
@Data
public class CaptchaResult extends DTO {
    private ByteArrayOutputStream outputStream;
    private String base64;
    private String cacheKey;

    public void toOutputStream(OutputStream out) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        try {
            IOUtils.copy(inputStream, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(out);
        }
    }

    public void toFile(String filePath) {
        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            toOutputStream(outputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
