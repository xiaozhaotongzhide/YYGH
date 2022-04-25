[toc]

### 1、websocket

#### 什么是websocket？

WebSocket协议是基于TCP的一种新的网络协议。它实现了客户端与服务器全双工通信，学过计算机网络都知道，既然是全双工，就说明了服务器可以主动发送信息给客户端。这与我们的推送技术或者是多人在线聊天的功能不谋而合。

![image-20220424162957940](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220424162957940.png)

为什么不使用HTTP 协议呢？这是因为HTTP是单工通信，通信只能由客户端发起，客户端请求一下，服务器处理一下，这就太麻烦了。于是websocket应运而生。

![image-20220424163024880](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220424163024880.png)

下面我们就直接开始使用Springboot开始整合。以下案例都在我自己的电脑上测试成功，你可以根据自己的功能进行修改即可。

现在我们的需求是根据这个特点,实现一个多个人的聊天室

这是我们的项目结构

![image-20220424172712574](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220424172712574.png)

#### 使用步骤

##### 1.引入依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

##### 2.建立配置类

```java
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

##### 3.业务层

因为代码比较多,核心代码为

```java
/**
 * 建立成功连接调用的方法
 */
@OnOpen
public void onOpen(Session session, @PathParam("sid") String sid) {
    this.session = session;
    //如果有重复直接返回
    for (WebSocketServer item : webSocketSet) {
        if (Objects.equals(item.sid, sid)) {
            return;
        }
    }
    webSocketSet.add(this);         //加入set中
    this.sid = sid;
    addOnlineCount();               //在线人数加1
    try {
        sendMessage("conn_success");
        log.info("有新窗口开始监听:" + sid + "当前在线人数为:" + getOnlineCount());
    } catch (IOException e) {
        log.error("websocket IO Exception");
    }
}

/**
 * 收到客户端调用的方法
 */
@OnMessage
public void onMessage(String message, Session session) {
    log.info("来自窗口:" + sid + "的消息" + message);
    //群发消息
    for (WebSocketServer item : webSocketSet) {
        try {
            item.sendMessage("来自窗口:" + sid + "的消息" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public static void sendInfo(String message) {
    for (WebSocketServer item : webSocketSet) {
        try {
            log.info("推送消息到窗口:" + item.sid + ",推送内容为:" + message);
            item.sendMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

因为我们要建立多人的聊天室,那必须要区分,不同客户端的类型,于是我想到了可以用Web中的路径参数每一个路径参数相当于一个id,之后通过thymeleaf的渲染,使他的路径参数最终变化为WebSocket。

##### Web类

```java
//页面请求
@GetMapping("/index/{user}")
public String socket(@PathVariable String user, Model model) {
    model.addAttribute("user",user);
    return "index";
}
```

##### HTML

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <title>Java后端WebSocket的Tomcat实现</title>
    <script type="text/javascript" src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
<button onclick="create(value)" th:attr="value = ${user}">连接Socket</button>
<br/>
Welcome
<br/><input id="text" type="text"/>

<button onclick="send()">发送消息</button>
<hr/>
<button onclick="closeWebSocket()">关闭WebSocket连接</button>
<hr/>
<div id="message"></div>
</body>
<script type="text/javascript">
    function create(user) {
        //判断当前浏览器是否支持WebSocket
        if ('WebSocket' in window) {
            //改成你的地址
            if (user == null) {
                websocket = new WebSocket("ws://localhost:8080/api/websocket/101");
                init(websocket)
            } else {
                websocket = new WebSocket("ws://localhost:8080/api/websocket/" + user);
                init(websocket)
            }
            setMessageInnerHTML(user + "连接成功");
            return websocket;
        } else {
            alert('当前浏览器 Not support websocket')
        }
    }

    function init(websocket){
        //连接发生错误的回调方法
        websocket.onerror = function () {
            setMessageInnerHTML("WebSocket连接发生错误");
        };

        //连接成功建立的回调方法
        websocket.onopen = function () {
            setMessageInnerHTML("WebSocket连接成功");
        }
        //接收到消息的回调方法
        websocket.onmessage = function (event) {
            console.log(event);
            setMessageInnerHTML(event.data);
        }

        //连接关闭的回调方法
        websocket.onclose = function () {
            setMessageInnerHTML("WebSocket连接关闭");
        }

        //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
        window.onbeforeunload = function () {
            closeWebSocket();
        }
    }

    //将消息显示在网页上
    function setMessageInnerHTML(innerHTML) {
        document.getElementById('message').innerHTML += innerHTML + '<br/>';
    }

    //关闭WebSocket连接
    function closeWebSocket() {
        websocket.close();
    }

    //发送消息
    function send() {
        var message = document.getElementById('text').value;
        websocket.send('{"msg":"' + message + '"}');
    }
</script>
</html>
```

![image-20220424174125326](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220424174125326.png)

![image-20220424174111899](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220424174111899.png)

#### 遇到的问题

首先一开始直接var的websocket之后调用init在create方法外,一直报错找不到websocket,之后定位错误是websocket为非全局函数