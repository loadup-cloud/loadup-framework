package com.github.loadup.components.captcha.base;

import com.github.loadup.components.captcha.enmus.CharColorEnum;
import com.github.loadup.components.captcha.enmus.CharFontEnum;
import com.github.loadup.components.captcha.enmus.CharTypeEnum;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

/**
 * 验证码抽象类
 * Created by 王帆 on 2018-07-27 上午 10:08.
 */
public abstract class Captcha {
    protected int len = 4; // 验证码随机字符长度
    protected int width = 130; // 验证码显示宽度
    protected int height = 48; // 验证码显示高度
    protected CharTypeEnum charType = CharTypeEnum.DEFAULT;  // 验证码类型
    protected String chars = null; // 当前验证码
    // 常用颜色
    private Font font = null; // 验证码的字体

    protected String generateCode(CharTypeEnum charType, int len) {
        String cs = "";
        switch (charType) {
            case NUMBER_ONLY:
                cs = RandomStringUtils.random(len, false, true);
                break;
            case CHAR_ONLY:
                cs = RandomStringUtils.random(len, true, false);
                break;
            case CHAR_UPPER_ONLY:
                cs = RandomStringUtils.random(len, true, false).toUpperCase();
                break;
            case CHAR_LOWER_ONLY:
                cs = RandomStringUtils.random(len, true, false).toLowerCase();
                break;
            case NUMBER_AND_UPPER:
                cs = RandomStringUtils.random(len, true, true).toUpperCase();
                break;
            default:
                cs = RandomStringUtils.random(len, true, true);
        }
        return cs;
    }

    protected void saveCache(String key, String value) {

    }

    /**
     * 生成随机验证码
     */
    protected String alphas() {
        String cs = "";
        switch (charType) {
            case NUMBER_ONLY:
                cs = RandomStringUtils.random(len, false, true);
                break;
            case CHAR_ONLY:
                cs = RandomStringUtils.random(len, true, false);
                break;
            case CHAR_UPPER_ONLY:
                cs = RandomStringUtils.random(len, true, false).toUpperCase();
                break;
            case CHAR_LOWER_ONLY:
                cs = RandomStringUtils.random(len, true, false).toLowerCase();
                break;
            case NUMBER_AND_UPPER:
                cs = RandomStringUtils.random(len, true, true).toUpperCase();
                break;
            default:
                cs = RandomStringUtils.random(len, true, true);
        }
        chars = cs;
        return cs;
    }

    /**
     * 给定范围获得随机颜色
     */
    protected Color color(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + RandomUtils.nextInt(0, bc - fc);
        int g = fc + RandomUtils.nextInt(0, bc - fc);
        int b = fc + RandomUtils.nextInt(0, bc - fc);
        return new Color(r, g, b);
    }

    /**
     * 获取随机常用颜色
     */
    protected Color color() {
        return CharColorEnum.randomColor();
    }

    /**
     * 验证码输出,抽象方法，由子类实现
     */
    public abstract boolean out(OutputStream os);

    /**
     * 输出base64编码
     */
    public abstract String toBase64();

    public String toBase64(ByteArrayOutputStream outputStream) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * 输出base64编码
     */
    public String toBase64(String type) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        out(outputStream);
        return type + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * 获取当前的验证码
     */
    public String text() {
        checkAlpha();
        return chars;
    }

    /**
     * 获取当前验证码的字符数组
     */
    public char[] textChar() {
        checkAlpha();
        return chars.toCharArray();
    }

    /**
     * 检查验证码是否生成，没有则立即生成
     */
    public void checkAlpha() {
        if (chars == null) {
            alphas(); // 生成验证码
        }
    }

    /**
     * 随机画干扰线
     */
    public void drawLine(int num, Graphics2D g) {
        drawLine(num, null, g);
    }

    /**
     * 随机画干扰线
     */
    public void drawLine(int num, Color color, Graphics2D g) {
        for (int i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int x1 = RandomUtils.nextInt(0, width - 10);
            int y1 = RandomUtils.nextInt(5, height - 5);
            int x2 = RandomUtils.nextInt(10, width + 10);
            int y2 = RandomUtils.nextInt(2, height - 2);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 随机画干扰圆
     */
    public void drawOval(int num, Graphics2D g) {
        drawOval(num, null, g);
    }

    /**
     * 随机画干扰圆
     */
    public void drawOval(int num, Color color, Graphics2D g) {
        for (int i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int w = 5 + RandomUtils.nextInt(0, 10);
            g.drawOval(RandomUtils.nextInt(0, width - 25), RandomUtils.nextInt(0, height - 15), w, w);
        }
    }

    /**
     * 随机画贝塞尔曲线
     */
    public void drawBesselLine(int num, Graphics2D g) {
        drawBesselLine(num, null, g);
    }

    /**
     * 随机画贝塞尔曲线
     */
    public void drawBesselLine(int num, Color color, Graphics2D g) {
        for (int i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int x1 = 5, y1 = RandomUtils.nextInt(5, height / 2);
            int x2 = width - 5, y2 = RandomUtils.nextInt(height / 2, height - 5);
            int ctrlx = RandomUtils.nextInt(width / 4, width / 4 * 3), ctrly = RandomUtils.nextInt(5, height - 5);
            if (RandomUtils.nextInt(0, 2) == 0) {
                int ty = y1;
                y1 = y2;
                y2 = ty;
            }
            if (RandomUtils.nextInt(0, 2) == 0) {  // 二阶贝塞尔曲线
                QuadCurve2D shape = new QuadCurve2D.Double();
                shape.setCurve(x1, y1, ctrlx, ctrly, x2, y2);
                g.draw(shape);
            } else {  // 三阶贝塞尔曲线
                int ctrlx1 = RandomUtils.nextInt(width / 4, width / 4 * 3), ctrly1 = RandomUtils.nextInt(5, height - 5);
                CubicCurve2D shape = new CubicCurve2D.Double(x1, y1, ctrlx, ctrly, ctrlx1, ctrly1, x2, y2);
                g.draw(shape);
            }
        }
    }

    public Font getFont() {
        if (font == null) {
            try {
                setFont(CharFontEnum.ACTIONJ);
            } catch (Exception e) {
                setFont(new Font("Arial", Font.BOLD, 32));
            }
        }
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setFont(CharFontEnum font) throws IOException, FontFormatException {
        setFont(font, 32f);
    }

    public void setFont(CharFontEnum font, float size) throws IOException, FontFormatException {
        setFont(font, Font.BOLD, size);
    }

    public void setFont(CharFontEnum font, int style, float size) throws IOException, FontFormatException {
        this.font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/" + font.getCode())).deriveFont(style,
                size);
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}