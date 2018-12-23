package com.coolweather.coolweather.network;


import android.support.annotation.Nullable;

import com.coolweather.coolweather.gson.Prov;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {
    private ApiService apiService;
    public static String BASE_URL = "http://guolin.tech/api/";
    public static final int TIMEOUT = 60;
    private static volatile RetrofitUtil mInstance;
    private Retrofit mRetrofit;
    Scheduler observeOn = AndroidSchedulers.mainThread();

    public static RetrofitUtil getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitUtil.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitUtil();
                }
            }
        }
        return mInstance;
    }

    private RetrofitUtil() {
        initRetrofit();
    }

    private void initRetrofit() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        // 设置超时
        builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(TIMEOUT, TimeUnit.SECONDS);
        OkHttpClient client = builder.addInterceptor(logging).build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new ObserveOnMainCallAdapterFactory(observeOn))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(client)
                .build();
        apiService = mRetrofit.create(ApiService.class);
    }

    public Observable<List<Prov>> getProvinces(){
        return apiService.getProvinces();
    }

    static final class ObserveOnMainCallAdapterFactory extends CallAdapter.Factory {
        final Scheduler scheduler;

        ObserveOnMainCallAdapterFactory(Scheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override public @Nullable
        CallAdapter<?, ?> get(
                Type returnType, Annotation[] annotations, Retrofit retrofit) {
            if (getRawType(returnType) != Observable.class) {
                return null; // Ignore non-Observable types.
            }

            // Look up the next call adapter which would otherwise be used if this one was not present.
            //noinspection unchecked returnType checked above to be Observable.
            final CallAdapter<Object, Observable<?>> delegate =
                    (CallAdapter<Object, Observable<?>>) retrofit.nextCallAdapter(this, returnType,
                            annotations);

            return new CallAdapter<Object, Object>() {
                @Override public Object adapt(Call<Object> call) {
                    // Delegate to get the normal Observable...
                    Observable<?> o = delegate.adapt(call);
                    // ...and change it to send notifications to the observer on the specified scheduler.
                    return o.observeOn(scheduler);
                }

                @Override public Type responseType() {
                    return delegate.responseType();
                }
            };
        }
    }
}
