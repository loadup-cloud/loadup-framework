package io.github.loadup.components.captcha;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.github.loadup.components.captcha.base.ChineseCaptchaAbstract;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChineseCaptcha extends ChineseCaptchaAbstract {
    private static final Logger log = LoggerFactory.getLogger(ChineseCaptcha.class);

    public ChineseCaptcha() {
        super();
    }

    public ChineseCaptcha(int width, int height) {
        this();
        setWidth(width);
        setHeight(height);
    }

    public ChineseCaptcha(int width, int height, int len) {
        this(width, height);
        setLen(len);
    }

    public ChineseCaptcha(int width, int height, int len, Font font) {
        this(width, height, len);
        setFont(font);
    }

    /**
     * 生成验证码
     *
     * @param out 输出流
     * @return 是否成功
     */
    @Override
    public boolean out(OutputStream out) {
        return graphicsImage(textChar(), out);
    }

    @Override
    public String toBase64() {
        return toBase64("data:image/png;base64,");
    }

    @Override
    public String getContentType() {
        return "image/png";
    }

    /**
     * 生成验证码图形
     *
     * @param strs 验证码
     * @param out  输出流
     * @return boolean
     */
    private boolean graphicsImage(char[] strs, OutputStream out) {
        BufferedImage bi = null;
        try {
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) bi.getGraphics();
            // 填充背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 画干扰圆
            drawOval(3, g2d);
            // 画干扰线
            g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            drawBesselLine(1, g2d);
            // 画字符串
            g2d.setFont(getFont());
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int fW = width / strs.length; // 每一个字符所占的宽度
            int fSp = (fW - (int) fontMetrics.getStringBounds("王", g2d).getWidth()) / 2; // 字符的左右边距
            for (int i = 0; i < strs.length; i++) {
                g2d.setColor(color());
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
            log.error(e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            if (bi != null) {
                bi.getGraphics().dispose();
            }
        }
        return false;
    }
}
