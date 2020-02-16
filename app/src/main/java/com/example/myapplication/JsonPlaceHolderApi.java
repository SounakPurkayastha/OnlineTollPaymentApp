package com.example.myapplication;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface JsonPlaceHolderApi {

    @GET("{token}")
    Call<Post> getQRCode(@Path("token") String token);

    @GET("remove/{qrcode}")
    Call<Void> deleteQRCode(@Path("qrcode")String qrcode);

}
