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
    
    FirebaseDatabase database = FirebaseDatabase.getInstance("YOUR URL FIREBASE REALTIME DATABASE");
    databaseReference = database.getReference();
    
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

## 🖥️ Konfigurasi Backend

### 📍 mytask-backend/.env

Konfigurasi environment variables untuk backend Node.js:

```env
PORT=3000
CLOUDINARY_CLOUD_NAME=YOUR CLOUDINARY NAME
CLOUDINARY_API_KEY=YOUR API KEY
CLOUDINARY_API_SECRET=YOUR API SECRET
```

**Penjelasan Environment Variables:**
- `PORT`: Port server backend (default: 3000)
- `CLOUDINARY_CLOUD_NAME`: Nama cloud Cloudinary Anda
- `CLOUDINARY_API_KEY`: API Key dari dashboard Cloudinary
- `CLOUDINARY_API_SECRET`: API Secret dari dashboard Cloudinary

### 📍 Setup Backend

1. **Install Dependencies**
   ```bash
   cd mytask-backend
   npm install express multer cloudinary dotenv cors
   ```

2. **Struktur Folder Backend**
   ```
   mytask-backend/
   ├── .env
   ├── server.js
   ├── package.json
   └── uploads/ (temporary folder)
   ```

3. **Konfigurasi Cloudinary**
   - Buat akun di [Cloudinary](https://cloudinary.com/)
   - Dapatkan Cloud Name, API Key, dan API Secret dari dashboard
   - Masukkan kredensial tersebut ke file `.env`

### 📍 Endpoints Backend

Backend menyediakan endpoint untuk:
- `POST /upload`: Upload file tugas ke Cloudinary
- `GET /files`: Mengambil daftar file berdasarkan kelas dan mata pelajaran

---

## 🚀 Cara Menjalankan Aplikasi

### 🔸 Setup Backend
1. Clone repository ini
2. Masuk ke folder backend: `cd mytask-backend`
3. Install dependencies: `npm install`
4. Buat file `.env` dan isi dengan konfigurasi Cloudinary
5. Jalankan server: `npm start` atau `node server.js`

### 🔸 Setup Android App
1. Buka project di Android Studio
2. Ganti `"YOUR URL FIREBASE REALTIME DATABASE"` dengan URL Firebase Realtime Database Anda
3. Update URL backend di aplikasi Android (biasanya `http://localhost:3000` untuk testing)
4. Build dan jalankan aplikasi di device/emulator Android

### 🔸 Testing
1. Pastikan backend berjalan di port 3000
2. Test endpoint upload menggunakan Postman atau tools sejenis
3. Jalankan aplikasi Android dan test fitur upload tugas

---

## 📝 Catatan Penting

- Pastikan koneksi internet stabil untuk sinkronisasi dengan Firebase
- File tugas hanya mendukung format JPG/PNG/JPEG
- Implementasi keamanan menggunakan SHA-256 untuk password
- Session management otomatis untuk pengalaman pengguna yang lebih baik
- Backend harus berjalan sebelum menggunakan fitur upload di aplikasi Android
- Simpan file `.env` dengan aman dan jangan commit ke repository publik
- Untuk production, gunakan HTTPS dan implementasikan autentikasi yang lebih robust

---

## 🔧 Troubleshooting

### Backend Issues:
- Pastikan semua dependencies terinstall
- Periksa konfigurasi `.env` sudah benar
- Cek koneksi internet untuk akses ke Cloudinary

### Android App Issues:
- Pastikan URL Firebase sudah benar
- Cek network permissions di AndroidManifest.xml
- Verifikasi URL backend sesuai dengan server yang berjalan
