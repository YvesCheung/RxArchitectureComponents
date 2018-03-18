package android.arch.archsample

import android.util.Log

/**
 * Created by 张宇 on 2018/3/15.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
object LogUtil {

    @JvmStatic
    fun i(msg: () -> Any?) {
        Log.i("ArchSample", msg().toString())
    }

    @JvmStatic
    fun e(msg: () -> Any?) {
        Log.i("ArchSample", msg().toString())
    }

    @JvmStatic
    fun i(msg: String) {
        Log.i("ArchSample", msg)
    }

    @JvmStatic
    fun e(msg: String) {
        Log.i("ArchSample", msg)
    }
}