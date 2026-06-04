# Technical Documentation: Banking Middleware API

Dokumentasi ini menjelaskan arsitektur, fitur, dan cara penggunaan sistem Middleware Perbankan yang dibangun menggunakan Java Spring Boot.

## 1. Arsitektur Proyek
Sistem menggunakan arsitektur Layered (Berlapis) dengan standar enterprise:
- **Controller**: Menangani request HTTP dan routing.
- **Service**: Berisi logika bisnis (Transfer, Validasi Saldo, Auth, Ownership Check).
- **Repository**: Interface untuk akses data ke database (PostgreSQL via Docker).
- **Entity**: Representasi tabel database (User, Account, Transaction).
- **DTO**: Objek untuk transfer data antar layer dan response API.
- **Config**: Konfigurasi keamanan (JWT), Async, Global Exception, dan Data Seeding.

## 2. Struktur Package
```text
com.startechinnovation.userapi
├── config          # Keamanan (JWT), Async, Global Exception, Data Seeding
├── controller      # REST API Endpoints (Auth, Banking)
├── dto             # Request/Response objects & Event models
├── entity          # Model Database (User, Account, Transaction)
├── repository      # Data Access Layer (Spring Data JPA)
└── service         # Logika Bisnis, Ownership Validation & Async Processing
```

## 3. Fitur Utama

### A. Keamanan & Otorisasi (Ownership Check)
API dilindungi menggunakan **JSON Web Token (JWT)**.
- **Authentication**: Memastikan token valid.
- **Authorization (Ownership)**: Mencegah **IDOR (Insecure Direct Object Reference)**. Sistem memvalidasi bahwa pemilik token JWT adalah pemilik sah dari nomor rekening yang sedang diakses.
- **RBAC**: Hierarki role (`SUPER_ADMIN`, `BRANCH_ADMIN`, `CUSTOMER`).

### B. Response Standar
Semua response API mengikuti format:
```json
{
  "code": "00",       // 00=Success, 99=Error, 01=Unauthorized
  "message": "...",
  "data": { ... }
}
```

### C. Pemrosesan Asynchronous
Sistem menggunakan **Spring Events** untuk memisahkan proses utama transfer dengan proses pendukung (notifikasi).

### D. Database Persistence
Sistem telah bermigrasi dari H2 (In-Memory) ke **PostgreSQL**. Data tersimpan secara permanen di dalam container Docker.

---

## 4. Authentication API

### Login
| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Mendapatkan token JWT |

**Body:**
```json
{
  "username": "wisnu",
  "password": "password123"
}
```

---

## 5. Banking & Admin API

### 5.1 Cek Detail Rekening (CUSTOMER)
| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `GET` | `/api/v1/banking/account/{accNo}` | Melihat informasi akun & saldo |

### 5.2 Daftar Rekening Cabang (ADMIN)
| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `GET` | `/api/v1/admin/accounts` | Melihat daftar semua akun di cabang admin |

**Headers:**
```text
Authorization: Bearer <token_admin>
```

**Success Response:**
```json
{
  "code": "00",
  "message": "Success",
  "data": [
    {
      "id": 1,
      "accountNumber": "1234567890",
      "accountHolderName": "John Doe",
      "balance": 1000000.00,
      "createdAt": "2026-06-03T14:13:23"
    }
  ]
}
```

### 5.3 Riwayat Transaksi (CUSTOMER)
| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `GET` | `/api/v1/banking/account/{accNo}/history` | Melihat riwayat transaksi terakhir |

### 5.3 Transfer Dana (CUSTOMER)
| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `POST` | `/api/v1/banking/transfer` | Kirim uang antar rekening |

### 5.4 Manage Admin (SUPER ADMIN)
| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `POST` | `/api/v1/admin/register-admin` | Membuat akun admin baru |

**Body:**
```json
{
  "username": "branch_admin_surabaya",
  "password": "password123",
  "role": "ROLE_BRANCH_ADMIN",
  "branchCode": "SUB01"
}
```

### 5.5 Manage Cabang (SUPER ADMIN)
| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `POST` | `/api/v1/admin/branches` | Membuat cabang baru |

**Body:**
```json
{
  "name": "Bandung",
  "branchCode": "BDG01"
}
```

---

## 6. Konfigurasi Sistem
Aplikasi dapat dikonfigurasi melalui file `src/main/resources/application.properties`.

### Database (PostgreSQL)
Aplikasi terhubung ke PostgreSQL dengan detail:
- **Host**: `localhost:5432`
- **DB Name**: `banking_middleware`
- **Username**: `user`
- **Password**: `password123`

### Mengganti Port Server
Secara default, aplikasi berjalan di port **8080**.
1.  **File Config**: Ubah nilai `server.port=8080` di `application.properties`.

## 7. Cara Menjalankan

### 1. Jalankan Database (Docker)
```bash
docker run -d --name banking-db -e POSTGRES_DB=banking_middleware -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password123 -p 5432:5432 postgres:15
```

### 2. Jalankan Aplikasi
```bash
./mvnw clean spring-boot:run
```

## 8. Data Uji (Seeded Data)
Saat aplikasi dijalankan, akun berikut otomatis terdaftar di PostgreSQL:
1. **User: `super_admin`** (`ROLE_SUPER_ADMIN`)
2. **User: `branch_admin_jkt`** (`ROLE_BRANCH_ADMIN`, Cabang JKT)
3. **User: `wisnu`** (`ROLE_CUSTOMER`, Cabang JKT)
