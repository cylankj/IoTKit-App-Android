####官方文档说明：
[链接](https://developer.android.com/studio/test/index.html)

*   Test Types and Location (测试种类和测试类的位置)

>   Local unit tests(本地单元测试)

位于`Located at module-name/src/test/java/.`

这些测试跑在本地机器的JVM环境中，这种方式可以很快速(取决于你的机器)。
运行过程中，测试用例可以使用例如`Mockito`库来`mock`一个`andriod.jar`中的任何一个对象实例。

```
dependencies {
    // Required for local unit tests (JUnit 4 framework)
    testCompile 'junit:junit:4.12'
}

```


>   Instrumented tests

位于： `module-name/src/androidTest/java/.`
依赖真机或者模拟器，跑在`android`环境中，可以调用` Instrumentation` api来辅助测试。

```
dependencies {
    // Required for instrumented tests
    androidTestCompile 'com.android.support:support-annotations:24.0.0'
    androidTestCompile 'com.android.support.test:runner:0.5'
}

```


*   搭建`build variant`相关的测试。
|------|----------------|
|------|----------------|
|Path to app class |Path to matching instrumentation test class|
|src/main/java/Foo.java 	|src/androidTest/java/AndroidFooTest.java|
|src/myFlavor/java/Foo.java |	src/androidTestMyFlavor/java/AndroidFooTest.java|




[建立测试环境——教你在Android Studio下建立测试环境](http://2dxgujun.com/post/2014/10/01/Testing-Your-Activity.html#anchor_1)
[创建和运行一个测试用例——教你创建一个Activity测试用例，并用Instrumentation测试器来执行](http://2dxgujun.com/post/2014/10/01/Testing-Your-Activity.html#anchor_2)
[测试UI组件——教你测试Activity中特定UI组件](http://2dxgujun.com/post/2014/10/01/Testing-Your-Activity.html#anchor_3)
[创建单元测试——教你在封闭状态下对Activity进行测试](http://2dxgujun.com/post/2014/10/01/Testing-Your-Activity.html#anchor_4)
[创建功能测试——教你创建一个功能性测试，并用来测试多个Activity间的交互](http://2dxgujun.com/post/2014/10/01/Testing-Your-Activity.html#anchor_5)
