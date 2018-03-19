package android.arch.archsample;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.trello.rxlifecycle2.components.support.RxFragment;

import io.reactivex.functions.Consumer;

/**
 * Created by 张宇 on 2018/3/12.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */

public class Fragment2 extends RxFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_light, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final MyViewModel viewModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);

        final EditText editText = (EditText) view.findViewById(R.id.editText);
        final Button button = (Button) view.findViewById(R.id.button);

        viewModel.getFragment2Text().bindLifecycle(this).subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(CharSequence charSequence) throws Exception {
                editText.setText(charSequence);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.postText1(editText.getText());
            }
        });
    }
}
