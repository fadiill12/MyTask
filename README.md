# 📱 Aplikasi Android Pengiriman Tugas

Aplikasi Android untuk pengelolaan dan pengiriman tugas berbasis mobile dengan dua sisi pengguna: **Siswa** dan **Guru**. Aplikasi ini memanfaatkan **Firebase Realtime Database** untuk manajemen data dan **Cloudinary** melalui backend **Node.js** untuk penyimpanan file tugas.

---

## 📖 Fitur Utama

### 🔸 Sisi Siswa
- Login ke akun siswa.
- Upload tugas dalam format JPG/PNG/JPEG.
- Memilih kelas dan mata pelajaran sebelum mengunggah tugas.
- File tugas otomatis dikirim ke Cloudinary

### 🔸 Sisi Guru
- Login ke akun guru.
- Melihat daftar tugas yang diunggah oleh siswa berdasarkan kelas dan mata pelajaran.
- Download file tugas langsung melalui link Cloudinary.

---

## 🛠️ Teknologi yang Digunakan

- **Android Studio** (Java)
- **Firebase Realtime Database**
- **Node.js + Express** (sebagai backend server)
- **Cloudinary** (untuk storage file tugas JPG/PNG/JPEG)
- **SHA-256 Encryption** (untuk enkripsi password)


## 🔌 Firebase Configuration in Code

### 📍 RegisterActivity.java

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
