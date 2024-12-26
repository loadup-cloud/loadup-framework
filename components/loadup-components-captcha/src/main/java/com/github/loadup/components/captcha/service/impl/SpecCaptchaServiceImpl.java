package com.github.loadup.components.captcha.service.impl;

import com.github.loadup.components.captcha.model.CaptchaResult;
import com.github.loadup.components.captcha.service.CaptchaService;
import com.github.loadup.components.captcha.service.CommonInnerService;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

/**
 * png格式验证码
 * Created by 王帆 on 2018-07-27 上午 10:08.
 */
@Service
public class SpecCaptchaServiceImpl extends CommonInnerService implements CaptchaService {

    /**
     * 生成验证码图形
     */
    private boolean graphicsImage(char[] strs, OutputStream out) {
        try {
            BufferedImage bi = new BufferedImage(captchaProperties.getWidth(), captchaProperties.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) bi.getGraphics();
            // 填充背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, captchaProperties.getWidth(), captchaProperties.getHeight());
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 画干扰圆
            drawOval(2, g2d);
            // 画干扰线
            g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            drawBesselLine(1, g2d);
            // 画字符串
            g2d.setFont(captchaProperties.getFont());
            FontMetrics fontMetrics = g2d.getFontMetrics();
            // 每一个字符所占的宽度
            int fW = captchaProperties.getWidth() / strs.length;
            // 字符的左右边距
            int fSp = (fW - (int) fontMetrics.getStringBounds("W", g2d).getWidth()) / 2;
            for (int i = 0; i < strs.length; i++) {
                g2d.setColor(randomColor());
                // 文字的纵坐标
                int fY = captchaProperties.getHeight() - ((captchaProperties.getHeight() - (int) fontMetrics.getStringBounds(
                        String.valueOf(strs[i]), g2d).getHeight()) >> 1);
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

    @Override
    public CaptchaResult generate(String key) {
        CaptchaResult captchaResult = new CaptchaResult();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String code = generateCode();
        saveCache(key, code);
        graphicsImage(code.toCharArray(), stream);
        captchaResult.setOutputStream(stream);
        captchaResult.setBase64(toBase64(stream));
        captchaResult.setCacheKey(key);
        return captchaResult;
    }

}