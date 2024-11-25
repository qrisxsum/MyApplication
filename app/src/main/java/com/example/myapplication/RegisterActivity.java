package com.example.myapplication;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.db.ApiClient;
import com.example.myapplication.db2.ApiClient1;
import com.example.myapplication.db2.ApiService;
import com.example.myapplication.db2.AuthRequest;
import com.example.myapplication.db2.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_username1;
    private EditText et_passward1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_username1 = findViewById(R.id.et_username1);
        et_passward1 = findViewById(R.id.et_passward1);

        // 点击注册按钮，跳转到登录页面
        findViewById(R.id.textView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转登录界面
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // 点击注册按钮，执行注册请求
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username1 = et_username1.getText().toString();
                String password1 = et_passward1.getText().toString();

                if (TextUtils.isEmpty(username1) || TextUtils.isEmpty(password1)) {
                    Toast.makeText(RegisterActivity.this, "请输入用户名或密码", Toast.LENGTH_SHORT).show();
                } else {
                    performAuthRequest(username1, password1, "register"); // 执行注册请求
                }
            }
        });
    }

    // 执行请求，type 为 "register" 表示注册
    private void performAuthRequest(String username, String password, String type) {
        // 创建 Retrofit 实例并发送请求
        ApiService apiService = ApiClient1.getRetrofitInstance().create(ApiService.class);
        AuthRequest authRequest = new AuthRequest(username, password, type); // 创建请求对象

        // 发起请求
        apiService.authenticate(authRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if ("success".equals(authResponse.getStatus())) {
                        // 注册成功
                        Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                        finish(); // 关闭注册页面并返回登录界面
                    } else {
                        // 注册失败
                        Toast.makeText(RegisterActivity.this, authResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 网络请求失败
                    Toast.makeText(RegisterActivity.this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // 请求失败
                Toast.makeText(RegisterActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
