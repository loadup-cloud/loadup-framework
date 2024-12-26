package com.github.loadup.components.captcha.base;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;
//import javax.script.ScriptException;

import org.apache.commons.lang3.RandomUtils;

/**
 * 算术验证码抽象类
 * Created by 王帆 on 2019-08-23 上午 10:08.
 */
public abstract class ArithmeticCaptchaAbstract extends Captcha {
    private String arithmeticString;  // 计算公式

    public ArithmeticCaptchaAbstract() {
        setLen(2);
    }

    /**
     * 生成随机验证码
     */
    @Override
    protected String alphas() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(RandomUtils.nextInt(0, 10));
            if (i < len - 1) {
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

        //        ScriptEngineManager manager = new ScriptEngineManager();
        //        ScriptEngine engine = manager.getEngineByName("javascript");
        //        try {
        //            chars = String.valueOf(engine.eval(sb.toString().replaceAll("x", "*")));
        //        } catch (ScriptException e) {
        //            e.printStackTrace();
        //        }
        int result = (int) Calculator.conversion(sb.toString().replaceAll("x", "*"));
        this.chars = String.valueOf(result);

        sb.append("=?");
        arithmeticString = sb.toString();
        return chars;
    }

    public String getArithmeticString() {
        checkAlpha();
        return arithmeticString;
    }

    public void setArithmeticString(String arithmeticString) {
        this.arithmeticString = arithmeticString;
    }
}
