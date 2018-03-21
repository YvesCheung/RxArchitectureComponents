# 无需support-26包的Android Architecture Component解决方案

---

## 背景

Google官方推出了 [ViewModel][1] 和 [LiveData][2] 等非常实用的组件工具,可以帮助我们：

* 避免内存泄漏。异步操作可以与 ``Activity`` 或者 ``Fragment`` 的生命周期绑定，在 ``Activity`` 或者 ``Fragment`` 销毁的时候自动取消异步操作。而且也不用遇到 *IllegalStateException:Activity has been destroyed* 等异常。
* 业务逻辑与UI解耦。之所以大量业务逻辑堆砌在 ``Activity`` 或者 ``Fragment`` 上，是因为它们具有 ``Context`` 和生命周期两种业务所必须的元素。现在我们可以把数据部分的逻辑放到 ``ViewModel`` 中，可以使得数据不会因为 ``Activity`` 重新建立而丢失（旋转或其他配置变化、内存不足被回收等）。而如果是通过 ``onSaveInstance`` 这种方式保存数据的话，一来需要实现 ``Parcelable`` 接口，二来数据量太大会造成 *TransactionTooLargeException* 。所以 ``ViewModel`` 是维护从网络或者数据库获取数据最好的地方。但是，并不是所有业务都能写在 ``ViewModel`` 上。 ``ViewModel`` 禁止持有 ``Application`` 以外的 ``Context`` ，所以个人认为还是需要一个Presenter层来完成其他业务。
* 同个业务的不同组件共享数据。一个 ``Activity`` 多个 ``Fragment`` 是现在推崇的结构。同一个业务可能会实现在不同的 ``Fragment`` 上，这些 ``Fragment`` 都可以获取到同一个 ``ViewModel``，从而实现数据的共享和交互。相比起 **RxBus** 或 **EventBus** 来说，业务内更加高内聚，逻辑更清晰，更容易维护。

## 目的

**Architecture Component** 必须要依赖 **Android support Library v26** 以上的库。对于我们的大型老项目，由于插件化等原因，暂时还无法升级到26版本 **support library**，使得这套神器就要失之交臂。为了曲线救国，所以选择了对 **Android Architecture Component** 修改使其不需要 **support-26** 包也能使用的方案：

## 对ViewModel修改

``ViewModel`` 之所以能绑定 ``Activity`` 或 ``Fragment`` 的生命周期，是因为偷偷地往 ``Activity`` 或 ``Fragment`` 上添加了一个看不到的占位 ``Fragment`` ，并且占位 ``Fragment`` 要 **setRetainInstance(true)** （这样可以使得 ``Fragment`` 不会跟随父容器销毁重建），占位 ``Fragment`` 就可以知道什么时候销毁ViewModel了。在这个占位的过程中用到了 ``FragmentManager`` 的 **registerFragmentLifecycleCallbacks** 方法，**support-26** 包才会有的方法：

![ViewModelNeedSupport26][3]

占位 ``Fragment`` 这招在 [Glide][4] 的时候已经被开发出来了，而且 ``Glide`` 是没有 **support-26** 版本要求的。所以修改方案就是把 ``ViewModel`` 这部分代码改成 ``Glide`` 的就好了。

## 对LiveData修改

``LiveData`` 就完全不能用了。因为在 **support-26** 包中，所有 ``Activity`` 和 ``Fragment`` 都会实现 ``LifecycleOwner`` 这个接口，用于提供生命周期的监听，低版本的我们不一样。不过我们有 [RxLifecycle][5] 可以完成相似的任务，所以用 [RxJava][6] 的API实现了一个 ``RxLiveData``，用于取代 ``LiveData``。

### RxLiveData与LiveData的区别：
|RxLiveData|LiveData
|:---:|:---:|
|线程安全|非线程安全，只能用于主线程|
|value不能为 **null** |value允许为 **null** |
|支持所有 **Reactive** 操作符|支持 **Map/SwitchMap/Merge** 等，其余自行实现|
|可以绑定 ``RxLifecycle`` 生命周期|可以绑定 ``LifecycleOwner`` 生命周期|

### RxLiveData的使用

``RxLiveData`` 的构造用法完全与Google官方提供的相同，除了一点：``RxLiveData`` 是线程安全的，你可以在异步操作中直接使用 **setValue** 方法。所以 ``RxLiveData`` 中是没有 **postValue** 方法的。你可以通过[这里][2]了解更多。

```Java
public class StockLiveData extends LiveData<BigDecimal> {
    private StockManager mStockManager;

    private SimplePriceListener mListener = new SimplePriceListener() {
        @Override
        public void onPriceChanged(BigDecimal price) {
            setValue(price);
        }
    };

    public StockLiveData(String symbol) {
        mStockManager = new StockManager(symbol);
    }

    @Override
    protected void onActive() {
        mStockManager.requestPriceUpdates(mListener);
    }

    @Override
    protected void onInactive() {
        mStockManager.removeUpdates(mListener);
    }
}
```

订阅操作就不一样了，RxLiveData用的是Reactive的接口：
```Java
public class Fragment2 extends RxFragment{
    //...
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    
        // `this` should be a LifecycleProvider like RxActivity or RxFragment
        // Without `bindLifecycle()`, it is equivalent to `ObserveForever`
        liveData.bindLifecycle(this)
                .map(new Function<BigDecimal, String>() {
                    @Override
                    public String apply(BigDecimal bigDecimal) throws Exception {
                        return bigDecimal.toEngineeringString();
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        editText.setText(s);
                    }
                });
    }
}
```

## 配置
1. 项目build.gradle添加、
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

2. 对应的module依赖
```groovy
//如果要使用ViewModel 添加以下依赖
//升级到support26以后，只需要修改为implementation "android.arch.lifecycle:viewmodel:1.1.0"即可，代码不需要修改。
implementation 'com.github.YvesCheung.RxArchitectureComponents:rxviewmodel:v1.0'

//如果要使用RxLiveData 添加以下依赖
implementation 'com.github.YvesCheung.RxArchitectureComponents:rxlivedata:v1.0'
//依赖RxLiveData同时也需要依赖RxLifecycle
implementation('com.trello.rxlifecycle2:rxlifecycle-components:2.2.1') {
    exclude group: 'com.android.support', module: 'appcompat-v7'
    exclude group: 'com.android.support', module: 'support-annotations'
}
```

  [1]: https://developer.android.com/topic/libraries/architecture/viewmodel.html
  [2]: https://developer.android.com/topic/libraries/architecture/livedata.html
  [3]: https://raw.githubusercontent.com/YvesCheung/RxArchitectureComponents/master/ViewModelSupport26.jpg
  [4]: https://github.com/bumptech/glide
  [5]: https://github.com/trello/RxLifecycle
  [6]: https://github.com/ReactiveX/RxJava
