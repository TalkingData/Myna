## TalkingData Myna 集成文档

![](http://p1.bqimg.com/562611/952bd822efce378b.png)

### 下载

使用 `git` 命令或者熟悉的 `git` 客户端将 Myna 克隆到本地：

	git clone https://github.com/TalkingData/Myna.git

### Demo App

Myna 项目中包含一个测试 Demo 工程：demo-myna, 将该工程和 Myna 项目本身导入到 Android Studio 中，就可以开始调试了。

Myna 提供了两套接口：

- 面向开发者的接口：开发者只需要简单的接口调用，就能在应用程序中获取实时识别的用户行为状态。
- 面向数据科学家的接口：数据科学家可以很方便地添加新的识别算法，在运行时调整订阅的传感器类型、采样频率和采样时长，而无需关心 Android 系统相关的传感器数据订阅细节。

### Myna 和 Google Awareness API

[Google 在 I/O 2016 大会上正式向开发者介绍了 Awareness API](https://events.google.com/io2016/schedule?sid=692d2aeb-0bef-e511-a517-00155d5066d7#day1/692d2aeb-0bef-e511-a517-00155d5066d7):

> A unified sensing platform enabling applications to be aware of multiple aspects of a users context, while managing battery and memory health.

Google 将 Google Play Service 中和用户场景识别相关的服务和功能整合在一个统一的 API 下，为开发者从兼顾内存占用和电量消耗方面提供更高效率的方案。

我们可以通过 `com.google.android.gms.awareness.Awareness.SnapshotApi.getDetectedActivity` 方法获取最后一次获取到的用户行为。Myna 兼容 Awareness API，开发者可以在初始化的时候选择使用 Awareness API 或者 Myna 的识别算法，当 Myna 检测到当前运行的设备不支持 Google Play Service 的时候，会自动切换到 Myna 的识别算法。

### 面向开发者的接口快读集成

#### 初始化

在应用自定义的 `Application` 派生类或者某个 `Activity` 的 `onCreate` 方法中调用下面的接口进行初始化：

	@Override
    public void onCreate() {
        super.onCreate();
        context = this;
        MynaApi.init(this, new MyInitCallback(), new MyCallback(), MynaApi.TALKINGDATA);
    }

初始化的时候，需要传入一个实现了接口 `MynaInitCallbacks` 的类的实例作为回调，这样将可以在 Myna 初始化成功或者失败时做不同的处理。接口 `MynaInitCallbacks` 的定义为：

	/**
	 * Define resultCallback methods to handle different initialization results.
	 */
	public interface MynaInitCallback {
	
	    /**
	     * Called when Myna is successfully initialized.
	     */
	    void onSucceeded();
	
	    /**
	     * Called when Myna failed to initialize.
	     */
	    void onFailed(MynaResult error);
	}

`MynaResultCallback` 用来返回识别结果：
	
	public interface MynaResultCallback<R extends MynaResultInterface> {
    void onResult(@NonNull R var1);
	}

通过下面的接口可以获取 Myna 的初始化状态：

	/**
     * Get the status of Myna initialization
     */
    public static boolean isInitialized()

#### 开始和停止

初始化后，就可以调用 `start` 和 `stop` 接口接收和停止识别算法的运行并获得识别结果。

    /**
     * Stop all background tasks
     */
    public static void stop(){
        MynaHelper.stop();
    }

    /**
     * Start to recognize
     */
    public static void start(){
        MynaHelper.start();
    }

### 使用 Google Awareness API

如果希望使用 Google Awareness API 提供的实时行为识别能力，在初始化时：

	MynaApi.init(this, new MyInitCallback(), new MyCallback(), MynaApi.GOOGLE);

需要注意的是，这是还需要额外进行下面的配置：

- 参考 [Android 开发者需要知道的 Google Awareness API](http://mp.weixin.qq.com/s?__biz=MjM5NzQ3NDg0Mg==&mid=2653096725&idx=1&sn=f6686df351aabe957a450c2fa1b01596&mpshare=1&scene=1&srcid=10311xdjSUJveY5gTtuLLQw2#rd) 文章申请 App key 并创建用于自己应用的 Credential。
- 在 `AndroidManifest.xml` 的 `application` 块中添加下面的声明：

		<meta-data
            android:name="com.google.android.awareness.API_KEY"
            android:value="申请的 App Key"/>
		
- 添加下面的 Permission：

		<uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />

- 在应用 module 的 `build.gradle` 文件中，添加下面的依赖：

		compile 'com.google.android.gms:play-services-awareness:9.8.0'

### 面向数据科学家的接口

`DataScientistAPI` 中定义了更多高级接口。

`MynaRecognizerInterface` 类型中定义了对订阅的传感器类型进行控制的接口。

#### 添加新的传感器类型订阅：

	/**
     * Add a sensor into the chosen sensor list.
     * @param sensorType sensorType
     */
    public synchronized void addSensorType(int sensorType)

#### 移除已经订阅的传感器类型：

	/**
     * Remove a sensor from the chosen sensor list.
     * @param sensorType The type of the sensor to be removed.
     */
    public synchronized void removeSensorType(int sensorType)

#### 设置采样的间隔时间（反映采样频率，单位毫秒）：

	/**
     * Set sampling duration.
     * @param duration Sampling duration
     */
    public void setSamplingDuration(int duration)

##### 设置 batch size：

	/**
     * Set total count of the data points for each recognition.
     * @param pointCount Total count of the data points.
     */
    public void setSamplingPointCount

Myna 支持同时配置多个识别器（MynaRecognizerInterface 派生类的实例），也就是 `Recognizer`，而且，在运行时，可以随时移除已经添加的某个 recognizer config：

	/**
     * Add a new recognition configuration to be executed later
     */
    public static void addRecognizer(MynaRecognizerInterface recognizer)

	/**
     * Remove a new recognition configuration to be executed later
     */
    public static void removeRecognizer(int configId)

如果想完全重置 Myna 运行环境，需要运行：

	/**
     * Clean Myna env
     */
    public static void cleanUp(Context ctx)

清理环境后，再次启动 Myna 需要重新初始化。

### 面向开发者的接口快读集成

#### 初始化

在应用自定义的 `Application` 派生类或者某个 `Activity` 的 `onCreate` 方法中调用下面的接口进行初始化：

	@Override
    public void onCreate() {
        super.onCreate();
        context = this;
        MynaApi.init(this, new MyInitCallback(), new MyCallback(), MynaApi.TALKINGDATA);
    }

初始化的时候，需要传入一个实现了接口 `MynaInitCallbacks` 的类的实例作为回调，这样将可以在 Myna 初始化成功或者失败时做不同的处理。接口 `MynaInitCallbacks` 的定义为：

	/**
 	* Define callback methods to handle different initialization results.
 	*/
	public interface MynaInitCallbacks {

	    /**
	     * Called when Myna is successfully initialized.
	     */
	    void onSucceeded();
	
	    /**
	     * Called when Myna failed to initialize.
	     */
	    void onFailed();	
	}

通过下面的接口可以获取 Myna 的初始化状态：

	/**
     * Get the status of Myna initialization
     */
    public static boolean isInitialized(){
        return isInitialized;
    }
