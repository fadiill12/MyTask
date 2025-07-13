package com.example.mytask;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class DashboardGuruActivity extends AppCompatActivity {

    private static final String TAG = "DashboardGuru";
    private static final int PERMISSION_REQUEST_CODE = 101;

    private static final String BASE_URL = "http://YOUR IP VPS/PC WHERE BACKEND RUNNING:3000/"; //Pastikan IP dan port sesuai

    private Spinner spinnerKelas, spinnerMataPelajaran;
    private SearchView searchView;
    private ListView listViewFiles;
    private Button btnLogout;

    private ApiService apiService;
    private ProgressDialog progressDialog;
    private FileListAdapter fileListAdapter;
    private List<FileItem> allFilesList;
    private List<FileItem> filteredFilesList;
    private Map<Long, String> downloadIdToFileName;

    private List<String> kelasList;
    private List<String> mataPelajaranList;

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadIdToFileName.containsKey(downloadId)) {
                String fileName = downloadIdToFileName.get(downloadId);
                Toast.makeText(DashboardGuruActivity.this,
                        "Download selesai: " + fileName, Toast.LENGTH_LONG).show();
                downloadIdToFileName.remove(downloadId);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_guru);

        try {
            initViews();
            setupRetrofit();
            setupSpinners();
            setupListeners();
            checkPermissions();

            allFilesList = new ArrayList<>();
            filteredFilesList = new ArrayList<>();
            downloadIdToFileName = new HashMap<>();

            fileListAdapter = new FileListAdapter(this, filteredFilesList);
            listViewFiles.setAdapter(fileListAdapter);

            registerReceiver(downloadReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        spinnerKelas = findViewById(R.id.spinnerKelas);
        spinnerMataPelajaran = findViewById(R.id.spinnerMataPelajaran);
        searchView = findViewById(R.id.searchView);
        listViewFiles = findViewById(R.id.listViewFiles);
        btnLogout = findViewById(R.id.btnLogout);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void setupRetrofit() {
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
    }

    private void setupSpinners() {
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

        ArrayAdapter<String> kelasAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, kelasList);
        kelasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKelas.setAdapter(kelasAdapter);

        ArrayAdapter<String> mataPelajaranAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mataPelajaranList);
        mataPelajaranAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMataPelajaran.setAdapter(mataPelajaranAdapter);
    }

    private void setupListeners() {
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadFiles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerKelas.setOnItemSelectedListener(spinnerListener);
        spinnerMataPelajaran.setOnItemSelectedListener(spinnerListener);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFiles(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFiles(newText);
                return true;
            }
        });

        // ListView item click listener
        listViewFiles.setOnItemClickListener((parent, view, position, id) -> {
            FileItem fileItem = filteredFilesList.get(position);
            downloadFile(fileItem);
        });

        // Logout button
        btnLogout.setOnClickListener(v -> logout());
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - no WRITE_EXTERNAL_STORAGE needed for Downloads folder
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void loadFiles() {
        if (spinnerKelas.getSelectedItemPosition() == 0 ||
                spinnerMataPelajaran.getSelectedItemPosition() == 0) {
            // Clear list jika belum ada pilihan
            allFilesList.clear();
            filteredFilesList.clear();
            fileListAdapter.notifyDataSetChanged();
            return;
        }

        String selectedKelas = spinnerKelas.getSelectedItem().toString();
        String selectedMataPelajaran = spinnerMataPelajaran.getSelectedItem().toString();

        // Convert to URL-safe format
        String kelasParam = selectedKelas.toLowerCase().replace(" ", "_");
        String mataPelajaranParam = selectedMataPelajaran.toLowerCase().replace(" ", "_");

        progressDialog.setMessage("Memuat daftar file...");
        progressDialog.show();

        Call<ResponseBody> call = apiService.getFiles(kelasParam, mataPelajaranParam);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                progressDialog.dismiss();

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        Log.d(TAG, "Files response: " + responseString);

                        JsonObject jsonResponse = new Gson().fromJson(responseString, JsonObject.class);

                        if (jsonResponse.get("success").getAsBoolean()) {
                            JsonArray filesArray = jsonResponse.getAsJsonArray("files");

                            allFilesList.clear();
                            for (int i = 0; i < filesArray.size(); i++) {
                                JsonObject fileObject = filesArray.get(i).getAsJsonObject();
                                FileItem fileItem = parseFileItem(fileObject);
                                if (fileItem != null) {
                                    allFilesList.add(fileItem);
                                }
                            }

                            filterFiles(searchView.getQuery().toString());

                            Toast.makeText(DashboardGuruActivity.this,
                                    "Ditemukan " + allFilesList.size() + " file", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = jsonResponse.get("message").getAsString();
                            Toast.makeText(DashboardGuruActivity.this,
                                    "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DashboardGuruActivity.this,
                                "Gagal memuat file: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing files response", e);
                    Toast.makeText(DashboardGuruActivity.this,
                            "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Failed to load files", t);
                Toast.makeText(DashboardGuruActivity.this,
                        "Gagal memuat file: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private FileItem parseFileItem(JsonObject fileObject) {
        try {
            FileItem item = new FileItem();
            item.publicId = fileObject.get("public_id").getAsString();
            item.secureUrl = fileObject.get("secure_url").getAsString();
            item.format = fileObject.get("format").getAsString();
            item.resourceType = fileObject.get("resource_type").getAsString();

            // Extract filename from public_id
            String[] pathParts = item.publicId.split("/");
            if (pathParts.length > 0) {
                item.fileName = pathParts[pathParts.length - 1] + "." + item.format;
            } else {
                item.fileName = "unknown." + item.format;
            }

            // Parse file size
            if (fileObject.has("bytes")) {
                item.fileSize = fileObject.get("bytes").getAsLong();
            }

            // Parse upload date
            if (fileObject.has("created_at")) {
                item.uploadedAt = fileObject.get("created_at").getAsString();
            }

            return item;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing file item", e);
            return null;
        }
    }

    private void filterFiles(String query) {
        filteredFilesList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredFilesList.addAll(allFilesList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (FileItem item : allFilesList) {
                if (item.fileName.toLowerCase().contains(lowerQuery)) {
                    filteredFilesList.add(item);
                }
            }
        }

        fileListAdapter.notifyDataSetChanged();
    }

    private void downloadFile(FileItem fileItem) {
        if (!hasStoragePermission()) {
            Toast.makeText(this, "Permission diperlukan untuk download", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(fileItem.secureUrl);

            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setTitle("MyTask - " + fileItem.fileName);
            request.setDescription("Mengunduh file tugas");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Set destination
            String fileName = sanitizeFileName(fileItem.fileName);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "MyTask/" + fileName);

            long downloadId = downloadManager.enqueue(request);
            downloadIdToFileName.put(downloadId, fileName);

            Toast.makeText(this, "Download dimulai: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error starting download", e);
            Toast.makeText(this, "Error starting download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String sanitizeFileName(String fileName) {
        // Replace invalid characters for file system
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true; // No permission needed for Downloads folder on Android 13+
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission diperlukan untuk download file", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(downloadReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver", e);
        }
    }

    // Data class untuk file item
    public static class FileItem {
        public String publicId;
        public String secureUrl;
        public String fileName;
        public String format;
        public String resourceType;
        public long fileSize;
        public String uploadedAt;

        public String getFormattedSize() {
            if (fileSize <= 0) return "Unknown size";

            String[] units = {"B", "KB", "MB", "GB"};
            int unitIndex = 0;
            double size = fileSize;

            while (size >= 1024 && unitIndex < units.length - 1) {
                size /= 1024;
                unitIndex++;
            }

            return String.format(Locale.getDefault(), "%.1f %s", size, units[unitIndex]);
        }

        public String getFormattedDate() {
            try {
                if (uploadedAt != null && !uploadedAt.isEmpty()) {
                    // Parse ISO date string and format it
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    Date date = isoFormat.parse(uploadedAt);
                    return displayFormat.format(date);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error formatting date", e);
            }
            return "Unknown date";
        }
    }

    // API Service interface
    public interface ApiService {
        @GET("api/files/{kelas}/{mataPelajaran}")
        Call<ResponseBody> getFiles(
                @Path("kelas") String kelas,
                @Path("mataPelajaran") String mataPelajaran
        );

        @DELETE("api/files/{publicId}")
        Call<ResponseBody> deleteFile(@Path("publicId") String publicId);

        @GET("api/status")
        Call<ResponseBody> getServerStatus();
    }
}