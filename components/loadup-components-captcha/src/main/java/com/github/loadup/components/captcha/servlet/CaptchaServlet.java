package com.github.loadup.components.captcha.servlet;

import com.github.loadup.components.captcha.utils.CaptchaUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * 验证码 servlet
 *
 * @author 王帆
 * @since 2018-07-27 上午 10:08
 */
public class CaptchaServlet extends HttpServlet {
	private static final long serialVersionUID = -6252308835887611909L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		CaptchaUtil.out(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		doGet(request, response);
	}

}
