

###CompositeSubscription
>   一定要注意这个Subscription集合类的使用,一旦unSubscribe之后.必须重新newCompositeSubscription();
 因为它的一个变量 unsubscribed =false,以后的所有订阅都不会成功.