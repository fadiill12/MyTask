// server.js
const express = require('express');
const multer = require('multer');
const cloudinary = require('cloudinary').v2;
const cors = require('cors');
const path = require('path');
const fs = require('fs');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Enhanced ANSI color codes with more vibrant colors
const colors = {
    reset: '\x1b[0m',
    bright: '\x1b[1m',
    dim: '\x1b[2m',
    underscore: '\x1b[4m',
    blink: '\x1b[5m',
    reverse: '\x1b[7m',
    hidden: '\x1b[8m',
    
    // Foreground colors
    black: '\x1b[30m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m',
    white: '\x1b[37m',
    
    // Bright foreground colors
    brightBlack: '\x1b[90m',
    brightRed: '\x1b[91m',
    brightGreen: '\x1b[92m',
    brightYellow: '\x1b[93m',
    brightBlue: '\x1b[94m',
    brightMagenta: '\x1b[95m',
    brightCyan: '\x1b[96m',
    brightWhite: '\x1b[97m',
    
    // Background colors
    bgBlack: '\x1b[40m',
    bgRed: '\x1b[41m',
    bgGreen: '\x1b[42m',
    bgYellow: '\x1b[43m',
    bgBlue: '\x1b[44m',
    bgMagenta: '\x1b[45m',
    bgCyan: '\x1b[46m',
    bgWhite: '\x1b[47m',
    
    // Bright background colors
    bgBrightBlack: '\x1b[100m',
    bgBrightRed: '\x1b[101m',
    bgBrightGreen: '\x1b[102m',
    bgBrightYellow: '\x1b[103m',
    bgBrightBlue: '\x1b[104m',
    bgBrightMagenta: '\x1b[105m',
    bgBrightCyan: '\x1b[106m',
    bgBrightWhite: '\x1b[107m'
};

// Function to create colorful borders
function createBorder(char = '=', length = 80, color = colors.brightCyan) {
    return color + char.repeat(length) + colors.reset;
}

// Function to create colorful box
function createBox(text, borderColor = colors.brightMagenta, textColor = colors.brightYellow) {
    const border = '█'.repeat(text.length + 4);
    return borderColor + border + colors.reset + '\n' +
           borderColor + '█ ' + colors.reset + textColor + text + colors.reset + borderColor + ' █' + colors.reset + '\n' +
           borderColor + border + colors.reset;
}

// Function to format file size
function formatFileSize(bytes) {
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    if (bytes === 0) return '0 Bytes';
    const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
    return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i];
}

// Middleware untuk logging semua request
app.use((req, res, next) => {
    const timestamp = new Date().toISOString();
    console.log('\n' + createBorder('═', 80, colors.brightBlue));
    console.log(colors.brightCyan + colors.bright + `[${timestamp}] ${req.method} ${req.url}` + colors.reset);
    console.log(colors.yellow + 'Headers:' + colors.reset, JSON.stringify(req.headers, null, 2));
    if (req.body && Object.keys(req.body).length > 0) {
        console.log(colors.green + 'Body:' + colors.reset, JSON.stringify(req.body, null, 2));
    }
    if (req.params && Object.keys(req.params).length > 0) {
        console.log(colors.magenta + 'Params:' + colors.reset, JSON.stringify(req.params, null, 2));
    }
    if (req.query && Object.keys(req.query).length > 0) {
        console.log(colors.cyan + 'Query:' + colors.reset, JSON.stringify(req.query, null, 2));
    }
    console.log(createBorder('═', 80, colors.brightBlue));
    next();
});

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Konfigurasi Cloudinary
cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
    api_key: process.env.CLOUDINARY_API_KEY,
    api_secret: process.env.CLOUDINARY_API_SECRET
});

// Konfigurasi Multer untuk menyimpan file sementara
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        const uploadDir = 'uploads/';
        if (!fs.existsSync(uploadDir)) {
            fs.mkdirSync(uploadDir, { recursive: true });
        }
        cb(null, uploadDir);
    },
    filename: function (req, file, cb) {
        // Buat nama file unik
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, uniqueSuffix + path.extname(file.originalname));
    }
});

const upload = multer({ 
    storage: storage,
    limits: {
        fileSize: 10 * 1024 * 1024 // 10MB limit
    },
    fileFilter: function (req, file, cb) {
        // Filter jenis file yang diizinkan
        const allowedTypes = [
            'image/jpeg', 'image/png', 'image/gif',
            'application/pdf',
            'application/msword',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'text/plain'
        ];
        
        if (allowedTypes.includes(file.mimetype)) {
            cb(null, true);
        } else {
            cb(new Error('Jenis file tidak diizinkan'), false);
        }
    }
});

// Fungsi untuk menentukan resource_type berdasarkan mimetype
function getResourceType(mimetype) {
    if (mimetype.startsWith('image/')) {
        return 'image';
    } else if (mimetype.startsWith('video/')) {
        return 'video';
    } else {
        return 'raw'; // untuk dokumen, pdf, dll
    }
}

// Fungsi untuk membersihkan nama folder
function sanitizeFolderName(name) {
    return name.toLowerCase()
               .replace(/\s+/g, '_')
               .replace(/[^a-z0-9_]/g, '');
}

// Enhanced API endpoint untuk upload file dengan warna-warna cerah
app.post('/api/upload', upload.single('file'), async (req, res) => {
    try {
        console.log('\n' + createBorder('🎨', 100, colors.brightMagenta));
        console.log(createBox('📤 UPLOAD REQUEST FROM DASHBOARD SISWA 🎓', colors.brightGreen, colors.brightYellow));
        console.log(createBorder('🎨', 100, colors.brightMagenta));
        
        // Student upload indicator with vibrant colors
        console.log(colors.bgBrightBlue + colors.brightWhite + colors.bright + '  👨‍🎓 STUDENT UPLOAD ACTIVITY  ' + colors.reset);
        console.log(colors.brightCyan + '📱 Request from: ' + colors.brightYellow + colors.bright + 'DashboardSiswaActivity.java' + colors.reset);
        console.log(colors.brightMagenta + '⏰ Timestamp: ' + colors.brightGreen + new Date().toLocaleString('id-ID') + colors.reset);
        
        // File information with enhanced colors
        if (req.file) {
            console.log('\n' + colors.bgBrightGreen + colors.black + colors.bright + ' 📁 FILE INFORMATION ' + colors.reset);
            console.log(colors.brightBlue + '📄 Original name: ' + colors.brightYellow + colors.bright + req.file.originalname + colors.reset);
            console.log(colors.brightBlue + '🎭 MIME type: ' + colors.brightMagenta + colors.bright + req.file.mimetype + colors.reset);
            console.log(colors.brightBlue + '📊 File size: ' + colors.brightCyan + colors.bright + formatFileSize(req.file.size) + colors.reset);
            console.log(colors.brightBlue + '📍 Temp path: ' + colors.brightGreen + req.file.path + colors.reset);
            
            // Visual file size indicator
            const sizeInMB = req.file.size / (1024 * 1024);
            const sizeBar = '█'.repeat(Math.min(Math.floor(sizeInMB), 20));
            const emptySizeBar = '░'.repeat(Math.max(20 - Math.floor(sizeInMB), 0));
            console.log(colors.brightBlue + '📈 Size visual: [' + colors.brightGreen + sizeBar + colors.brightBlack + emptySizeBar + colors.brightBlue + '] ' + colors.brightYellow + sizeInMB.toFixed(2) + 'MB' + colors.reset);
        } else {
            console.log(colors.bgBrightRed + colors.brightWhite + colors.bright + ' ❌ NO FILE DETECTED ' + colors.reset);
        }

        // Request body information with colorful display
        console.log('\n' + colors.bgBrightCyan + colors.black + colors.bright + ' 📋 FORM DATA ' + colors.reset);
        console.log(colors.brightGreen + '🎯 Kelas: ' + colors.brightYellow + colors.bright + (req.body.kelas || 'Not provided') + colors.reset);
        console.log(colors.brightGreen + '📚 Mata Pelajaran: ' + colors.brightMagenta + colors.bright + (req.body.mataPelajaran || 'Not provided') + colors.reset);

        // Validasi input dengan pesan berwarna
        if (!req.file) {
            console.log('\n' + colors.bgBrightRed + colors.brightWhite + colors.bright + ' ❌ UPLOAD FAILED: NO FILE ' + colors.reset);
            return res.status(400).json({
                success: false,
                message: 'Tidak ada file yang diupload'
            });
        }

        const { kelas, mataPelajaran } = req.body;
        
        if (!kelas || !mataPelajaran) {
            console.log('\n' + colors.bgBrightRed + colors.brightWhite + colors.bright + ' ❌ UPLOAD FAILED: MISSING DATA ' + colors.reset);
            console.log(colors.brightRed + '❌ Missing: ' + (!kelas ? 'Kelas ' : '') + (!mataPelajaran ? 'Mata Pelajaran' : '') + colors.reset);
            // Hapus file temporary
            fs.unlinkSync(req.file.path);
            return res.status(400).json({
                success: false,
                message: 'Kelas dan mata pelajaran harus diisi'
            });
        }

        // Processing dengan animasi loading visual
        console.log('\n' + colors.bgBrightYellow + colors.black + colors.bright + ' 🔄 PROCESSING UPLOAD ' + colors.reset);
        
        // Sanitize folder names
        const kelasFolder = sanitizeFolderName(kelas);
        const mataPelajaranFolder = sanitizeFolderName(mataPelajaran);
        
        // Buat path folder: kelas/mata_pelajaran
        const folderPath = `${kelasFolder}/${mataPelajaranFolder}`;
        
        // Tentukan resource type
        const resourceType = getResourceType(req.file.mimetype);
        
        // Generate public_id yang unik
        const timestamp = Date.now();
        const fileName = path.parse(req.file.originalname).name;
        const publicId = `${folderPath}/${fileName}_${timestamp}`;

        console.log(colors.brightCyan + '📂 Original kelas: "' + colors.brightYellow + kelas + colors.brightCyan + '" → Sanitized: "' + colors.brightGreen + kelasFolder + colors.brightCyan + '"' + colors.reset);
        console.log(colors.brightCyan + '📚 Original mata pelajaran: "' + colors.brightMagenta + mataPelajaran + colors.brightCyan + '" → Sanitized: "' + colors.brightGreen + mataPelajaranFolder + colors.brightCyan + '"' + colors.reset);
        console.log(colors.brightBlue + '🗂️ Final folder path: ' + colors.bgBrightMagenta + colors.brightWhite + ' ' + folderPath + ' ' + colors.reset);
        console.log(colors.brightBlue + '🆔 Public ID: ' + colors.brightCyan + publicId + colors.reset);
        console.log(colors.brightBlue + '🎭 Resource type: ' + colors.brightGreen + colors.bright + resourceType.toUpperCase() + colors.reset);

        // Visual upload progress
        console.log('\n' + colors.bgBrightBlue + colors.brightWhite + colors.bright + ' ☁️ UPLOADING TO CLOUDINARY ' + colors.reset);
        const loadingChars = ['⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏'];
        let loadingIndex = 0;
        const loadingInterval = setInterval(() => {
            process.stdout.write('\r' + colors.brightYellow + loadingChars[loadingIndex] + colors.brightCyan + ' Uploading to cloud storage...' + colors.reset);
            loadingIndex = (loadingIndex + 1) % loadingChars.length;
        }, 100);

        // Upload ke Cloudinary
        const uploadResult = await cloudinary.uploader.upload(req.file.path, {
            public_id: publicId,
            resource_type: resourceType,
            folder: folderPath,
            use_filename: true,
            unique_filename: false,
            overwrite: true
        });

        clearInterval(loadingInterval);
        process.stdout.write('\r' + ' '.repeat(50) + '\r'); // Clear loading line

        console.log('\n' + colors.bgBrightGreen + colors.black + colors.bright + ' ✅ CLOUDINARY UPLOAD SUCCESS! ' + colors.reset);
        console.log(colors.brightGreen + '🌟 Upload completed successfully!' + colors.reset);
        console.log(colors.brightBlue + '🔗 Secure URL: ' + colors.brightCyan + colors.underscore + uploadResult.secure_url + colors.reset);
        console.log(colors.brightBlue + '📊 Size: ' + colors.brightYellow + formatFileSize(uploadResult.bytes) + colors.reset);
        console.log(colors.brightBlue + '🎭 Format: ' + colors.brightMagenta + uploadResult.format.toUpperCase() + colors.reset);
        console.log(colors.brightBlue + '⏱️ Upload time: ' + colors.brightGreen + new Date().toLocaleTimeString('id-ID') + colors.reset);

        // Hapus file temporary
        fs.unlinkSync(req.file.path);
        console.log(colors.brightYellow + '🧹 Temporary file cleaned up' + colors.reset);

        const response = {
            success: true,
            message: 'File berhasil diupload',
            url: uploadResult.secure_url,
            public_id: uploadResult.public_id,
            folder: folderPath,
            kelas: kelas,
            mataPelajaran: mataPelajaran,
            originalName: req.file.originalname,
            size: req.file.size,
            uploadedAt: new Date().toISOString()
        };

        console.log('\n' + colors.bgBrightMagenta + colors.brightWhite + colors.bright + ' 📱 RESPONSE TO ANDROID APP ' + colors.reset);
        console.log(colors.brightGreen + JSON.stringify(response, null, 2) + colors.reset);
        
        // Success celebration
        console.log('\n' + colors.brightGreen + '🎉'.repeat(20) + colors.reset);
        console.log(colors.brightYellow + colors.bright + '🎊 STUDENT UPLOAD COMPLETED SUCCESSFULLY! 🎊' + colors.reset);
        console.log(colors.brightGreen + '🎉'.repeat(20) + colors.reset);
        
        res.json(response);

    } catch (error) {
        console.log('\n' + colors.bgBrightRed + colors.brightWhite + colors.bright + ' 💥 UPLOAD ERROR OCCURRED ' + colors.reset);
        console.error(colors.brightRed + '❌ Upload error details:' + colors.reset);
        console.error(colors.red + error.message + colors.reset);
        console.error(colors.brightRed + 'Stack trace:' + colors.reset);
        console.error(colors.red + error.stack + colors.reset);
        
        // Hapus file temporary jika ada error
        if (req.file && fs.existsSync(req.file.path)) {
            fs.unlinkSync(req.file.path);
            console.log(colors.yellow + '🧹 Temporary file cleaned up after error' + colors.reset);
        }

        const errorResponse = {
            success: false,
            message: 'Gagal mengupload file',
            error: error.message
        };

        console.log('\n' + colors.bgBrightRed + colors.brightWhite + colors.bright + ' 📱 ERROR RESPONSE TO ANDROID ' + colors.reset);
        console.log(colors.brightRed + JSON.stringify(errorResponse, null, 2) + colors.reset);

        res.status(500).json(errorResponse);
    }
});

// API endpoint untuk mendapatkan daftar file berdasarkan kelas dan mata pelajaran
app.get('/api/files/:kelas/:mataPelajaran', async (req, res) => {
    try {
        const { kelas, mataPelajaran } = req.params;
        
        console.log(colors.brightGreen + '\n📂 LOAD FILES REQUEST RECEIVED FROM ANDROID' + colors.reset);
        console.log(colors.green + '🎯 Request from DashboardGuruActivity' + colors.reset);
        console.log(colors.green + 'Original kelas: ' + colors.cyan + kelas + colors.reset);
        console.log(colors.green + 'Original mataPelajaran: ' + colors.cyan + mataPelajaran + colors.reset);
        
        const kelasFolder = sanitizeFolderName(kelas);
        const mataPelajaranFolder = sanitizeFolderName(mataPelajaran);
        const folderPath = `${kelasFolder}/${mataPelajaranFolder}`;
        
        console.log(colors.green + 'Sanitized kelas folder: ' + colors.yellow + kelasFolder + colors.reset);
        console.log(colors.green + 'Sanitized mata pelajaran folder: ' + colors.yellow + mataPelajaranFolder + colors.reset);
        console.log(colors.green + 'Final folder path: ' + colors.magenta + folderPath + colors.reset);
        console.log(colors.brightGreen + '🔍 Searching in Cloudinary...' + colors.reset);

        // Cari semua file dalam folder
        const result = await cloudinary.search
            .expression(`folder:${folderPath}`)
            .max_results(100)
            .execute();

        console.log(colors.brightGreen + '📊 Cloudinary search results:' + colors.reset);
        console.log(colors.green + 'Total files found: ' + colors.cyan + result.total_count + colors.reset);
        console.log(colors.green + 'Resources count: ' + colors.cyan + (result.resources ? result.resources.length : 0) + colors.reset);
        
        if (result.resources && result.resources.length > 0) {
            console.log(colors.brightGreen + '📋 File details:' + colors.reset);
            result.resources.forEach((file, index) => {
                console.log(colors.green + `  ${index + 1}. ` + colors.blue + file.public_id + colors.reset);
                console.log(colors.green + '     - URL: ' + colors.cyan + file.secure_url + colors.reset);
                console.log(colors.green + '     - Format: ' + colors.yellow + file.format + colors.reset);
                console.log(colors.green + '     - Size: ' + colors.magenta + file.bytes + ' bytes' + colors.reset);
                console.log(colors.green + '     - Created: ' + colors.cyan + file.created_at + colors.reset);
            });
        } else {
            console.log(colors.yellow + '📭 No files found in this folder' + colors.reset);
        }

        const response = {
            success: true,
            files: result.resources,
            total: result.total_count
        };

        console.log(colors.brightGreen + '📱 Response to Android:' + colors.reset);
        console.log(colors.green + JSON.stringify(response, null, 2) + colors.reset);
        
        res.json(response);

    } catch (error) {
        console.error(colors.red + '❌ Get files error:' + colors.reset, error);
        console.error(colors.red + 'Error details:' + colors.reset, error.stack);
        
        const errorResponse = {
            success: false,
            message: 'Gagal mengambil daftar file',
            error: error.message
        };
        
        console.log(colors.red + '📱 Error response to Android:' + colors.reset, JSON.stringify(errorResponse, null, 2));
        res.status(500).json(errorResponse);
    }
});

// API endpoint untuk menghapus file
app.delete('/api/files/:publicId', async (req, res) => {
    try {
        const { publicId } = req.params;
        const decodedPublicId = decodeURIComponent(publicId);
        
        console.log('\n🗑️ DELETE FILE REQUEST');
        console.log('Public ID:', publicId);
        console.log('Decoded Public ID:', decodedPublicId);
        
        // Hapus dari Cloudinary
        const result = await cloudinary.uploader.destroy(decodedPublicId);
        
        console.log('Cloudinary delete result:', result);
        
        if (result.result === 'ok') {
            console.log('✅ File deleted successfully');
            res.json({
                success: true,
                message: 'File berhasil dihapus'
            });
        } else {
            console.log('❌ File not found or delete failed');
            res.status(404).json({
                success: false,
                message: 'File tidak ditemukan'
            });
        }

    } catch (error) {
        console.error('❌ Delete file error:', error);
        res.status(500).json({
            success: false,
            message: 'Gagal menghapus file',
            error: error.message
        });
    }
});

// API endpoint untuk mendapatkan info server
app.get('/api/status', (req, res) => {
    console.log('\n🏥 SERVER STATUS REQUEST');
    
    const response = {
        success: true,
        message: 'Server berjalan dengan baik',
        timestamp: new Date().toISOString(),
        cloudinary: {
            configured: !!(cloudinary.config().cloud_name && 
                          cloudinary.config().api_key && 
                          cloudinary.config().api_secret)
        }
    };
    
    console.log('Status response:', JSON.stringify(response, null, 2));
    res.json(response);
});

// Error handling middleware
app.use((error, req, res, next) => {
    console.error('\n💥 ERROR MIDDLEWARE TRIGGERED');
    console.error('Error:', error);
    
    if (error instanceof multer.MulterError) {
        if (error.code === 'LIMIT_FILE_SIZE') {
            console.log('❌ File too large error');
            return res.status(400).json({
                success: false,
                message: 'File terlalu besar. Maksimal 10MB'
            });
        }
    }
    
    console.log('❌ General server error');
    res.status(500).json({
        success: false,
        message: error.message || 'Terjadi kesalahan server'
    });
});

// 404 handler
app.use('*', (req, res) => {
    console.log('\n❌ 404 - Endpoint not found:', req.originalUrl);
    res.status(404).json({
        success: false,
        message: 'Endpoint tidak ditemukan'
    });
});

// Mulai server
app.listen(PORT, () => {
    console.log(colors.brightGreen + '\n' + '🚀'.repeat(20) + colors.reset);
    console.log(colors.brightGreen + `🌟 SERVER STARTED SUCCESSFULLY` + colors.reset);
    console.log(colors.green + `🌐 Server running on port ` + colors.cyan + PORT + colors.reset);
    console.log(colors.green + `📊 API Status: ` + colors.blue + `http://localhost:${PORT}/api/status` + colors.reset);
    console.log(colors.green + `📁 Upload endpoint: ` + colors.blue + `POST http://localhost:${PORT}/api/upload` + colors.reset);
    console.log(colors.green + `📂 Get files endpoint: ` + colors.blue + `GET http://localhost:${PORT}/api/files/{kelas}/{mataPelajaran}` + colors.reset);
    console.log(colors.green + `🗑️ Delete file endpoint: ` + colors.blue + `DELETE http://localhost:${PORT}/api/files/{publicId}` + colors.reset);
    
    // Cek konfigurasi Cloudinary
    const config = cloudinary.config();
    if (!config.cloud_name || !config.api_key || !config.api_secret) {
        console.log(colors.yellow + '\n⚠️  WARNING: Cloudinary configuration incomplete!' + colors.reset);
        console.log(colors.yellow + '⚠️  Make sure to set CLOUD_NAME, API_KEY, and API_SECRET' + colors.reset);
    } else {
        console.log(colors.green + `\n☁️  Cloudinary configured for: ` + colors.cyan + config.cloud_name + colors.reset);
        console.log(colors.brightGreen + '✅ All configurations ready!' + colors.reset);
    }
    
    console.log(colors.brightGreen + '\n👀 Waiting for requests from Android app...' + colors.reset);
    console.log(colors.green + '🔍 All API calls will be logged with detailed information' + colors.reset);
    console.log(colors.brightYellow + '📤 Student uploads will be shown in VIBRANT COLORS!' + colors.reset);
    console.log(colors.green + '💚 Load data requests will be shown in GREEN color!' + colors.reset);
    console.log(colors.green + '='.repeat(60) + colors.reset);
});

module.exports = app;