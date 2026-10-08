# File Resources Web

可选的文件资源 MVC 适配。引入 `loadup-modules-file-web` 后，在 JDBC 与 DFS binder 可用时自动注册 `/api/files`。通过 `loadup.file.web.enabled: false` 关闭 HTTP 接口。

## 接口

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/api/files` | multipart 字段 `file` 上传；所有者取登录用户 ID |
| POST | `/api/files/list` | JSON 分页列出自己的文件；超级管理员可指定 `ownerId` |
| POST | `/api/files/detail` | JSON `{ "id": "..." }` 查询元数据 |
| GET | `/api/files/{id}/content` | 下载二进制附件 |
| POST | `/api/files/references` | JSON `{ "id": "..." }` 查看业务引用 |
| POST | `/api/files/delete` | JSON `{ "id": "..." }` 删除无引用文件 |
| POST | `/api/files/cleanup` | JSON `{ "limit": 100 }` 超级管理员重试待清理文件 |

JSON 接口沿用 WebMVC 统一 `{result, data}` 响应；文件内容保持二进制响应。文件读取与删除只允许文件所有者或 `ROLE_SUPER_ADMIN`。上传文件名不得含路径分隔符或控制换行。业务引用通过可信业务代码调用 `FileResourceService.attach/detach`，Web 不提供任意关联写入入口。

设计与依赖边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。
