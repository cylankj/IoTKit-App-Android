采用mvp模块,抽取代码


命名:
       模块module:可以选择按包区分,或按照功能区分. 由于本应用已经是按包分块,所以尽可能按包分

       接口interface: Required 被要求,可认为是回调,Ops可认为是未实现方法接口
                            View : 功能+View+Required+Ops
                            Presenter: 功能+Presenter+Required+Ops  or 功能+Presenter+Ops
                            Model: 功能+model+Ops

       类class: Required 被要求,可认为是回调,Compl 可认为是实现了接口或抽象的具体类.
                            View : Activitiy Or Fregement
                            Presenter: 功能+Presenter+Compl  (此类实现以上两个接口功能+Presenter+Required+Ops  && 功能+Presenter+Ops)
                            Model: 功能+model+Compl

       方法method:
                            View         -> Presenter    动词+名词,表示执行任务
                            Presenter -> Model          动词+名词+da,表示model执行任务
                            Model      -> Presenter     on+名词+动词过去分词,表示事情实行完毕,回调
                            Presenter -> View             名字+动词过去分词,表示任务执行完毕,回调

        eg:
        public interface SplashPresenter {
            /***********
             * View -> Presenter
             */
            interface Ops {
                void initCache();
            }

            /************
             * Model -> Present <br/>
             *****/
            interface RequiredOps {
                void onCacheInited();
            }
        }

       public interface SplashViewRequiredOps {
            /**
              * Presenter -> View
              * */
            void cacheInited();
        }

       public interface SplashModelOps {
               /**
                * Present -> Model
                * */
               void initCacheda();
       }

       View:         public class SplashActivity extends Activity implements SplashViewRequiredOps {...}
       Presenter: public class SplashPresenterCompl implements SplashPresenter.Ops,SplashPresenter.RequiredOps {...}
       Model:      public class SplashModelCompl implements SplashModelOps {...}

逻辑图:
       以view初始缓存数据为例,参考https://raw.githubusercontent.com/DroidWorkerLYF/Translate/master/Model-View-Presenter%20(MVP)/1.png

                    @2 执行方法                    @7实现接口
                <--------          View             -------->
                |                                                              |
                |                                                              |
       <ingterface>PresenterOps                <interface>ViewRequiredOps
       实现View ->Presenter接口                   Presenter -> View
        void initCache()                                  void cacheInited();
                |                                                              |
                |  @1实现接口                     @8回调方法    |
                <-------                                -------->

                                        Presenter

                    @4执行方法                    @5实现接口
                <-------                                 -------->
                |                                                               |
                |                                                               |
        <interface>ModelOps                       <interface>PresenterRequiredOps
        实现Presenter -> Model接口              实现Model -> Presenter接口
        void initCacheda();                             void onCacheInited();
                |                                                               |
                |   @3实现接口                    @6回调方法     |
                <--------           Model           -------->



```java
//按照google官方教程,todo-mvp模板.建一个`XXXContract`类.接口,实现逻辑一目了然.
public interface NewHomeActivityContract {

    interface View extends BaseView {
        void sample();

        void ok();

        void onBackPress();
    }

    interface Presenter extends BasePresenter {
        void loadTable();

        void doSample();

    }
}

*********************************

