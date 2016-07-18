package com.example.chenshuwang.rxsample;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private TextView tv1;
    private TextView tv2;
    private Button startBtn;
    private Button stopBtn;
    private String tvText1 = "";
    private String tvText2 = "";
    public static final int UPDATE_TEXT1 = 1;
    public static final int UPDATE_TEXT2 = 2;

    private ConnectableObservable<Long> publishObserver() {
        Observable<Long> obser = Observable.interval(1, TimeUnit.SECONDS);
        obser.observeOn(Schedulers.newThread());
        return obser.publish();
    }

    Observable.OnSubscribe mObservableAction = new Observable.OnSubscribe<String>() {
        @Override public void call(Subscriber<? super String> subscriber) {
            subscriber.onNext(setValue());
            subscriber.onCompleted();
        }
    };

    Subscription mSubscription;
    Observable<String> observable;

    private String setValue() {
        return "Hello, Walter1";
    }

    private void setValue1() {
        tv1.setText(tvText1);
    }

    private void setValue2() {
        tv2.setText(tvText2);
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT1:
                    setValue1();
                    break;
                case UPDATE_TEXT2:
                    setValue2();
                break;
            }
            super.handleMessage(msg);
        }
    };

    public void initView() {
        setContentView(R.layout.activity_main);
        tv1 = (TextView)findViewById(R.id.textview1);
        tv2 = (TextView)findViewById(R.id.textview2);
        startBtn = (Button)findViewById(R.id.button1);
        stopBtn = (Button)findViewById(R.id.button2);
    }

    public void sendMsg1(Object o) {
        System.out.println("action1:" + o);
        tvText1 = String.valueOf(o);
        Message m = new Message();
        m.what = UPDATE_TEXT1;
        handler.sendMessage(m);
    }

    public void sendMsg2(Object o) {
        System.out.println("action2:" + o);
        tvText2 = String.valueOf(o);
        Message m = new Message();
        m.what = UPDATE_TEXT2;
        handler.sendMessage(m);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        ConnectableObservable<Long> obs = publishObserver();
        Action1 action2 = o -> sendMsg2(o);
        Action1 action1 = o -> {
            sendMsg1(o);
            if ((long) o == 3) {
                obs.subscribe(action2);
            }
        };

        obs.subscribe(action1);
        observable = Observable.create(mObservableAction);
        observable.observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(s -> tv1.setText(s));

        startBtn.setOnClickListener(e -> mSubscription = obs.connect());
        stopBtn.setOnClickListener(e -> {
            if (mSubscription != null) {
                mSubscription.unsubscribe();
            }
        });
    }
}
