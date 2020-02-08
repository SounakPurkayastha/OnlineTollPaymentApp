package com.example.myapplication;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface JsonPlaceHolderApi {

    @GET("{vehicleId}")
    Call<Post> getQRCode(@Path("vehicleId") String vehicleId);

    @GET("remove/{qrcode}")
    Call<Void> deleteQRCode(@Path("qrcode")String qrcode);

}
