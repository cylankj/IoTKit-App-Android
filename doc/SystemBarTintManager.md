

#### 状态栏，导航栏颜色适配。


*  API>19(Build.VERSION_CODES.KITKAT),才有`immersive`特性。


>   1.在对应的activity_layout,最外层。
    
```
//可设置为true查看效果（layout被覆盖）
    android:clipToPadding="false"
    android:fitsSystemWindows="true"
```
    
>   2.手动设置


```
        @TargetApi(19)
        private void setTranslucentStatus(boolean on) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
```

>   3.使用`SystemBarTintManager`