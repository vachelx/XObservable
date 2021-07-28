package com.vachel.observable.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vachel.observable.Emitter;
import com.vachel.observable.R;
import com.vachel.observable.XObservable;
import com.vachel.observable.XThreadPoolManager;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, XObservable.IExtraLife {

    private TextView mScoreView;
    private int testCount;
    private CheckBox mDisableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScoreView = findViewById(R.id.score);
        mDisableView = findViewById(R.id.disable_test);
        findViewById(R.id.test).setOnClickListener(this);
    }

    @Override
    public boolean isLifeDestroy(Object tag) {
        return mDisableView.isChecked();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.test) {
            Log.d("test", "click count = " + testCount++);
            XObservable.create(new XObservable.ObservableOnSubscribe<Integer>() {
                @Override
                public void subscribe(Emitter<Integer> emitter) {
                    emitter.onNext(-1);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    emitter.onNext(new Random().nextInt(100));
                }
            }).bindLifeCycle(MainActivity.this.getLifecycle())
                    .bindExtraLife(this, mScoreView.getTag())
//                        .setMainHandler(new Handler())
                    .executeOnExecutor(XThreadPoolManager.getThreadPool())
                    .subscribe(new XObservable.Consumer<Integer>() {
                        @Override
                        public void accept(Integer result) {
                            if (result == -1) {// 正在考试中
                                mScoreView.setText("韩梅梅马不停蹄的答题中......");
                            } else {
                                Log.d("test", "test result = " + result);
                                mScoreView.setText("韩梅梅考了" + result + "分！");
                            }
                        }
                    });
        }
    }
}