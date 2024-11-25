package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.db.ApiClient;
import com.example.myapplication.db.ApiService;
import com.example.myapplication.db.LoginRequest;
import com.example.myapplication.db.LoginResponse;
import com.example.myapplication.db.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_passward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_username = findViewById(R.id.et_username);
        et_passward = findViewById(R.id.et_passward);
        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转注册界面
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 点击登录按钮
        findViewById(R.id.button0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = et_username.getText().toString();
                String password = et_passward.getText().toString();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "请输入用户名或密码", Toast.LENGTH_SHORT).show();
                } else {
                    performLogin(username, password); // 执行登录请求
                }
            }
        });
    }

    private void performLogin(String username, String password) {
        // 创建 Retrofit 实例并发送请求
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(username, password); // 创建请求对象

        // 发起登录请求
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    User user = loginResponse.getUser(); // 获取用户信息
                    if (user != null) {
                        // 登录成功，显示用户信息
                        Toast.makeText(LoginActivity.this, "登录成功，欢迎 " + user.getUsername(), Toast.LENGTH_SHORT).show();

                        // 示例：跳转到主页面并传递用户信息
                        Intent intent = new Intent(LoginActivity.this, TakeActivity.class);
                        intent.putExtra("username", user.getUsername());
                        intent.putExtra("uid", user.getUid());
                        intent.putExtra("last_login", user.getLast_login());
                        startActivity(intent);

                        // 可在此保存登录状态，比如使用 SharedPreferences
                    } else {
                        // 用户信息为空
                        Toast.makeText(LoginActivity.this, "登录成功，但未获取到用户信息", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 网络请求成功，但服务器返回失败
                    Toast.makeText(LoginActivity.this, "登录失败，请检查用户名或密码", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // 请求失败
                Toast.makeText(LoginActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}