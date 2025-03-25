package com.github.loadup.components.captcha.service.impl;

import com.github.loadup.components.captcha.base.Calculator;
import com.github.loadup.components.captcha.model.CaptchaResult;
import com.github.loadup.components.captcha.service.CaptchaService;
import com.github.loadup.components.captcha.service.CommonInnerService;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * png格式 计算验证码
 * Created by 王帆 on 2018-07-27 上午 10:08.
 */
@Component
public class ArithmeticCaptchaServiceImpl extends CommonInnerService implements CaptchaService {

    private String generateCalculator() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            sb.append(RandomUtils.nextInt(0, 10));
            if (i < 1) {
                int type = RandomUtils.nextInt(1, 4);
                if (type == 1) {
                    sb.append("+");
                } else if (type == 2) {
                    sb.append("-");
                } else if (type == 3) {
                    sb.append("x");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 生成验证码
     */
    @Override
    public CaptchaResult generate(String key) {
        CaptchaResult captchaResult = new CaptchaResult();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String str = generateCalculator();
        int result = (int) Calculator.conversion(str.replaceAll("x", "*"));
        saveCache(key, String.valueOf(result));
        graphicsImage((str + "=?").toCharArray(), stream);
        captchaResult.setOutputStream(stream);
        captchaResult.setBase64(toBase64(stream));
        captchaResult.setCacheKey(key);
        return captchaResult;
    }

    /**
     * 生成验证码图形
     */
    private boolean graphicsImage(char[] strs, OutputStream out) {

        int height = captchaProperties.getHeight();
        int width = captchaProperties.getWidth();
        int len = captchaProperties.getLength();
        try {
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) bi.getGraphics();
            // 填充背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 画干扰圆
            drawOval(2, g2d);
            // 画字符串
            g2d.setFont(getFont());
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int fW = width / strs.length; // 每一个字符所占的宽度
            int fSp = (fW - (int) fontMetrics.getStringBounds("8", g2d).getWidth()) / 2; // 字符的左右边距
            for (int i = 0; i < strs.length; i++) {
                g2d.setColor(randomColor());
                int fY = height
                        - ((height
                        - (int) fontMetrics
                        .getStringBounds(String.valueOf(strs[i]), g2d)
                        .getHeight())
                        >> 1); // 文字的纵坐标
                g2d.drawString(String.valueOf(strs[i]), i * fW + fSp + 3, fY - 3);
            }
            g2d.dispose();
            ImageIO.write(bi, "png", out);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}