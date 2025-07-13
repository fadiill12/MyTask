package com.example.mytask;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNama, etNisNip, etUsername, etPassword;
    private Spinner spinnerRole;
    private LinearLayout btnDaftar;
    private ImageView btnBack;
    private TextView tvLogin;

    private DatabaseReference databaseReference;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        FirebaseDatabase database = FirebaseDatabase.getInstance("YOUR URL FIREBASE REALTIME DATABASE");
        databaseReference = database.getReference();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etNama = findViewById(R.id.etNama);
        etNisNip = findViewById(R.id.etNisNip);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnDaftar = findViewById(R.id.btnDaftar);
        btnBack = findViewById(R.id.btnBack);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupClickListeners() {
        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRegistering) {
                    registerUser();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        String nama = etNama.getText().toString().trim();
        String nisNip = etNisNip.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        // Validasi input
        if (!validateInput(nama, nisNip, username, password, role)) {
            return;
        }

        isRegistering = true;
        showLoading(true);

        // Cek apakah username sudah ada
        databaseReference.child("users").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            showLoading(false);
                            isRegistering = false;
                            Toast.makeText(RegisterActivity.this,
                                    "Username sudah digunakan, silakan pilih username lain",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Cek apakah NIS/NIP sudah ada
                            checkNisNipExists(nama, nisNip, username, password, role);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        isRegistering = false;
                        Toast.makeText(RegisterActivity.this,
                                "Gagal memeriksa username: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkNisNipExists(String nama, String nisNip, String username, String password, String role) {
        databaseReference.child("users").orderByChild("nisNip").equalTo(nisNip)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            showLoading(false);
                            isRegistering = false;
                            Toast.makeText(RegisterActivity.this,
                                    "NIS/NIP sudah terdaftar",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Simpan user baru
                            saveNewUser(nama, nisNip, username, password, role);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        isRegistering = false;
                        Toast.makeText(RegisterActivity.this,
                                "Gagal memeriksa NIS/NIP: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveNewUser(String nama, String nisNip, String username, String password, String role) {
        // Generate unique ID
        String userId = databaseReference.child("users").push().getKey();

        if (userId != null) {
            // Hash password dengan SHA-256
            String hashedPassword = hashPassword(password);

            if (hashedPassword == null) {
                showLoading(false);
                isRegistering = false;
                Toast.makeText(this, "Gagal mengenkripsi password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user object
            User user = new User(userId, nama, nisNip, username, hashedPassword, role,
                    System.currentTimeMillis());

            // Save to Firebase
            databaseReference.child("users").child(userId).setValue(user)
                    .addOnCompleteListener(task -> {
                        showLoading(false);
                        isRegistering = false;

                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Registrasi berhasil! Silakan login.",
                                    Toast.LENGTH_LONG).show();

                            // Clear form
                            clearForm();

                            // Navigate to LoginActivity
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("registered_username", username);
                            startActivity(intent);
                            finish();

                        } else {
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Gagal menyimpan data";
                            Toast.makeText(RegisterActivity.this,
                                    "Registrasi gagal: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            showLoading(false);
            isRegistering = false;
            Toast.makeText(this, "Gagal membuat ID pengguna", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String nama, String nisNip, String username, String password, String role) {
        // Validasi nama
        if (TextUtils.isEmpty(nama)) {
            etNama.setError("Nama lengkap harus diisi");
            etNama.requestFocus();
            return false;
        }

        if (nama.length() < 2) {
            etNama.setError("Nama minimal 2 karakter");
            etNama.requestFocus();
            return false;
        }

        // Validasi NIS/NIP
        if (TextUtils.isEmpty(nisNip)) {
            etNisNip.setError("NIS/NIP harus diisi");
            etNisNip.requestFocus();
            return false;
        }

        if (nisNip.length() < 8) {
            etNisNip.setError("NIS/NIP minimal 8 digit");
            etNisNip.requestFocus();
            return false;
        }

        // Validasi username
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username harus diisi");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            etUsername.setError("Username minimal 3 karakter");
            etUsername.requestFocus();
            return false;
        }

        if (!isValidUsername(username)) {
            etUsername.setError("Username hanya boleh mengandung huruf, angka, dan underscore");
            etUsername.requestFocus();
            return false;
        }

        // Validasi password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password harus diisi");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return false;
        }

        // Validasi role
        if (role.equals("Pilih Role") || TextUtils.isEmpty(role)) {
            Toast.makeText(this, "Silakan pilih role", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Pastikan role hanya Siswa atau Guru
        if (!role.equals("Siswa") && !role.equals("Guru")) {
            Toast.makeText(this, "Role hanya boleh Siswa atau Guru", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Hash password menggunakan SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isValidUsername(String username) {
        // Username hanya boleh mengandung huruf, angka, dan underscore
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
        return pattern.matcher(username).matches();
    }

    private void showLoading(boolean show) {
        if (show) {
            btnDaftar.setEnabled(false);
            btnDaftar.setAlpha(0.6f);
            // Anda bisa menambahkan progress bar di sini
        } else {
            btnDaftar.setEnabled(true);
            btnDaftar.setAlpha(1.0f);
        }
    }

    private void clearForm() {
        etNama.setText("");
        etNisNip.setText("");
        etUsername.setText("");
        etPassword.setText("");
        spinnerRole.setSelection(0);
    }

    // User model class
    public static class User {
        public String id;
        public String nama;
        public String nisNip;
        public String username;
        public String password;
        public String role;
        public long createdAt;

        public User() {
            // Default constructor required for Firebase
        }

        public User(String id, String nama, String nisNip, String username,
                    String password, String role, long createdAt) {
            this.id = id;
            this.nama = nama;
            this.nisNip = nisNip;
            this.username = username;
            this.password = password;
            this.role = role;
            this.createdAt = createdAt;
        }

        // Getters (required for Firebase)
        public String getId() { return id; }
        public String getNama() { return nama; }
        public String getNisNip() { return nisNip; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getRole() { return role; }
        public long getCreatedAt() { return createdAt; }

        // Setters (required for Firebase)
        public void setId(String id) { this.id = id; }
        public void setNama(String nama) { this.nama = nama; }
        public void setNisNip(String nisNip) { this.nisNip = nisNip; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
        public void setRole(String role) { this.role = role; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    }
}
