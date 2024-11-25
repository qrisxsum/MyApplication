package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TakeActivity extends AppCompatActivity {

    private static final String SERVER_BASE_URL = "http://192.168.31.140:8001"; // 全局服务器地址
    private static final String UPLOAD_ENDPOINT = SERVER_BASE_URL + "/image/upload"; // 上传接口地址
    private static final int REQUEST_PERMISSIONS_CODE = 100;

    private ActivityMainBinding binding;
    private Button takePhoto;
    private Button uploadButton; // 新增上传按钮
    private TextView tvUsername;
    private ImageView photoView;
    private Uri photoUri;
    private File photoFile;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (photoFile != null && photoFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        photoView.setImageBitmap(bitmap);
                    } else {
                        showToast("无法加载图片");
                    }
                } else {
                    showToast("拍照取消");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        takePhoto = binding.btnTakePhoto;
        uploadButton = binding.btnUpload; // 绑定新增的上传按钮
        photoView = binding.imageView;
        tvUsername = binding.tvUsername;

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        if (tvUsername != null ) {
            tvUsername.setText("Welcome, " + username);
        } else {
            tvUsername.setText("Welcome, User");
        }

        checkPermissions();

        // 拍照按钮点击事件
        takePhoto.setOnClickListener(view -> {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(
                            this,
                            "com.example.myapplication.fileprovider",
                            photoFile
                    );
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    cameraLauncher.launch(cameraIntent);
                }
            } catch (IOException e) {
                showToast("创建图片文件失败");
            }
        });

        // 上传按钮点击事件
        uploadButton.setOnClickListener(view -> {
            if (photoFile != null && photoFile.exists()) {
                new Thread(() -> uploadImage(photoFile)).start();
            } else {
                showToast("请先拍照后再上传");
            }
        });
    }

    private void checkPermissions() {
        String[] requiredPermissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        boolean permissionsGranted = true;

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = false;
                break;
            }
        }

        if (!permissionsGranted) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_PERMISSIONS_CODE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void uploadImage(File imageFile) {
        showToast("正在上传图片，请稍候...");

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody fileBody = RequestBody.create(imageFile, mediaType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_ENDPOINT)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                Log.d("Upload", "Response: " + responseData);

                JSONObject jsonResponse = new JSONObject(responseData);
                if (jsonResponse.getInt("errno") == 0) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    String imageUrl = data.getString("url");

                    runOnUiThread(() -> {
                        showToast("图片上传成功，加载处理结果");
                        loadProcessedImage(imageUrl);
                    });
                } else {
                    runOnUiThread(() -> showToast("图片上传失败"));
                }
            } else {
                runOnUiThread(() -> showToast("图片上传失败，错误码：" + response.code()));
                Log.e("Upload", "Failed to upload image. Response code: " + response.code());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> showToast("上传过程中发生错误"));
        }
    }

    private void loadProcessedImage(String imageUrl) {
        new Thread(() -> {
            String fullUrl = Uri.parse(SERVER_BASE_URL).buildUpon()
                    .appendEncodedPath(imageUrl)
                    .build().toString();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(fullUrl).build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    runOnUiThread(() -> photoView.setImageBitmap(bitmap));
                } else {
                    runOnUiThread(() -> showToast("加载处理后的图片失败"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> showToast("加载处理后的图片失败"));
            }
        }).start();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                showToast("需要摄像头权限才能拍照");
                takePhoto.setEnabled(false);
            }
        }
    }
}
