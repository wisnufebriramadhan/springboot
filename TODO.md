# Banking Middleware API - Roadmap & TODO

Daftar fitur dan peningkatan yang direncanakan untuk tahap pengembangan selanjutnya.

## Phase 1: Payment Services (High Priority)
- [ ] **Virtual Account (VA)**: Implementasi generator nomor VA dan sistem *callback* pembayaran.
- [ ] **QRIS Support**: Simulasi integrasi QRIS dinamis untuk transaksi *cashless*.
- [ ] **Transfer Antar Bank**: Simulasi integrasi dengan pihak ketiga (switching) untuk transfer ke bank lain.

## Phase 2: Security & Resilience
- [ ] **Rate Limiting**: Membatasi jumlah request per menit menggunakan Bucket4j untuk mencegah penyalahgunaan API.
- [ ] **Refresh Token**: Implementasi siklus hidup token yang lebih panjang dan rotasi token yang aman.
- [ ] **API Quota per Role**: Membedakan limit request antara Customer biasa dan Merchant/Admin.
- [ ] **IP Whitelisting**: Membatasi akses API Admin hanya dari IP tertentu.

## Phase 3: Monitoring & Observability
- [ ] **Spring Boot Actuator**: Aktivasi endpoint monitoring kesehatan aplikasi.
- [ ] **Prometheus & Grafana**: Setup dashboard visual untuk memantau trafik transaksi dan penggunaan resource server.
- [ ] **ELK Stack / Centralized Log**: Integrasi pengumpulan log ke satu tempat untuk mempermudah debugging di VPS.

## Phase 4: Operational & Batch Processing
- [ ] **Daily Settlement Job**: Penjadwalan otomatis (Scheduler) untuk rangkuman transaksi harian setiap tengah malam.
- [ ] **Email/SMS Notification Gateway**: Integrasi nyata dengan provider pengirim pesan (bukan lagi simulasi log).
- [ ] **Audit Trail Dashboard**: UI sederhana untuk Super Admin melihat log aktivitas dari tabel `audit_logs`.

## Phase 5: Documentation & UX
- [ ] **OpenAPI / Swagger Customization**: Menambahkan contoh response error yang lebih mendalam dan deskripsi field per model.
- [ ] **Postman Collection**: Menyediakan file JSON Postman yang siap di-import lengkap dengan environment variable.
