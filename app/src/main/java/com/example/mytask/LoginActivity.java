package com.example.mytask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etUsername, etPassword;
    private LinearLayout btnLogin;
    private ImageView btnBack;
    private TextView tvLogin;

    private DatabaseReference databaseReference;
    private boolean isLoggingIn = false;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyTaskPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.login_activity);
            Log.d(TAG, "Layout set successfully");

            sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

            if (isUserLoggedIn()) {
                navigateBasedOnRole();
                return;
            }

            FirebaseDatabase database = FirebaseDatabase.getInstance("YOUR URL FIREBASE REALTIME DATABASE");
            databaseReference = database.getReference();
            Log.d(TAG, "Firebase initialized");

            initViews();
            setupClickListeners();

            Intent intent = getIntent();
            if (intent.hasExtra("registered_username")) {
                String username = intent.getStringExtra("registered_username");
                etUsername.setText(username);
                etPassword.requestFocus();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing login: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void navigateBasedOnRole() {
        try {
            String role = sharedPreferences.getString(KEY_ROLE, "");
            String username = sharedPreferences.getString(KEY_USERNAME, "");

            Log.d(TAG, "Navigating - Role: " + role + ", Username: " + username);

            if (TextUtils.isEmpty(role) || TextUtils.isEmpty(username)) {
                Log.w(TAG, "Empty role or username, performing logout");
                performLogout();
                return;
            }

            Intent intent = null;

            if ("Siswa".equals(role)) {
                intent = new Intent(this, DashboardSiswaActivity.class);
                intent.putExtra("username", username);
                Log.d(TAG, "Navigating to DashboardSiswaActivity");
            } else if ("Guru".equals(role)) {
                intent = new Intent(this, DashboardGuruActivity.class);
                intent.putExtra("username", username);
                Log.d(TAG, "Navigating to DashboardGuruActivity");

            } else {
                Log.w(TAG, "Unknown role: " + role);
                performLogout();
                return;
            }

            if (intent != null) {
                startActivity(intent);
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in navigateBasedOnRole", e);
            Toast.makeText(this, "Error navigating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            performLogout();
        }
    }

    private void initViews() {
        try {
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            btnBack = findViewById(R.id.btnBack);
            tvLogin = findViewById(R.id.tvLogin);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLoggingIn) {
                    loginUser();
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
                // Navigate to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Login attempt for username: " + username);

        // Validasi input
        if (!validateInput(username, password)) {
            return;
        }

        isLoggingIn = true;
        showLoading(true);

        databaseReference.child("users").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                RegisterActivity.User user = userSnapshot.getValue(RegisterActivity.User.class);
                                if (user != null) {
                                    String hashedPassword = hashPassword(password);

                                    if (hashedPassword != null && hashedPassword.equals(user.getPassword())) {
                                        Log.d(TAG, "Login successful for user: " + username + ", role: " + user.getRole());

                                        showLoading(false);
                                        isLoggingIn = false;

                                        saveLoginSession(username, user.getRole());

                                        String welcomeMessage = user.getRole().equals("Guru") ?
                                                "Selamat datang Guru " + username :
                                                "Selamat datang Siswa " + username;

                                        Toast.makeText(LoginActivity.this,
                                                welcomeMessage,
                                                Toast.LENGTH_LONG).show();
                                        clearForm();
                                        navigateToRoleDashboard(user.getRole(), username);

                                    } else {
                                        Log.w(TAG, "Wrong password for user: " + username);
                                        showLoading(false);
                                        isLoggingIn = false;
                                        Toast.makeText(LoginActivity.this,
                                                "Username atau password salah",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                }
                            }
                        } else {
                            Log.w(TAG, "User not found: " + username);
                            showLoading(false);
                            isLoggingIn = false;
                            Toast.makeText(LoginActivity.this,
                                    "Username atau password salah",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error during login", error.toException());
                        showLoading(false);
                        isLoggingIn = false;
                        Toast.makeText(LoginActivity.this,
                                "Gagal melakukan login: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveLoginSession(String username, String role) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();

        Log.d(TAG, "Login session saved - Username: " + username + ", Role: " + role);
    }

    public static void performLogout(android.content.Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).finish();
        }
    }

    private void performLogout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRoleDashboard(String role, String username) {
        try {
            Intent intent = null;

            if ("Siswa".equals(role)) {
                intent = new Intent(LoginActivity.this, DashboardSiswaActivity.class);
                intent.putExtra("username", username);
                Log.d(TAG, "Creating intent for DashboardSiswaActivity");
            } else if ("Guru".equals(role)) {
                intent = new Intent(LoginActivity.this, DashboardGuruActivity.class);
                intent.putExtra("username", username);
                Log.d(TAG, "Creating intent for DashboardGuruActivity");

            } else {
                Log.w(TAG, "Unknown role in navigateToRoleDashboard: " + role);
                Toast.makeText(this, "Role tidak dikenali", Toast.LENGTH_SHORT).show();
                return;
            }

            if (intent != null) {
                Log.d(TAG, "Starting activity for role: " + role);
                startActivity(intent);
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in navigateToRoleDashboard", e);
            Toast.makeText(this, "Error navigating to dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInput(String username, String password) {
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

        return true;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));

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
            Log.e(TAG, "Error hashing password", e);
            e.printStackTrace();
            return null;
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.6f);
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1.0f);
        }
    }

    private void clearForm() {
        etUsername.setText("");
        etPassword.setText("");
    }
}