# 自定义字符集使用示例

## 示例 1: 使用默认优化字符集

```java
import com.github.loadup.components.captcha.SpecCaptcha;
import com.github.loadup.components.captcha.GifCaptcha;
import com.github.loadup.components.captcha.ArithmeticCaptcha;

public class DefaultCaptchaExample {
    public static void main(String[] args) {
        // 创建验证码（自动使用优化后的默认字符集：2-9, A-Z除I,L,O, a-z除i,l,o）
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);

        // 获取验证码文本
        String code = captcha.text();
        System.out.println("验证码: " + code);

        // 输出为base64
        String base64 = captcha.toBase64();
        System.out.println("Base64: " + base64);
    }
}
```

## 示例 2: 编程方式自定义字符集（仅数字和大写字母）

```java
import com.github.loadup.components.captcha.SpecCaptcha;
import com.github.loadup.components.captcha.base.Randoms;

public class CustomAlphaExample {
    public static void main(String[] args) {
        // 设置自定义字符集：仅数字2-9和大写字母（无混淆字符）
        Randoms.setCustomAlpha("23456789ABCDEFGHJKMNPQRSTUVWXYZ");

        // 生成验证码
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 5);
        String code = captcha.text();
        System.out.println("自定义验证码: " + code);

        // 重置为默认字符集
        Randoms.resetToDefault();
    }
}
```

## 示例 3: 编程方式自定义字符集（仅数字）

```java
import com.github.loadup.components.captcha.SpecCaptcha;
import com.github.loadup.components.captcha.base.Randoms;

public class NumberOnlyCaptchaExample {
    public static void main(String[] args) {
        // 仅使用数字2-9（去除0和1）
        Randoms.setCustomAlpha("23456789");

        SpecCaptcha captcha = new SpecCaptcha(130, 48, 6);
        String code = captcha.text();
        System.out.println("纯数字验证码: " + code);
    }
}
```

## 示例 4: Spring Boot 配置文件方式

### application.yml

```yaml
captcha:
  # 验证码长度
  len: 4
  # 图片宽度
  width: 130
  # 图片高度
  height: 48
  # 自定义字符集（可选）- 仅使用数字和部分大写字母
  custom-characters: 23456789ABCDEFGH
```

### Controller 使用

```java
import com.github.loadup.components.captcha.ArithmeticCaptcha;
import com.github.loadup.components.captcha.config.CaptchaProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class CaptchaController {

    private final CaptchaProperties properties;

    public CaptchaController(CaptchaProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/captcha")
    public void generateCaptcha(HttpServletResponse response) throws Exception {
        // 使用配置的参数创建验证码（会自动应用custom-characters）
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(
                properties.getWidth(),
                properties.getHeight()
        );
        captcha.setLen(properties.getLen());

        // 设置响应头
        response.setContentType(captcha.getContentType());
        response.setHeader("Cache-Control", "no-cache");

        // 输出验证码
        captcha.out(response.getOutputStream());
    }
}
```

## 示例 5: 动态切换字符集

```java
import com.github.loadup.components.captcha.SpecCaptcha;
import com.github.loadup.components.captcha.base.Randoms;

public class DynamicCaptchaExample {

    // 场景1: 高安全场景，使用复杂字符集
    public static String generateHighSecurityCaptcha() {
        Randoms.setCustomAlpha("23456789ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz");
        SpecCaptcha captcha = new SpecCaptcha(150, 50, 6);
        return captcha.text();
    }

    // 场景2: 简单场景，仅用数字
    public static String generateSimpleCaptcha() {
        Randoms.setCustomAlpha("23456789");
        SpecCaptcha captcha = new SpecCaptcha(120, 40, 4);
        return captcha.text();
    }

    // 场景3: 儿童应用，仅用大写字母
    public static String generateKidsCaptcha() {
        Randoms.setCustomAlpha("ABCDEFGHJKMNPQRSTUVWXYZ");
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        return captcha.text();
    }

    public static void main(String[] args) {
        System.out.println("高安全: " + generateHighSecurityCaptcha());
        System.out.println("简单: " + generateSimpleCaptcha());
        System.out.println("儿童: " + generateKidsCaptcha());
    }
}
```

## 示例 6: 获取当前字符集

```java
import com.github.loadup.components.captcha.base.Randoms;

public class GetCurrentAlphaExample {
    public static void main(String[] args) {
        // 获取当前字符集
        char[] currentAlpha = Randoms.getCurrentAlpha();
        System.out.println("当前字符集长度: " + currentAlpha.length);
        System.out.println("当前字符集: " + new String(currentAlpha));

        // 自定义后再获取
        Randoms.setCustomAlpha("ABC123");
        currentAlpha = Randoms.getCurrentAlpha();
        System.out.println("自定义后字符集: " + new String(currentAlpha));

        // 重置
        Randoms.resetToDefault();
        currentAlpha = Randoms.getCurrentAlpha();
        System.out.println("重置后字符集长度: " + currentAlpha.length);
    }
}
```

## 示例 7: Spring Boot 完整配置示例

### application.yml (生产环境推荐配置)

```yaml
server:
  port: 8080

captcha:
  # 验证码长度 (推荐4-6)
  len: 5
  # 图片宽度
  width: 150
  # 图片高度
  height: 50
  # 自定义字符集 - 去除所有易混淆字符
  # 数字: 2-9 (无0,1)
  # 大写: A-Z (无I,L,O)
  # 小写: a-z (无i,l,o)
  custom-characters: 23456789ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz
  # 验证码生成路径
  create:
    path: /api/captcha/create

spring:
  redis:
    host: localhost
    port: 6379
    # 用于存储验证码
```

### 完整的验证码服务

```java
import com.github.loadup.components.captcha.ArithmeticCaptcha;
import com.github.loadup.components.captcha.config.CaptchaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final CaptchaProperties   properties;
    private final StringRedisTemplate redisTemplate;

    /**
     * 生成验证码
     */
    public CaptchaVO generate() {
        // 创建验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(
                properties.getWidth(),
                properties.getHeight()
        );
        captcha.setLen(properties.getLen());

        // 生成key
        String key = UUID.randomUUID().toString();

        // 获取验证码答案
        String answer = captcha.text();

        // 存入Redis，5分钟过期
        redisTemplate.opsForValue().set(
                "captcha:" + key,
                answer.toLowerCase(),
                5,
                TimeUnit.MINUTES
        );

        // 返回结果
        CaptchaVO vo = new CaptchaVO();
        vo.setKey(key);
        vo.setImage(captcha.toBase64());
        return vo;
    }

    /**
     * 验证验证码
     */
    public boolean verify(String key, String code) {
        String answer = redisTemplate.opsForValue().get("captcha:" + key);
        if (answer == null) {
            return false;
        }

        // 验证后删除
        redisTemplate.delete("captcha:" + key);

        return answer.equalsIgnoreCase(code);
    }
}

// VO类
class CaptchaVO {
    private String key;
    private String image;

    // getters and setters
    public String getKey() {return key;}

    public void setKey(String key) {this.key = key;}

    public String getImage() {return image;}

    public void setImage(String image) {this.image = image;}
}
```

## 常见使用场景

### 场景1: 注册/登录 - 使用数字+字母

```yaml
captcha:
  custom-characters: 23456789ABCDEFGHJKMNPQRSTUVWXYZ
```

### 场景2: 敏感操作 - 使用完整字符集

```yaml
captcha:
  len: 6
  custom-characters: 23456789ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz
```

### 场景3: 简化版 - 仅数字

```yaml
captcha:
  len: 6
  custom-characters: 23456789
```

### 场景4: 无障碍版 - 仅大写字母

```yaml
captcha:
  custom-characters: ABCDEFGHJKMNPQRSTUVWXYZ
```

## 最佳实践

1. **生产环境**: 使用完整的默认字符集或配置文件中的custom-characters
2. **开发环境**: 可以使用简化字符集方便测试
3. **安全性**: 验证码长度至少4位，推荐5-6位
4. **易用性**: 避免使用易混淆字符（已自动处理）
5. **存储**: 使用Redis存储验证码，设置合理的过期时间（3-5分钟）

