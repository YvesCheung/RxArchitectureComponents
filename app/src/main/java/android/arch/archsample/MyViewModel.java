package android.arch.archsample;

import android.arch.lifecycle.ViewModel;

/**
 * Created by 张宇 on 2018/3/12.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */

public class MyViewModel extends ViewModel {

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtil.i("onCleared");
    }
}
