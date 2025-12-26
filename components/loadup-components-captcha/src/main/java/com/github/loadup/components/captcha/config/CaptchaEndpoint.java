package com.github.loadup.components.captcha.config;

import com.github.loadup.components.captcha.ArithmeticCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * @author lengleng
 * @date 2020/7/31
 */
@Controller
@RequiredArgsConstructor
public class CaptchaEndpoint {

	private final CaptchaProperties properties;

	/**
	 * 生成验证码
	 *
	 * @param response 响应流
	 */
	@SneakyThrows
	@GetMapping("${captcha.create.path:/create}")
	public void create(HttpServletResponse response) {
		ArithmeticCaptcha captcha = new ArithmeticCaptcha(properties.getWidth(), properties.getHeight());
		String result = captcha.text();
		// 设置响应头
		response.setContentType(captcha.getContentType());
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		// 转换流信息写出
		captcha.out(response.getOutputStream());
	}

}
