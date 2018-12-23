package com.coolweather.coolweather.network;

import com.coolweather.coolweather.db.Province;
import com.coolweather.coolweather.gson.Prov;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ApiService {
    @GET("china")
    Observable<List<Prov>> getProvinces();

    @GET("{url}")
    Observable<ResponseBody> getWithQuery(
            @Path("url") String url,
            @QueryMap Map<String, String> maps);

    @POST("{url}")
    Observable<ResponseBody> post(
            @Path("url") String url,
            @Body RequestBody body);

}
