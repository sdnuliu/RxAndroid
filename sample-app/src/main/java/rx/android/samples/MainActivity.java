package rx.android.samples;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.subjects.PublishSubject;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MainActivity extends Activity {
    private static final String TAG = "RxAndroidSamples";

    private Handler backgroundHandler;
    private Observable<Integer> integerObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        findViewById(R.id.button_run_scheduler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunSchedulerExampleButtonClicked();
            }
        });
        //简单的RxAndroid测试
        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunSimpleRxAndroidTest();
            }
        });
        //已有数据的RxAndroid测试
        findViewById(R.id.btn_next2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunExistDataButtonClick();
            }
        });
        //PublishSubject测试
        findViewById(R.id.btn_next3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunSubjectTest();
            }
        });
    }

    private void onRunSubjectTest() {
        final PublishSubject<Boolean> objectPublishSubject = PublishSubject.create();
        objectPublishSubject.subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "Observable completed!!And PublishSubject " +
                        "completed!!", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                Toast.makeText(MainActivity.this, aBoolean + "", Toast.LENGTH_SHORT)
                        .show();
//                onCompleted();
            }
        });
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                for (int i = 0; i < 5; i++) {
                    subscriber.onNext(i);
                }
                subscriber.onCompleted();
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                objectPublishSubject.onNext(true);
            }
        }).subscribe();
    }

    private void onRunExistDataButtonClick() {
        ArrayList strList = new ArrayList();
        strList.add("hello");
        strList.add("world");
        strList.add("sdnuliu");
        Observable fromObservable = Observable.from(strList);
        fromObservable.subscribe(new Observer() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "onCompleted!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this, "onError!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Object o) {
                Toast.makeText(MainActivity.this, "Item is:" + o.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }


    private void onRunSimpleRxAndroidTest() {
        integerObservable = Observable.create(new Observable.OnSubscribe<Integer>() {

            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                for (int i = 0; i < 5; i++) {
                    subscriber.onNext(i);
                }
                subscriber.onCompleted();
            }
        });
        integerObservable.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "onCompleted!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this, "onError!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Integer integer) {
                Toast.makeText(MainActivity.this, "onNext(" + integer + ")", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void onRunSchedulerExampleButtonClicked() {
        sampleObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ")");
                    }
                });
    }

    static Observable<String> sampleObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                try {
                    // Do some long running operation
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just("one", "two", "three", "four", "five");
                //just
            }
        });
    }

    static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }
}
