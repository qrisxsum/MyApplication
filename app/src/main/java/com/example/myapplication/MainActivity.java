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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Button takePhoto;
    private Button uploadButton; // 新增上传按钮
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
                        Toast.makeText(this, "无法加载图片", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "拍照取消", Toast.LENGTH_SHORT).show();
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

        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

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
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
            }
        });

        // 上传按钮点击事件
        uploadButton.setOnClickListener(view -> {
            if (photoFile != null && photoFile.exists()) {
                new Thread(() -> {
                    uploadImage(photoFile);
                }).start();
            } else {
                Toast.makeText(this, "请先拍照后再上传", Toast.LENGTH_SHORT).show();
            }
        });
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
        String uploadUrl = "http://192.168.31.231:5000/upload"; // 替换为你的服务器接口地址

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("image/jpeg");

        RequestBody fileBody = RequestBody.create(imageFile, mediaType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                runOnUiThread(() -> {
                    Toast.makeText(this, "图片上传成功", Toast.LENGTH_SHORT).show();
                });
                Log.d("Upload", "Response: " + responseData);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "图片上传失败，错误码：" + response.code(), Toast.LENGTH_SHORT).show();
                });
                Log.e("Upload", "Failed to upload image. Response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(this, "上传过程中发生错误", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "需要摄像头权限才能拍照", Toast.LENGTH_SHORT).show();
                takePhoto.setEnabled(false);
            }
        }
    }
}


