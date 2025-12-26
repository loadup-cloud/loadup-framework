## 1.简介

&emsp;Java图形验证码，支持gif、中文、算术等类型，可用于Java Web、JavaSE等项目。

**重要更新：**

- ✨ 已合并 `captcha-core` 和 `captcha-spring-boot-starter` 模块为统一模块
- ✨ 默认字符集已移除容易混淆的字符（0, O, 1, I, L, i, l, o）
- ✨ 支持自定义字符集配置

[![Maven Central](https://img.shields.io/maven-central/v/com.pig4cloud.plugin/easy-captcha.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.pig4cloud.plugin/easy-captcha)

---

## 2.效果展示

![验证码](https://s2.ax1x.com/2019/08/23/msFrE8.png)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msF0DP.png)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msFwut.png)
<br/>
![验证码](https://s2.ax1x.com/2019/08/23/msFzVK.gif)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msFvb6.gif)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msFXK1.gif)

**算术类型：**

![验证码](https://s2.ax1x.com/2019/08/23/mskKPg.png)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msknIS.png)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/mskma8.png)

**中文类型：**

![验证码](https://s2.ax1x.com/2019/08/23/mskcdK.png)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msk6Z6.png)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msksqx.png)

**内置字体：**

![验证码](https://s2.ax1x.com/2019/08/23/msAVSJ.png)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msAAW4.png)
&emsp;&emsp;
![验证码](https://s2.ax1x.com/2019/08/23/msAkYF.png)


---

## 3.导入项目

### 3.1.gradle方式的引入

```groovy
dependencies {
    implementation 'com.pig4cloud.plugin:easy-captcha:2.2.5'
}
```

### 3.2.maven方式引入

```xml

<dependencies>
    <dependency>
        <groupId>com.github.loadup.framework</groupId>
        <artifactId>loadup-components-captcha</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

> 注意：原 `captcha-core` 和 `captcha-spring-boot-starter` 已合并为统一模块

### 3.3.jar包下载

[easy-captcha](https://repo1.maven.org/maven2/com/pig4cloud/plugin/easy-captcha)

## 4.使用方法

> 注意：<br/>
> &emsp;1. 使用 Jakarta 时用 `CaptchaJakartaUtil`；使用 Javax 时使用 `CaptchaUtil`；  
> &emsp;2. 使用 `CaptchaUtil` 时需要根据使用情况引入对应的 `servlet` 依赖。

### 4.1.在SpringMVC中使用

```java

@Controller
public class CaptchaController {

    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 使用 Jakarta 时用 CaptchaJakartaUtil
        CaptchaUtil.out(request, response);
    }
}
```

前端html代码：

```html
<img src="/captcha" width="130px" height="48px"/>
```

> 不要忘了把`/captcha`路径排除登录拦截，比如shiro的拦截。

### 4.2.在servlet中使用

web.xml 中配置 Javax servlet：

```xml

<web-app>
    <!-- 图形验证码servlet -->
    <servlet>
        <servlet-name>CaptchaServlet</servlet-name>
        <servlet-class>com.github.loadup.components.captcha.servlet.CaptchaServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>CaptchaServlet</servlet-name>
        <url-pattern>/captcha</url-pattern>
    </servlet-mapping>
</web-app>
```

web.xml 中配置 Jakarta servlet：

```xml

<web-app>
    <!-- 图形验证码servlet -->
    <servlet>
        <servlet-name>CaptchaServlet</servlet-name>
        <servlet-class>com.github.loadup.components.captcha.servlet.CaptchaJakartaServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>CaptchaServlet</servlet-name>
        <url-pattern>/captcha</url-pattern>
    </servlet-mapping>
</web-app>
```

前端html代码：

```html
<img src="/captcha" width="130px" height="48px"/>
```

### 4.3.判断验证码是否正确

```java

@Controller
public class LoginController {

    @PostMapping("/login")
    public JsonResult login(String username, String password, String verCode) {
        // 使用 Jakarta 时用 CaptchaJakartaUtil
        if (!CaptchaUtil.ver(verCode, request)) {
            CaptchaUtil.clear(request);  // 清除session中的验证码
            return JsonResult.error("验证码不正确");
        }
    }
}
```

### 4.4.设置宽高和位数

```java

@Controller
public class CaptchaController {

    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置位数
        CaptchaUtil.out(5, request, response);
        // 设置宽、高、位数
        CaptchaUtil.out(130, 48, 5, request, response);

        // 使用gif验证码
        GifCaptcha gifCaptcha = new GifCaptcha(130, 48, 4);
        CaptchaUtil.out(gifCaptcha, request, response);
    }
}
```

### 4.5.不使用工具类

&emsp;CaptchaUtil封装了输出验证码、存session、判断验证码等功能，也可以不使用此工具类：

```java

@Controller
public class CaptchaController {

    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 三个参数分别为宽、高、位数
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        // 设置字体
        specCaptcha.setFont(new Font("Verdana", Font.PLAIN, 32));  // 有默认字体，可以不用设置
        // 设置类型，纯数字、纯字母、字母数字混合
        specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);

        // 验证码存入session
        request.getSession().setAttribute("captcha", specCaptcha.text().toLowerCase());

        // 输出图片流
        specCaptcha.out(response.getOutputStream());
    }

    @PostMapping("/login")
    public JsonResult login(String username, String password, String verCode) {
        // 获取session中的验证码
        String sessionCode = request.getSession().getAttribute("captcha");
        // 判断验证码
        if (verCode == null || !sessionCode.equals(verCode.trim().toLowerCase())) {
            return JsonResult.error("验证码不正确");
        }
    }
}
```

## 5.更多设置

### 5.1.验证码类型

```java
public class Test {

    public static void main(String[] args) {
        // png类型
        SpecCaptcha captcha = new SpecCaptcha(130, 48);
        captcha.text();  // 获取验证码的字符
        captcha.textChar();  // 获取验证码的字符数组

        // gif类型
        GifCaptcha captcha = new GifCaptcha(130, 48);

        // 中文类型
        ChineseCaptcha captcha = new ChineseCaptcha(130, 48);

        // 中文gif类型
        ChineseGifCaptcha captcha = new ChineseGifCaptcha(130, 48);

        // 算术类型
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        captcha.setLen(3);  // 几位数运算，默认是两位
        captcha.getArithmeticString();  // 获取运算的公式：3+2=?
        captcha.text();  // 获取运算的结果：5
        captcha.supportAlgorithmSign(2); // 可设置支持的算法：2 表示只生成带加减法的公式
        captcha.setDifficulty(50); // 设置计算难度，参与计算的每一个整数的最大值
        captcha.out(outputStream);  // 输出验证码
        //简单算术类型 SimpleArithmeticCaptcha,用法同ArithmeticCaptcha,只支持加减，计算结果为正整数
    }
}
```

> 注意：<br/>
> &emsp;1. 算术验证码的len表示是几位数运算，而其他验证码的len表示验证码的位数，算术验证码的text()表示的是公式的结果，
> 对于算术验证码，你应该把公式的结果存储session，而不是公式。  
> &emsp;2. 由于部分字体库的问题，除号可能无法显示

### 5.2.验证码字符类型

#### 算数验证码

| 值 | 描述   |
|---|------|
| 2 | 加法   |
| 3 | 加减   |
| 4 | 加减乘  |
| 5 | 加减乘除 |

使用方法：

```java
    ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha();
    arithmeticCaptcha.

supportAlgorithmSign(5);
```

#### 验证码

| 类型                 | 描述      |
|--------------------|---------|
| TYPE_DEFAULT       | 数字和字母混合 |
| TYPE_ONLY_NUMBER   | 纯数字     |
| TYPE_ONLY_CHAR     | 纯字母     |
| TYPE_ONLY_UPPER    | 纯大写字母   |
| TYPE_ONLY_LOWER    | 纯小写字母   |
| TYPE_NUM_AND_UPPER | 数字和大写字母 |

使用方法：

```java
    SpecCaptcha captcha = new SpecCaptcha(130, 48, 5);
    captcha.

setCharType(Captcha.TYPE_ONLY_NUMBER);
```

> 只有`SpecCaptcha`和`GifCaptcha`设置才有效果。

### 5.3.字体设置

内置字体：

| 字体              | 效果                                             |
|-----------------|------------------------------------------------|
| Captcha.FONT_1  | ![](https://s2.ax1x.com/2019/08/23/msMe6U.png) |
| Captcha.FONT_2  | ![](https://s2.ax1x.com/2019/08/23/msMAf0.png) |
| Captcha.FONT_3  | ![](https://s2.ax1x.com/2019/08/23/msMCwj.png) |
| Captcha.FONT_4  | ![](https://s2.ax1x.com/2019/08/23/msM9mQ.png) |
| Captcha.FONT_5  | ![](https://s2.ax1x.com/2019/08/23/msKz6S.png) |
| Captcha.FONT_6  | ![](https://s2.ax1x.com/2019/08/23/msKxl8.png) |
| Captcha.FONT_7  | ![](https://s2.ax1x.com/2019/08/23/msMPTs.png) |
| Captcha.FONT_8  | ![](https://s2.ax1x.com/2019/08/23/msMmXF.png) |
| Captcha.FONT_9  | ![](https://s2.ax1x.com/2019/08/23/msMVpV.png) |
| Captcha.FONT_10 | ![](https://s2.ax1x.com/2019/08/23/msMZlT.png) |

使用方法：

```
SpecCaptcha captcha = new SpecCaptcha(130, 48, 5);

// 设置内置字体
captcha.setFont(Captcha.FONT_1); 

// 设置系统字体
captcha.setFont(new Font("楷体", Font.PLAIN, 28)); 
```

### 5.4.输出base64编码

```
SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
specCaptcha.toBase64();

// 如果不想要base64的头部data:image/png;base64,
specCaptcha.toBase64("");  // 加一个空的参数即可
```

### 5.5.输出到文件

```
FileOutputStream outputStream = new FileOutputStream(new File("C:/captcha.png"))
SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
specCaptcha.out(outputStream);
```

---

## 6.前后端分离项目的使用

&emsp;前后端分离项目建议不要存储在session中，存储在redis中，redis存储需要一个key，key一同返回给前端用于验证输入：

```java

@Controller
public class CaptchaController {
    @Autowired
    private RedisUtil redisUtil;

    @ResponseBody
    @RequestMapping("/captcha")
    public JsonResult captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        String verCode = specCaptcha.text().toLowerCase();
        String key = UUID.randomUUID().toString();
        // 存入redis并设置过期时间为30分钟
        redisUtil.setEx(key, verCode, 30, TimeUnit.MINUTES);
        // 将key和base64返回给前端
        return JsonResult.ok().put("key", key).put("image", specCaptcha.toBase64());
    }

    @ResponseBody
    @PostMapping("/login")
    public JsonResult login(String username, String password, String verCode, String verKey) {
        // 获取redis中的验证码
        String redisCode = redisUtil.get(verKey);
        // 判断验证码
        if (verCode == null || !redisCode.equals(verCode.trim().toLowerCase())) {
            return JsonResult.error("验证码不正确");
        }
    }
}
```

前端使用ajax获取验证码：

```html
<img id="verImg" width="130px" height="48px"/>

<script>
    var verKey;
    // 获取验证码
    $.get('/captcha', function (res) {
        verKey = res.key;
        $('#verImg').attr('src', res.image);
    }, 'json');

    // 登录
    $.post('/login', {
        verKey: verKey,
        verCode: '8u6h',
        username: 'admin',
        password: 'admin'
    }, function (res) {
        console.log(res);
    }, 'json');
</script>
```

> RedisUtil到这里获取[https://gitee.com/whvse/RedisUtil](https://gitee.com/whvse/RedisUtil)

---

## 7.自定义字符集

### 7.1.默认字符集优化

&emsp;为了提升用户体验，默认字符集已移除容易混淆的字符：

- `0` (数字零) - 与大写字母 O 相似
- `O` (大写字母O) - 与数字 0 相似
- `1` (数字一) - 与大写字母 I 和小写字母 l 相似
- `I` (大写字母I) - 与数字 1 相似
- `L` (大写字母L) - 与小写字母 l 相似
- `i` (小写字母i) - 与大写字母 I 和数字 1 相似
- `l` (小写字母l) - 与数字 1 和大写字母 I 相似
- `o` (小写字母o) - 与数字 0 相似

**当前默认字符集：**

- 数字：2, 3, 4, 5, 6, 7, 8, 9
- 大写字母：A, B, C, D, E, F, G, H, J, K, M, N, P, Q, R, S, T, U, V, W, X, Y, Z
- 小写字母：a, b, c, d, e, f, g, h, j, k, m, n, p, q, r, s, t, u, v, w, x, y, z

### 7.2.编程方式自定义

```java
// 设置自定义字符集（仅大写字母和数字）
Randoms.setCustomAlpha("23456789ABCDEFGHJKMNPQRSTUVWXYZ");

// 或使用字符数组
char[] customChars = {'A', 'B', 'C', 'D', 'E', 'F', '2', '3', '4', '5'};
Randoms.

setCustomAlpha(customChars);

// 重置为默认字符集
Randoms.

resetToDefault();

// 获取当前字符集
char[] current = Randoms.getCurrentAlpha();
```

### 7.3.配置文件方式自定义

在 `application.yml` 中配置：

```yaml
captcha:
  len: 4                    # 验证码长度
  width: 130                # 验证码宽度
  height: 48                # 验证码高度
  custom-characters: 23456789ABCDEFGHJKMNPQRSTUVWXYZ  # 自定义字符集
```

或在 `application.properties` 中配置：

```properties
captcha.len=4
captcha.width=130
captcha.height=48
captcha.custom-characters=23456789ABCDEFGHJKMNPQRSTUVWXYZ
```

---

## 8.自定义效果

&emsp;继承`Captcha`实现`out`方法，中文验证码可继承`ChineseCaptchaAbstract`，算术验证码可继承`ArithmeticCaptchaAbstract`。

---

## 9.致谢

本项目 fork 源至EasyCaptcha ，  [ele-admin/EasyCaptcha](https://github.com/ele-admin/EasyCaptcha)
