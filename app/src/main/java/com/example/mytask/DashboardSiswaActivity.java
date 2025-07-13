package com.example.mytask;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.GET;

public class DashboardSiswaActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "DashboardSiswa";

    private static final String BASE_URL = "http:/YOUR IP VPS/PC WHERE BACKEND RUNNING:3000/"; // Pastikan IP dan port sesuai

    private Spinner spinnerKelas, spinnerMataPelajaran;
    private Button btnChooseFile, btnSubmit, btnLogout;
    private TextView tvSelectedFile;

    private Uri selectedFileUri;
    private String selectedFileName;
    private ProgressDialog progressDialog;
    private ApiService apiService;

    // DATA DROP DOWN LISTT
    private List<String> kelasList;
    private List<String> mataPelajaranList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_siswa);

        try {
            initViews();
            setupRetrofit();
            setupSpinners();
            setupClickListeners();
            checkPermissions();

            // KONEKSI KE SERVERRR
            testServerConnection();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        try {
            spinnerKelas = findViewById(R.id.spinnerKelas);
            spinnerMataPelajaran = findViewById(R.id.spinnerMataPelajaran);
            btnChooseFile = findViewById(R.id.btnChooseFile);
            btnSubmit = findViewById(R.id.btnSubmit);
            btnLogout = findViewById(R.id.btnLogout);
            tvSelectedFile = findViewById(R.id.tvSelectedFile);

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading file...");
            progressDialog.setCancelable(false);

        } catch (Exception e) {
            Log.e(TAG, "Error in initViews: ", e);
            throw e;
        }
    }

    private void setupRetrofit() {
        try {
            // Tambahkan logging interceptor untuk debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);

        } catch (Exception e) {
            Log.e(TAG, "Error in setupRetrofit: ", e);
            throw e;
        }
    }

    private void testServerConnection() {
        Call<ResponseBody> call = apiService.getServerStatus();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Server connection successful");
                    Toast.makeText(DashboardSiswaActivity.this, "Terhubung ke server", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Server connection failed: " + response.code());
                    Toast.makeText(DashboardSiswaActivity.this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Server connection error: ", t);
                Toast.makeText(DashboardSiswaActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSpinners() {
        try {
            // Setup data kelas
            kelasList = new ArrayList<>();
            kelasList.add("Pilih Kelas");
            kelasList.add("X MIPA 1");
            kelasList.add("X MIPA 2");
            kelasList.add("X IPS 1");
            kelasList.add("X IPS 2");
            kelasList.add("XI MIPA 1");
            kelasList.add("XI MIPA 2");
            kelasList.add("XI IPS 1");
            kelasList.add("XI IPS 2");
            kelasList.add("XII MIPA 1");
            kelasList.add("XII MIPA 2");
            kelasList.add("XII IPS 1");
            kelasList.add("XII IPS 2");

            // Setup data mata pelajaran
            mataPelajaranList = new ArrayList<>();
            mataPelajaranList.add("Pilih Mata Pelajaran");
            mataPelajaranList.add("Matematika");
            mataPelajaranList.add("Bahasa Indonesia");
            mataPelajaranList.add("Bahasa Inggris");
            mataPelajaranList.add("Fisika");
            mataPelajaranList.add("Kimia");
            mataPelajaranList.add("Biologi");
            mataPelajaranList.add("Sejarah");
            mataPelajaranList.add("Geografi");
            mataPelajaranList.add("Ekonomi");
            mataPelajaranList.add("Sosiologi");
            mataPelajaranList.add("PKN");

            // Setup adapter untuk spinner
            ArrayAdapter<String> kelasAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, kelasList);
            kelasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerKelas.setAdapter(kelasAdapter);

            ArrayAdapter<String> mataPelajaranAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, mataPelajaranList);
            mataPelajaranAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMataPelajaran.setAdapter(mataPelajaranAdapter);

        } catch (Exception e) {
            Log.e(TAG, "Error in setupSpinners: ", e);
            throw e;
        }
    }

    private void setupClickListeners() {
        try {
            btnChooseFile.setOnClickListener(v -> openFileChooser());
            btnSubmit.setOnClickListener(v -> submitTask());
            btnLogout.setOnClickListener(v -> logout());
        } catch (Exception e) {
            Log.e(TAG, "Error in setupClickListeners: ", e);
            throw e;
        }
    }

    private void checkPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ menggunakan READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_VIDEO,
                                    Manifest.permission.READ_MEDIA_AUDIO
                            },
                            PERMISSION_REQUEST_CODE);
                }
            } else {
                // Android 12 dan bawah menggunakan READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in checkPermissions: ", e);
        }
    }

    private void openFileChooser() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // Filter untuk file yang diizinkan
            String[] mimeTypes = {
                    "image/*",
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "text/plain"
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            startActivityForResult(Intent.createChooser(intent, "Pilih File"), PICK_FILE_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error opening file chooser: ", e);
            Toast.makeText(this, "Error membuka file chooser: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
                selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    selectedFileName = getFileName(selectedFileUri);
                    tvSelectedFile.setText("File dipilih: " + selectedFileName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        tvSelectedFile.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));
                    } else {
                        tvSelectedFile.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult: ", e);
            Toast.makeText(this, "Error selecting file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "";
        try {
            if (uri.getScheme().equals("content")) {
                try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex);
                        }
                    }
                }
            }
            if (fileName.isEmpty()) {
                fileName = uri.getPath();
                int cut = fileName.lastIndexOf('/');
                if (cut != -1) {
                    fileName = fileName.substring(cut + 1);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name: ", e);
            fileName = "unknown_file";
        }
        return fileName;
    }

    private void submitTask() {
        try {
            // Validasi input
            if (spinnerKelas.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Pilih kelas terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnerMataPelajaran.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Pilih mata pelajaran terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedFileUri == null) {
                Toast.makeText(this, "Pilih file terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ambil data dari spinner
            String selectedKelas = spinnerKelas.getSelectedItem().toString();
            String selectedMataPelajaran = spinnerMataPelajaran.getSelectedItem().toString();

            uploadFile(selectedKelas, selectedMataPelajaran);

        } catch (Exception e) {
            Log.e(TAG, "Error in submitTask: ", e);
            Toast.makeText(this, "Error submitting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(String kelas, String mataPelajaran) {
        try {
            // Buat file temporary dari URI
            File tempFile = createTempFileFromUri(selectedFileUri);
            if (tempFile == null) {
                Toast.makeText(this, "Gagal membuat file temporary", Toast.LENGTH_SHORT).show();
                return;
            }

            // Buat request body untuk file
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(getContentResolver().getType(selectedFileUri)),
                    tempFile
            );
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", selectedFileName, requestFile);

            // Buat request body untuk parameter lain
            RequestBody kelasBody = RequestBody.create(MediaType.parse("text/plain"), kelas);
            RequestBody mataPelajaranBody = RequestBody.create(MediaType.parse("text/plain"), mataPelajaran);

            progressDialog.show();

            // Panggil API
            Call<ResponseBody> call = apiService.uploadFile(filePart, kelasBody, mataPelajaranBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    progressDialog.dismiss();

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseString = response.body().string();
                            Log.d(TAG, "Upload response: " + responseString);

                            JsonObject jsonResponse = new Gson().fromJson(responseString, JsonObject.class);

                            if (jsonResponse.get("success").getAsBoolean()) {
                                String cloudinaryUrl = jsonResponse.get("url").getAsString();
                                Toast.makeText(DashboardSiswaActivity.this,
                                        "File berhasil diupload!", Toast.LENGTH_LONG).show();

                                // Reset form
                                resetForm();

                                Log.d(TAG, "Cloudinary URL: " + cloudinaryUrl);
                            } else {
                                String errorMessage = jsonResponse.get("message").getAsString();
                                Toast.makeText(DashboardSiswaActivity.this,
                                        "Upload gagal: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(DashboardSiswaActivity.this,
                                    "Upload gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        Toast.makeText(DashboardSiswaActivity.this,
                                "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    // Hapus file temporary
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    Log.e(TAG, "Upload failed", t);
                    Toast.makeText(DashboardSiswaActivity.this,
                            "Upload gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                    // Hapus file temporary
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Error in uploadFile", e);
            Toast.makeText(this, "Error uploading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(getCacheDir(), selectedFileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Error creating temp file", e);
            return null;
        }
    }

    private void resetForm() {
        try {
            spinnerKelas.setSelection(0);
            spinnerMataPelajaran.setSelection(0);
            selectedFileUri = null;
            selectedFileName = null;
            tvSelectedFile.setText("Belum ada file yang dipilih");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvSelectedFile.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            } else {
                tvSelectedFile.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in resetForm: ", e);
        }
    }

    private void logout() {
        try {
            // Panggil static method performLogout dari LoginActivity
            LoginActivity.performLogout(this);
        } catch (Exception e) {
            Log.e(TAG, "Error in logout: ", e);
            Toast.makeText(this, "Error during logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this, "Permission diperlukan untuk memilih file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Interface untuk API Service
    public interface ApiService {
        @Multipart
        @POST("api/upload")
        Call<ResponseBody> uploadFile(
                @Part MultipartBody.Part file,
                @Part("kelas") RequestBody kelas,
                @Part("mataPelajaran") RequestBody mataPelajaran
        );

        @GET("api/status")
        Call<ResponseBody> getServerStatus();
    }
}