

####编码风格

*   按照`google-java`.
*   控件变量名:
    Button btnSave
    TextView tvName
    ImageView imgvPortrait
    ImageButton imgBtnPortrait
    LinearLayout lLayoutTitle
    RelativeLayout rLayoutTitle
    ListView lvContent
    GridView gvContent
    ViewPager vpContent
    ...
    这样直观,一看就知道什么控件.
    驼峰式.

*   变量初始值 String类型最好赋值`""`
    
*   Activity,Fragment类命名
    xxxActivity
    xxxFragment

*   对象一定要**非空**检验,强转前一定要(instanceof)校验.

*   频繁使用的临时字符串.转化成`static final`

*   严格按照`MVP`模式编码.为后续维护铺路.

*   [ListView,GridView中的view的onClickListener优化] [https://www.zybuluo.com/eastern/note/206715]