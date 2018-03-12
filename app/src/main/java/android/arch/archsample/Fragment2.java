package android.arch.archsample;

import android.arch.rxlifecycle.ViewModel;
import android.arch.rxlifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 张宇 on 2018/3/12.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */

public class Fragment2 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_light, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewModel viewModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        Log.i("zycheck", viewModel.toString());
    }
}
