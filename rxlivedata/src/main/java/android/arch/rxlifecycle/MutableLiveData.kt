package android.arch.rxlifecycle

/**
 * Created by 张宇 on 2018/3/19.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
open class MutableLiveData<DATA> : LiveData<DATA>() {

    public override fun setValue(value: DATA) {
        super.setValue(value)
    }
}