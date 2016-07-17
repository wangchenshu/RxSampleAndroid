package com.example.chenshuwang.rxsample;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.ThreadFactory;
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
    private Button startBtn;
    private Button stopBtn;
    private String tvText = "";

    private ConnectableObservable<Long> publishObserver() {
        Observable<Long> obser = Observable.interval(1, TimeUnit.SECONDS);
        obser.observeOn(Schedulers.newThread());
        //obser.subscribeOn(AndroidSchedulers.mainThread());
        return obser.publish();
    }

    Subscriber<String> mTextSubscriber = new Subscriber<String>() {
        @Override public void onCompleted() {
            System.out.println("on complete");
        }

        @Override public void onError(Throwable e) {

        }

        @Override public void onNext(String s) {
            tv1.setText(s);
        }
    };

    Subscriber<Long> mTextSubscriber2 = new Subscriber<Long>() {
        @Override public void onCompleted() {
            System.out.println("on complete");
        }

        @Override public void onError(Throwable e) {
            System.out.println("e: " + e);
        }

        @Override public void onNext(Long s) {
            tv1.setText(String.valueOf(s));
        }
    };

    private String setValue() {
        return tvText;
    }

    Observable.OnSubscribe mObservableAction = new Observable.OnSubscribe<String>() {
        @Override public void call(Subscriber<? super String> subscriber) {
            subscriber.onNext(setValue());
            subscriber.onCompleted();
        }
    };

    Subscription mSubscription;
    Observable<String> observable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = (TextView)findViewById(R.id.textview1);
        startBtn = (Button)findViewById(R.id.button1);
        stopBtn = (Button)findViewById(R.id.button2);
        tv1.setText("");

        ConnectableObservable<Long> obs = publishObserver();
        Action1 action2 = o -> System.out.println("action2:" + o);
        Action1 action1 = o -> {
            System.out.println("action1:" + o);
            if ((long) o == 3) obs.subscribe(action2);
        };
        obs.subscribe(action1);

        observable = Observable.create(mObservableAction);
        observable.observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(mTextSubscriber);

        startBtn.setOnClickListener(e -> mSubscription = obs.connect());
        stopBtn.setOnClickListener(e -> {
            if (mSubscription != null) {
                mSubscription.unsubscribe();
            }
        });
    }
}
