#### 关于某些测试功能.

*   情景

>   在debug版本,我想展示某些提示.在release版本不展示.但是又不想
```
if(BuildConfig.DEBUG){
xxx
}
```
这样看起来不但很丑,代码也很庞大.


>   好在万能的`gradle`提供了某些特性.
`testCompile 'junit'`//最常见,就是在test模式下,这个`junit`才发挥作用,并且不会污染`debug` `release` 版本的代码
不止`testCompile`,还有`debugCompile` `releaseCompile`

*   栗子

```java
//这段代码,用于debug版本
public class DebugOptionsImpl {

    private static final String TAG = "iDebugOptions";

    public void enableCrashHandler(Context context, String dir) {
        Log.d(TAG, "enableCrashHandler");
        CrashHandler.getInstance().init(context, dir);
    }
}    

```

```java
//这段代码,用于release版本
public class DebugOptionsImpl {

    private static final String TAG = "iDebugOptions";

    public void enableCrashHandler(Context context, String dir) {
        //Log.d(TAG, "enableCrashHandler");
        //CrashHandler.getInstance().init(context, dir);
    }
}    

```

>   上面两端代码分别打两个`jar`.
最后使用,如:
    debugCompile files('src/main/jniLibs/jfg-debug-support.jar')
    releaseCompile files('src/main/jniLibs/jfg-release-support.jar')
    
>   仔细想想,`android.os.Log`也是一样的道理.`Log.d`在`release`版本中就是一个空方法.

#### 程序员就是喜欢折腾的.不厌其烦,要是你想走捷径.


* 但是你可能想通过实现一个`Interface`的方式来实现,`debug`与`release`的区别.需要用到反射,不可取.