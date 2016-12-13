package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendScanAddContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.rx.RxBus;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendScanAddPresenterImp extends AbstractPresenter<MineFriendScanAddContract.View>
        implements MineFriendScanAddContract.Presenter {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private CompositeSubscription compositeSubscription;

    public MineFriendScanAddPresenterImp(MineFriendScanAddContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(beginScan());
            compositeSubscription.add(getUserInfo());
            compositeSubscription.add(checkAccountCallBack());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    public Bitmap encodeAsBitmap(String contents, int dimension) {

        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        BitMatrix result = null;
        try {
            result = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, dimension, dimension, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    @Override
    public int getDimension() {
        WindowManager manager = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        int width = displaySize.x;
        int height = displaySize.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 7 / 8;
        return smallerDimension;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }


    /**
     * 检测扫描结果
     * @param account
     */
    @Override
    public void checkScanAccount(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            JfgCmdInsurance.getCmd().checkFriendAccount(s);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("checkScanAccount"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 扫描结果的回调
     * @return
     */
    @Override
    public Subscription checkAccountCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null && checkAccountCallback instanceof RxEvent.CheckAccountCallback){
                            handlerCheckResult(checkAccountCallback);
                        }
                    }
                });
    }

    /**
     * 获取到用户的信息用于产生二维码
     * @return
     */
    @Override
    public Subscription getUserInfo() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo){
                            if (getView() != null){
                                getView().showQrCode(encodeAsBitmap(getUserInfo.jfgAccount.getAccount(),getDimension()));
                            }
                        }
                    }
                });
    }

    /**
     * 开始扫描
     * @return
     */
    @Override
    public Subscription beginScan() {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().onStartScan();
                    }
                });
    }

    /**
     * 处理检测的结果
     * @param checkAccountCallback
     */
    private void handlerCheckResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (getView() != null){
            getView().hideLoadingPro();
            if (checkAccountCallback.i == 0){
                // 已注册
                MineAddReqBean resutBean = new MineAddReqBean();
                resutBean.account = checkAccountCallback.s;
                resutBean.alias = checkAccountCallback.s1;
                getView().jump2FriendDetailFragment(false,resutBean);
            }else if (checkAccountCallback.i == 241){
                // 已经是好友了
                getView().isMineFriendResult();
            } else{
                // 未注册
                getView().scanNoResult();
            }
        }
    }

}
