以下参考其他文章.
##glide简介:
在泰国举行的谷歌开发者论坛上，谷歌介绍,作者是bumptech,源码https://github.com/bumptech/glide

##导入库
repositories {
  mavenCentral() // jcenter() works as well because it pulls from Maven Central
}

dependencies {
  compile 'com.github.bumptech.glide:glide:3.7.0'
  compile 'com.android.support:support-v4:19.1.0'   //Glide 需要 Android Support Library v4 包
}

##使用
```java
Glide.with(context)
    .load("http://inthecheesefactory.com/uploads/source/glidepicasso/cover.jpg")
    .into(ivImg);

*Glide 的 with() 方法不光接受 Context，还接受 Activity 和 Fragment。with() 方法能还自动地从你放入的各种东西里面提取出 Context，供它自己使用```
  建议参数为Activity 和 Fragment,因为Glide和Activity/Fragment的生命周期是一致的，gif动画(或下载静态图片)也会自动的随着Activity/Fragment的状态暂停、重放。Glide 的缓存在gif这里也是一样，调整大小然后缓存.
*除了gif动画之外(测试显示用 Glide 显示动画会消耗很多内存，因此谨慎使用)，Glide还可以将任意本地视频解码成一张静态图片。
*还有一个特性是你可以配置图片显示的动画.
*最后一个是可以使用thumbnail()产生一个你所加载图片的thumbnail。
*其实还有一些特性，不过不是非常重要，比如将图像转换成字节数组等。
*配置
 有许多可以配置的选项，比如大小，缓存的磁盘位置，最大缓存空间，位图格式等等。可以在这个页面查看这些配置 Configuration 。

##图片格式
*Glide 默认Bitmap格式是RGB_565,这样比ARGB_8888内存开销小一半
*如果你要调整图片的默认格式ARGB_8888,将一个像下面的GlideModule子类.
```java
 public class GlideConfiguration implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Apply options to the builder here.
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        // register ModelLoaders here.
    }
}
```
```
然后在AndroidManifest.xml中将GlideModule定义为meta-data

```java
<meta-data android:name="com.inthecheesefactory.lab.glidepicasso.GlideConfiguration"
            android:value="GlideModule"/>
```
##磁盘缓存
 * Glide 缓存的图片和 ImageView 的尺寸相同,如果有多个地方ImageView尺寸不同,会有多次下载,多个缓存尺寸
   比如:具体说来就是：假如在第一个页面有一个200x200的ImageView，在第二个页面有一个100x100的ImageView，这两个ImageView本来是要显示同一张图片，却需要下载两次
   如果同一张图需要多个尺寸,你可以，让Glide既缓存全尺寸又缓存其他尺寸,下次在任何ImageView中加载图片的时候，全尺寸的图片将从缓存中取出，重新调整大小，然后缓存
 ```java
 Glide.with(this)
      .load("http://nuuneoi.com/uploads/source/playstore/cover.jpg")
      .diskCacheStrategy(DiskCacheStrategy.ALL)
      .into(ivImgGlide);
 ```
 *设置占位图或者加载错误图：
 ```java
Glide
 .placeholder(R.drawable.placeholder)
 .error(R.drawable.imagenotfound)
 ```
##顺便提下,
Glide (v3.5.2)的大小约430kb,方法个数2678个(比较大了)