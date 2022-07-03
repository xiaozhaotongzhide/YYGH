### 项目背景

网上预约挂号是近年来开展的一项便民就医服务，旨在缓解看病难、挂号难的就医难题，许多患者为看一次病要跑很多次医院，最终还不一定能保证看得上医生。
项目用到的技术

##### 项目架构图

![YYGH架构图](http://cdn.zhaodapiaoliang.top/PicGo/YYGH%E6%9E%B6%E6%9E%84%E5%9B%BE.png)

项目笔记

做这个项目的时候写的笔记

[ 预约挂号项目](https://blog.csdn.net/weixin_53227758/category_11890539.html)

在开发的时候也遇到了不少bug,这里我记录下来也上传到博客上

[YYGH-BUG-01](https://blog.csdn.net/weixin_53227758/article/details/123940207)

[YYGH-BUG-02](https://blog.csdn.net/weixin_53227758/article/details/125267538)

[YYGH-BUG-03](https://blog.csdn.net/weixin_53227758/article/details/125358017)

[YYGH-BUG-04](https://blog.csdn.net/weixin_53227758/article/details/125497977)

GitHub项目地址：

java后台：https://github.com/xiaozhaotongzhide/YYGH

管理前端：https://github.com/xiaozhaotongzhide/yygh-admin

用户前端：https://github.com/xiaozhaotongzhide/yygh-site



### 项目细节

#### 后端技术

SpringBoot：spring的脚手架,cloud的基础

SpringCloud：

​							gateway:服务网关,位于nginx之后,主要负责微服务直接的请求转发

​							openfeign:HTTP 形式的 Rest [API](https://so.csdn.net/so/search?q=API&spm=1001.2101.3001.7020) 提供了非常简洁高效的 RPC 调用方式

​							nacos:注册中心,服务发现

mysql：开源的关系型数据库

mybatis-plus：2种方式操作数据

​							BaseMapper：集成简单增删改查的操作

​							xml配置文件：复杂的逻辑

redis：做缓存,支付二维码的失效

rabbitMQ：异步消息队列提高系统的高可用，创建订单的时候，订单模块通过mq调用医院模块给库存-1之后调用邮箱模块发送邮件

七牛云oss：云对象存储,存放了用户的认证证件

QQ邮箱SMTP：完成了邮箱模块,给用户发送邮件

Mongodb：非关系型数据库，主要存放了医院的信息和排班信息

微信登录：调用了微信开发者平台的api

Docker：用来部署了mongodb，redis，rabbitMQ

EasyExcel：用来操作EasyExcel表格



#### 前端技术

node：js运行环境

vue：渲染

Element-ui：Vue的ui库

nuxt：客户端渲染

npm：基于node的包管理器

ECharts：表格渲染



#### 医院管理系统

这个是我们管理员管理预约挂号系统的后台管理系统

![image-20220703174245545](http://cdn.zhaodapiaoliang.top/PicGo/image-20220703174245545.png)

后端管理页面的目录

医院设置管理

​	医院设置列表	hosp模块

​	医院设置添加	hosp模块

​	医院列表	hosp模块

数据管理

​	数据字典	cmn模块

用户管理

​	用户列表	user模块

​	认证审批列表	user模块

统计管理

​	预约统计	sta模块

​	支付统计	sta模块

#### 用户挂号系统

![image-20220703174230678](http://cdn.zhaodapiaoliang.top/PicGo/image-20220703174230678.png)



### 

### 需要完善的功能

我想为后台管理系统添加一个订单管理,在统计管理下面

![image-20220703180754448](http://cdn.zhaodapiaoliang.top/PicGo/image-20220703180754448.png)

主要是两个功能一个是查看订单列表,另一个在oss模块添加一个下载中心的功能。

查看订单列表这个功能简单。

下载中心的设计类似于这样会有一个导出的功能

![image-20220703181352728](C:\Users\86157\AppData\Roaming\Typora\typora-user-images\image-20220703181352728.png)

在导出之前有这样的查询,可以根据日期和医院名称下载对应的消费报表

![image-20220703181428979](C:\Users\86157\AppData\Roaming\Typora\typora-user-images\image-20220703181428979.png)

这里说一下我的思路

1.前端两个页面,一个是订单下载(负责控制下载)另一个是下载中心(展示下载下来的url)

2.建立一个yygh_oss的mysql库,里面建立一个download表,字段有id,医院id,开始日期,结束日期,文件url,status （这个status,0为还没有下载,1为已经下载,2为下载失败）

3.在order模块设置一个根据医院和开始结束时间的接口,建立一个feign方便oss调用。

4.oss模块建立一个接口,当条件下载模块请求这个接口的时候,oss向download_status添加一条记录status_设置为0

5.在oss模块的业务层中建立一个服务,当收到task模块的请求之后,就会查询status为0的,并根据这个调用order模块的接口得到订单数据

6.利用task模块每一分钟发送一次请求,oss模块收到请求之后,

7.oss模块将这些数据,利用EasyExcel变为一个xlsx文件,并利用Java.util.zip包中的ZipOutputStream 实现文件的压缩

8.oss将压缩后的文件上传到七牛云oss之上,得到文件url,并在download表中更新数据byid

9.前端下载中心可以通过请求获取下载中心的数据。



