# 📱 Aplikasi Android Pengiriman Tugas

Aplikasi Android untuk pengelolaan dan pengiriman tugas berbasis mobile dengan dua sisi pengguna: **Siswa** dan **Guru**. Aplikasi ini memanfaatkan **Firebase Realtime Database** untuk manajemen data dan **Cloudinary** melalui backend **Node.js** untuk penyimpanan file tugas.

---

## 📖 Fitur Utama

### 🔸 Sisi Siswa
- Login ke akun siswa
- Upload tugas dalam format JPG/PNG/JPEG
- Memilih kelas dan mata pelajaran sebelum mengunggah tugas
- File tugas otomatis dikirim ke Cloudinary

### 🔸 Sisi Guru
- Login ke akun guru
- Melihat daftar tugas yang diunggah oleh siswa berdasarkan kelas dan mata pelajaran
- Download file tugas langsung melalui link Cloudinary

---

## 🛠️ Teknologi yang Digunakan

- **Android Studio** (Java)
- **Firebase Realtime Database**
- **Node.js + Express** (sebagai backend server)
- **Cloudinary** (untuk storage file tugas JPG/PNG/JPEG)
- **SHA-256 Encryption** (untuk enkripsi password)

---

## 🔌 Firebase Configuration in Code

### 📍 RegisterActivity.java

Konfigurasi Firebase untuk halaman registrasi pengguna baru:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register_activity);
    
    // Inisialisasi Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance("YOUR URL FIREBASE REALTIME DATABASE");
    databaseReference = database.getReference();
    
    // Setup views dan listeners
    initViews();
    setupClickListeners();
}
```

**Fungsi Utama RegisterActivity:**
- Validasi input data pengguna
- Enkripsi password menggunakan SHA-256
- Menyimpan data pengguna baru ke Firebase Realtime Database
- Redirect ke LoginActivity setelah registrasi berhasil

### 📍 LoginActivity.java

Konfigurasi Firebase untuk halaman login dan autentikasi:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
        setContentView(R.layout.login_activity);
        Log.d(TAG, "Layout set successfully");
        
        // Inisialisasi SharedPreferences untuk session management
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // Cek apakah user sudah login sebelumnya
        if (isUserLoggedIn()) {
            navigateBasedOnRole();
            return;
        }
        
        // Inisialisasi Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance("YOUR URL FIREBASE REALTIME DATABASE");
        databaseReference = database.getReference();
        Log.d(TAG, "Firebase initialized");
        
        // Setup views dan listeners
        initViews();
        setupClickListeners();
        
        // Handle data dari RegisterActivity
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
```

**Fungsi Utama LoginActivity:**
- Validasi kredensial pengguna
- Autentikasi dengan Firebase Realtime Database
- Session management menggunakan SharedPreferences
- Navigasi berdasarkan role pengguna (Siswa/Guru)
- Auto-fill username dari RegisterActivity

---

## 🔐 Alur Autentikasi

1. **Registrasi** → RegisterActivity memproses data baru dan menyimpan ke Firebase
2. **Login** → LoginActivity memvalidasi kredensial dan mengatur session
3. **Session Management** → Aplikasi mengingat status login pengguna
4. **Role-based Navigation** → Redirect otomatis ke dashboard yang sesuai

---

## 🚀 Cara Menjalankan Aplikasi

1. Clone repository ini
2. Buka project di Android Studio
3. Ganti `"YOUR URL FIREBASE REALTIME DATABASE"` dengan URL Firebase Realtime Database Anda
4. Setup backend Node.js untuk integrasi Cloudinary
5. Build dan jalankan aplikasi di device/emulator Android

---

## 📝 Catatan Penting

- Pastikan koneksi internet stabil untuk sinkronisasi dengan Firebase
- File tugas hanya mendukung format JPG/PNG/JPEG
- Implementasi keamanan menggunakan SHA-256 untuk password
- Session management otomatis untuk pengalaman pengguna yang lebih baik
