package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import android.Manifest;

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
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Button takePhoto;
    private ImageView photoView;
    private Uri photoUri;

    // 声明 ActivityResultLauncher
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // 使用 URI 加载原图到 ImageView
                    if (photoUri != null) {
                        photoView.setImageURI(photoUri);
                    } else {
                        Toast.makeText(this, "图片路径无效", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "取消拍照", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        takePhoto = binding.btnTakePhoto;
        photoView = binding.imageView;

        // 请求摄像头权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        takePhoto.setOnClickListener(view -> {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.myapplication.fileprovider", // 应用的 authorities，需与 Manifest 中一致
                        photoFile
                );
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); // 指定保存路径
                cameraLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, "无法创建图片文件", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // 应用的图片目录
            if (storageDir != null && !storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e("MainActivity", "无法创建图片存储目录");
                    return null;
                }
            }
            return File.createTempFile(
                    imageFileName,  /* 文件名前缀 */
                    ".jpg",         /* 文件后缀 */
                    storageDir      /* 保存目录 */
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 处理权限请求结果
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
