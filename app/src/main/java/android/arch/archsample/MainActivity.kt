package android.arch.archsample

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProviders.of(this).get(MyViewModel::class.java) }

    lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.lifecycleEvent.setValue("onCreate")
        disposable = viewModel.lifecycleEvent.subscribe({
            Log.i("zyclife", "life $it")
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.lifecycleEvent.setValue("onStart")
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.lifecycleEvent.setValue("onRestart")
    }

    override fun onResume() {
        super.onResume()
        viewModel.lifecycleEvent.setValue("onResume")
    }

    override fun onPause() {
        viewModel.lifecycleEvent.setValue("onPause")
        super.onPause()
    }

    override fun onStop() {
        viewModel.lifecycleEvent.setValue("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        disposable.dispose()
        viewModel.lifecycleEvent.setValue("onDestroy")
        super.onDestroy()
    }
}
