package io.github.loadup.components.captcha.tianai;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.captcha.CaptchaProvider;
import io.github.loadup.components.captcha.CaptchaResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * tianai-captcha backed {@link CaptchaProvider}.
 *
 * <p>Verification input is intentionally loose so the facade stays engine-agnostic:
 * <ul>
 *   <li>{@link Number} — slider percentage ({@code application.matching(id, floatValue)})</li>
 *   <li>{@link ImageCaptchaTrack} — track object</li>
 *   <li>{@link String} — float percentage, or JSON that is parsed into {@link ImageCaptchaTrack}</li>
 *   <li>anything else — rejected</li>
 * </ul>
 */
public class TianaiCaptchaProvider implements CaptchaProvider {

    private final ImageCaptchaApplication application;
    private final String defaultType;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TianaiCaptchaProvider(ImageCaptchaApplication application, String defaultType) {
        this.application = application;
        this.defaultType = defaultType;
    }

    @Override
    public CaptchaResponse generate(String type) {
        String captchaType = type != null ? type : defaultType;
        ApiResponse<ImageCaptchaVO> response = application.generateCaptcha(captchaType);
        if (!response.isSuccess() || response.getData() == null) {
            throw new IllegalStateException(
                    "Failed to generate captcha type=" + captchaType + ": " + response.getMsg());
        }
        ImageCaptchaVO vo = response.getData();
        return new CaptchaResponse(
                vo.getId(),
                vo.getType(),
                vo.getBackgroundImage(),
                vo.getTemplateImage(),
                vo.getBackgroundImageTag(),
                vo.getTemplateImageTag(),
                vo.getBackgroundImageWidth(),
                vo.getBackgroundImageHeight(),
                vo.getTemplateImageWidth(),
                vo.getTemplateImageHeight(),
                vo.getData());
    }

    @Override
    public boolean verify(String captchaId, Object userInput) {
        if (captchaId == null || userInput == null) {
            return false;
        }
        try {
            if (userInput instanceof Number number) {
                return application.matching(captchaId, number.floatValue());
            }
            if (userInput instanceof ImageCaptchaTrack track) {
                return application.matching(captchaId, track).isSuccess();
            }
            if (userInput instanceof String input) {
                String trimmed = input.trim();
                try {
                    return application.matching(captchaId, Float.parseFloat(trimmed));
                } catch (NumberFormatException ignored) {
                    ImageCaptchaTrack track = objectMapper.readValue(trimmed, ImageCaptchaTrack.class);
                    return application.matching(captchaId, track).isSuccess();
                }
            }
            return false;
        } catch (Exception e) {
            LogUtil.debug(TianaiCaptchaProvider.class, "Captcha verification failed for id={}", captchaId, e);
            return false;
        }
    }

    @Override
    public String getBinderType() {
        return "tianai";
    }
}
