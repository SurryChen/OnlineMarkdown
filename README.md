## OnlineMarkdown----支持多种模块式语句导入的在线实时多人协作Markdown编辑平台

### 简介：
----
+ 简单易用Markdown在线协作编辑平台

### 相关技术：
---
+ SpringBoot，MybatisPlus，Redis，MySQL

### 技术要点：
---
+ 使用**Spring Boot**搭建简易后台，进行**事务管理**、**全局异常处理**
+ 使用**JWT与拦截器**搭配**Redis**做**登录状态的验证与刷新**，并将验证与刷新通过拦截器链**排序**，避免了部分访问未刷新状态的问题，同时解决因前后端分离导致的**跨域问题**
+ 使用**反射**实现Bean以Map形式存储在**Redis**中，以及获取时的Redis中Map类型对于**Bean的映射**，并构造了部分数据类型与Redis储存类型的**映射器**
+ 使用**树的数据结构**设计文件目录的构成，在**Redis**中维护多个文件目录Set集合，大大提升了目录切换的速度