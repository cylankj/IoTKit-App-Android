
###SharedElement提供一个全新的交互方式。

*   经过多番努力，终于看到成果。

>   `ImageView` 在 `Activity_A`中。 跳转到`Activity_B`中，跳转到`Fragment`中。
>   `ImageView` 在 `Fragment`中。 跳转到`Activity_B`中，跳转到`Fragment`中。

需要以下三个步骤。

>   1.创建一个`values-v21/styles.xml

```
<item name="android:windowEnterTransition">@transition/details_window_enter_transition</item>
<item name="android:windowReturnTransition">@transition/details_window_return_transition</item>

```

>   2.在`DetailFragment`的宿主`Activity`中使用该 theme(一般在Manifest中设置)


>   3.给`Activity_A`的`ImageView`设置transitionName

 ```
 //transitionName必须唯一。
 ViewCompat.setTransitionName(View view,String transitionName);
 ```
>   4.在`Activity_A`中

```
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);
        setExitSharedElementCallback(mCallback);
    }

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        }
    };
    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        super.onActivityReenter(requestCode, data);
        if (onActivityReenterListener != null)
           onActivityReenterListener.onActivityReenter(requestCode, data);
    }
//启动Activity_B
Intent intent = new Intent(getActivity(), MediaActivity.class);
// Pass data object in the bundle and populate details activity.
intent.putExtra("key", position);
getActivity().startActivity(intent,
   ActivityOptions.makeSceneTransitionAnimation(getActivity(),
   v,
   ViewCompat.getTransitionName(v)).toBundle());

```

>   5.`Activity_B`中设置

 ```
     @TargetApi(Build.VERSION_CODES.LOLLIPOP)
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_media);
         postponeEnterTransition();
         setEnterSharedElementCallback(mCallback);

     }

     @Override
     public void finishAfterTransition() {
         mIsReturning = true;
         Intent data = new Intent();
         data.putExtra(JConstant.EXTRA_STARTING_ALBUM_POSITION, mStartingPosition);
         data.putExtra(JConstant.EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
         setResult(RESULT_OK, data);
         super.finishAfterTransition();
     }

     private final SharedElementCallback mCallback = new SharedElementCallback() {
             @Override
             public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                 AppLogger.d("transition:mIsReturning " + mIsReturning);
                 AppLogger.d("transition:mStartingPosition " + mStartingPosition);
                 AppLogger.d("transition:mCurrentPosition " + mCurrentPosition);
                 if (mIsReturning) {
                     ImageView sharedElement = mCurrentDetailsFragment.getAlbumImage();
                     if (sharedElement == null) {
                         // If shared element is null, then it has been scrolled off screen and
                         // no longer visible. In this case we cancel the shared element transition by
                         // removing the shared element from the shared elements map.
                         names.clear();
                         sharedElements.clear();
                     } else if (mStartingPosition != mCurrentPosition) {
                         // If the user has swiped to a different ViewPager page, then we need to
                         // remove the old shared element and replace it with the new shared element
                         // that should be transitioned instead.
                         final String transitionName = sharedElement.getTransitionName();
                         AppLogger.d("transition:transitionName " + transitionName);
                         names.clear();
                         names.add(transitionName);
                         sharedElements.clear();
                         sharedElements.put(transitionName, sharedElement);
                     }
                 }
             }
         };

 ```

>   6.在