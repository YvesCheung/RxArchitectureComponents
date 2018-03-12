/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package android.arch.rxlifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 修改说明：
 * 目前公司项目中使用的support-v4包是版本24的，而且使用Small插件化框架导致暂时无法升级到版本26，所以没办法使用
 * {@link FragmentManager} 中的 {@code FragmentLifecycleCallbacks}，迫于无奈把
 * {@code holderFragmentFor(Fragment parentFragment)} 对应的实现替换成了
 * {@code Glide.with(Fragment fragment)} 的解决方案
 * <p>
 * Modification：
 * At present, the support-v4 package used in the company project is version 24, and using the Small
 * plug-in framework causes a temporary failure to upgrade to version 26, so there is no way to use
 * the {@code FragmentLifecycleCallbacks} in the {@link FragmentManager}, and the implementation of the
 * {@code holderFragmentFor(Fragment parentFragment)} cannot be achieved. Replaced with
 * {@code Glide.with(Fragment fragment)} solution
 *
 * @hide
 */
public class HolderFragment extends Fragment {
    /**
     * @hide
     */
    public static final String HOLDER_TAG =
            "android.arch.rxlifecycle.state.StateProviderHolderFragment";

    private static final String LOG_TAG = "ViewModelStores";
    private static final HolderFragmentManager sHolderFragmentManager = new HolderFragmentManager();
    private ViewModelStore mViewModelStore = new ViewModelStore();

    public HolderFragment() {
        setRetainInstance(true);
    }

    /**
     * @hide
     */
    public static HolderFragment holderFragmentFor(FragmentActivity activity) {
        return sHolderFragmentManager.holderFragmentFor(activity);
    }

    /**
     * @hide
     */
    public static HolderFragment holderFragmentFor(Fragment fragment) {
        return sHolderFragmentManager.holderFragmentFor(fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sHolderFragmentManager.holderFragmentCreated(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModelStore.clear();
    }

    public ViewModelStore getViewModelStore() {
        return mViewModelStore;
    }

    @SuppressWarnings("WeakerAccess")
    static class HolderFragmentManager {
        private static final int ID_REMOVE_SUPPORT_FRAGMENT_MANAGER = 2;

        private Map<Activity, HolderFragment> mNotCommittedActivityHolders = new HashMap<>();
        private Map<FragmentManager, HolderFragment> mNotCommittedFragmentHolders = new HashMap<>();

        private Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == ID_REMOVE_SUPPORT_FRAGMENT_MANAGER) {
                    FragmentManager supportFm = (FragmentManager) message.obj;
                    mNotCommittedFragmentHolders.remove(supportFm);
                    return true;
                }
                return false;
            }
        });

        private Application.ActivityLifecycleCallbacks mActivityCallbacks =
                new EmptyActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        HolderFragment fragment = mNotCommittedActivityHolders.remove(activity);
                        if (fragment != null) {
                            Log.e(LOG_TAG, "Failed to save a ViewModel for " + activity);
                        }
                    }
                };

        private boolean mActivityCallbacksIsAdded = false;

        private static HolderFragment findHolderFragment(FragmentManager manager) {
            if (manager.isDestroyed()) {
                throw new IllegalStateException("Can't access ViewModels from onDestroy");
            }

            Fragment fragmentByTag = manager.findFragmentByTag(HOLDER_TAG);
            if (fragmentByTag != null && !(fragmentByTag instanceof HolderFragment)) {
                throw new IllegalStateException("Unexpected "
                        + "fragment instance was returned by HOLDER_TAG");
            }
            return (HolderFragment) fragmentByTag;
        }

        private static HolderFragment createHolderFragment(FragmentManager fragmentManager) {
            HolderFragment holder = new HolderFragment();
            fragmentManager.beginTransaction().add(holder, HOLDER_TAG).commitAllowingStateLoss();
            return holder;
        }

        void holderFragmentCreated(Fragment holderFragment) {
            Fragment parentFragment = holderFragment.getParentFragment();
            if (parentFragment != null) {
                mNotCommittedFragmentHolders.remove(parentFragment.getChildFragmentManager());
            } else {
                mNotCommittedActivityHolders.remove(holderFragment.getActivity());
            }
        }

        HolderFragment holderFragmentFor(FragmentActivity activity) {
            FragmentManager fm = activity.getSupportFragmentManager();
            HolderFragment holder = findHolderFragment(fm);
            if (holder != null) {
                return holder;
            }
            holder = mNotCommittedActivityHolders.get(activity);
            if (holder != null) {
                return holder;
            }

            if (!mActivityCallbacksIsAdded) {
                mActivityCallbacksIsAdded = true;
                activity.getApplication().registerActivityLifecycleCallbacks(mActivityCallbacks);
            }
            holder = createHolderFragment(fm);
            mNotCommittedActivityHolders.put(activity, holder);
            return holder;
        }

        HolderFragment holderFragmentFor(Fragment parentFragment) {
            FragmentManager fm = parentFragment.getChildFragmentManager();
            HolderFragment holder = findHolderFragment(fm);
            if (holder != null) {
                return holder;
            }
            holder = mNotCommittedFragmentHolders.get(fm);
            if (holder != null) {
                return holder;
            }

            holder = createHolderFragment(fm);
            mNotCommittedFragmentHolders.put(fm, holder);
            handler.obtainMessage(ID_REMOVE_SUPPORT_FRAGMENT_MANAGER, fm).sendToTarget();
            return holder;
        }
    }
}
