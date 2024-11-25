package com.example.myapplication.db;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("login") // 这里的 URL 是后端登录接口的路径，根据后端实际路径修改
    Call<LoginResponse> login(@Body LoginRequest request);
}
