#### 最新版本
模块|abridge|
---|---
最新版本|[![Download](https://api.bintray.com/packages/iyifei/maven/abridge/images/download.svg)](https://bintray.com/iyifei/maven/abridge/_latestVersion)

更新说明：为了让用户能更灵活的使用ABridge进行进程间的通信，且不在局限于Activity使用场景，1.0.0版本做了全面的改进，可方便用户在进程中任何地方和另一个进程进行通信，同时也不在支持0.0.1的用法，给用户带来的不便尽请谅解。老版本用户可根据新版本的用法做简单的改动就可以升级上来。

>Android 进程间通信最牛方案，为简单而生

### Github 源码: [ABridge](https://github.com/yifei8/ABridge)

## 一、介绍
做Android开发的小伙伴们是不是经常有遇到同一个公司有多个App，而这些App之间需要进行通信业务。于是需要解决这种IPC问题，而ABridge可轻松解决进程间通信问题。

## 二、Android IPC方式
跨进程常见的几种通信方式：Bundle通过Intent传递数据，文件共享，ContentProvider，基于Binder的AIDL和Messenger以及Socket。

## 三、IPC是what?
也许有些小伙伴还不是很清楚IPC概念，这里我简单的概述一下。

IPC是 Inter-Process Communication的缩写，意为进程间通信或跨进程通信，是指两个进程之间进行数据交换的过程。

线程是CPU调度的最小单元，同时线程是一种有限的系统资源。进程一般指一个执行单元，在PC和移动设备上指一个程序或者一个应用。一个进程可以包含多个线程，因此进程和线程是包含与被包含的关系。最简单的情况下，一个进程中只可以有一个线程，即主线程，在Android中也叫UI线程。

IPC不是Android中所独有的，任何一个操作系统都需要相应的IPC机制，比如Windows上可以通过剪贴板等来进行进程间通信。Android是一种基于Linux内核的移动操作系统，它的进程间通信方式并不能完全继承自Linux，它有自己的进程间通信方式。

## 四、Why ABridge
在使用ABridge之前，我们可以通过上面的方式来实现IPC，但这些方式实现过程繁琐，学习成本较高。为此，ABridge诞生了——一款可以几行代码轻松实现跨进程通信框架。

ABridge提供了两种方案进行跨进程来满足不同场景的业务需求：一种是基于Messenger，另一种是基于AIDL。当然Messenger本质也是AIDL，只是进行了封装，开发的时候不用再写.aidl文件。

## 五、新版本基本用法
### step1 添加依赖
```gradle
    api "com.sjtu.yifei:abridge:_latestVersion"
```
### step2 初始化
```java
  public class MainApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        //注意这里的packagename，需要通信的多个app只能使用一个packagename
        //ABridge初始化有两种方式
        //方式一:基于Messenger
        IBridge.init(this, "com.sjtu.yifei.aidlserver", IBridge.AbridgeType.MESSENGER);
        //方式二:基于自定义的AIDL
        IBridge.init(this, "com.sjtu.yifei.aidlserver", IBridge.AbridgeType.AIDL);
    }

    @Override
    public void onTerminate() {
        //注意释放
        IBridge.recycle();
        super.onTerminate();
    }
  }
```
  
### step3 通信使用
#### 3.1 基于Messenger IPC
```java
 // 1 注册回调
 IBridge.registerMessengerCallBack(callBack = new AbridgeMessengerCallBack() {
            @Override
            public void receiveMessage(Message message) {
                if (message.arg1 == ACTIVITYID) {
                    //todo客户端接受服务端传来的消息
                    
                }
            }
        });
        
// 2 反注册回调，避免内存泄漏
IBridge.uRegisterMessengerCallBack(callBack);

// 3 发送消息
 Message message = Message.obtain();
 message.arg1 = ACTIVITYID;
 //注意这里，把`Activity`的`Messenger`赋值给了`message`中，当然可能你已经发现这个就是`Service`中我们调用的`msg.replyTo`了。
 Bundle bundle = new Bundle();
 bundle.putString("content", messageStr);
 message.setData(bundle);
 IBridge.sendMessengerMessage(message);
```
#### 3.2 基于AIDL IPC
```java
 // 1 注册回调
 IBridge.registerAIDLCallBack(callBack = new AbridgeCallBack() {
            @Override
            public void receiveMessage(String message) {
                //todo客户端接受服务端传来的消息
            }
        });
        
// 2 反注册回调，避免内存泄漏
IBridge.uRegisterAIDLCallBack(callBack);

// 3 发送消息
String message = "待发送消息";
IBridge.sendAIDLMessage(message);
```

## 注、版本更新到1.0.0以后将弃用~~0.0.1版本基本用法~~以下是老版本用法
- 方案一：基于Messenger
  ### step1 添加依赖
  ```java
    api "com.sjtu.yifei:abridge:xxx.xxx.xxx"
  ```
  ### step2 初始化
  ```java
  public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //注意这里的packagename，需要通信的多个app只能使用一个packagename
        //即使用一个app作为server启动这个共享服务来进行通信
        IBridge.init(this, "packagename");
    }
  }
  ```
  ### step3 需要通信的activity实现接口 MessengerReceiver
  ```java
  public class TestMessengerActivity extends AppCompatActivity implements MessengerReceiver {
   //跨进程通信的 发送器
   private MessengerSender sender;

   //MessengerReceiver 接口方法 设置发送器
   @Override
    public void setSender(MessengerSender sender) {
        this.sender = sender;
    }
   
    //MessengerReceiver 接口方法  接受跨进程发送过来的message
    @Override
    public void receiveMessage(Message message) {
        if (message.arg1 == ACTIVITYID) {
            //客户端接受服务端传来的消息
            String str = (String) message.getData().get("content");
            tv_show_in_message.setText(str);
        }
    }
  }
  ```
  ### step4 跨进程发送信息
  ```java
   //接上文
    public void sendMessage(String messageStr)
            Message message = Message.obtain();
            message.arg1 = ACTIVITYID;
            Bundle bundle = new Bundle();
            bundle.putString("content", messageStr);
            message.setData(bundle);
            sender.sendMessage(message);
    }
  ```
- 方案二：基于AIDL
  ### step1 添加依赖
  ```java
    api "com.sjtu.yifei:abridge:xxx.xxx.xxx"
  ```
  ### step2 初始化
  ```java
  public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //注意这里的packagename，需要通信的多个app只能使用一个packagename
        //即使用一个app作为server启动这个共享服务来进行通信
        IBridge.init(this, "packagename");
    }
  }
  ```
  ### step3 需要通信的activity实现接口 MessengerReceiver
  ```java
  public class TestAIDLActivity extends AppCompatActivity implements IReceiver {
   //跨进程通信的 发送器
   private ISender sender;

   //IReceiver 接口方法 设置发送器
  @Override
    public void setSender(ISender sender) {
        this.send = sender;
    }

   //IReceiver 接口方法  接受跨进程发送过来的message
    @Override
    public void receiveMessage(String message) {
        tv_user.setText(message);
    }
 
  }
  ```
  ### step4 跨进程发送信息
  ```java
   //接上文
    public void sendMessage(String messageStr)
        send.sendMessage(messageStr);
    }
  ```
## 六、沟通
#### qq群

![qq交流群.jpeg](https://user-gold-cdn.xitu.io/2018/6/5/163cea15e497ee16?w=200&h=274&f=jpeg&s=15655)

#### Email
yifei8@gmail.com

644912187@qq.com
## 七、欢迎 fork、issues
