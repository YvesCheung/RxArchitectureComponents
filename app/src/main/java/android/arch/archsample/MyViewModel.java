package android.arch.archsample;

import android.arch.lifecycle.ViewModel;
import android.arch.rxlifecycle.LiveData;
import android.arch.rxlifecycle.MutableLiveData;
import android.util.Log;

/**
 * Created by 张宇 on 2018/3/12.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */

public class MyViewModel extends ViewModel {

    private final MyLiveData liveData1 = new MyLiveData("liveData1");
    private final MyLiveData liveData2 = new MyLiveData("liveData2");
    private final MyLiveData liveData3 = new MyLiveData("liveData3");

    public MyViewModel() {
        liveData1.setValue("data1");
        liveData2.setValue("data2");
    }

    public LiveData<CharSequence> getFragment1Text() {
        return liveData1;
    }

    public LiveData<CharSequence> getFragment2Text() {
        return liveData2;
    }

    public void postText1(CharSequence text) {
        liveData1.setValue(text);
    }

    public void postText2(CharSequence text) {
        liveData2.setValue(text);
    }

    public MutableLiveData<CharSequence> getLifecycleEvent() {
        return liveData3;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtil.i("onCleared");
    }

    private static class MyLiveData extends MutableLiveData<CharSequence> {

        private final String name;

        MyLiveData(String name) {
            this.name = name;
        }

        @Override
        public void onActive() {
            super.onActive();
            Log.i("zyc", "onActive " + name);
        }

        @Override
        public void onInactive() {
            super.onInactive();
            Log.i("zyc", "onInactive " + name);
        }
    }
}
