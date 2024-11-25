package com.example.myapplication.db2;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("register") // 只需填写相对于 BASE_URL 的路径
    Call<AuthResponse> authenticate(@Body AuthRequest authRequest);
}
