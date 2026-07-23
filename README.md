# 🛡️ CryptoVault

**CryptoVault** là một ứng dụng ví lưu trữ ghi chú bảo mật cao (Secure Note Vault) trên nền tảng Android, hoạt động theo cơ chế **Zero-Trust** và mô hình giám sát an ninh kết hợp **Hybrid Threat Monitoring**. Ứng dụng kết hợp sức mạnh bảo mật phần cứng local với hệ thống giám sát tập trung trên nền tảng đám mây để ngăn chặn triệt để các rủi ro rò rỉ dữ liệu trước spyware và các thiết bị đã bị can thiệp (Root/Tampered).

---

## ✨ Tính Năng Cốt Lõi (Key Features)

### 1. Pháo Đài Bảo Mật Lõi (Core Security Hardening)
* **Anti-Screenshot & Overlay:** Kích hoạt `FLAG_SECURE` trên toàn bộ Activity, chặn tuyệt đối việc chụp ảnh, quay video màn hình hoặc các cuộc tấn công dạng Tapjacking (phủ giao diện giả mạo).
* **Hardware-backed Encryption:** Sử dụng cơ sở dữ liệu **Room Database** tích hợp lớp mã hóa vật lý toàn phần **SQLCipher**. Khóa giải mã DB (`Master Key`) được sinh ra và lưu trữ độc quyền trong chip bảo mật phần cứng **Android Keystore System (TEE/StrongBox)**, không bao giờ xuất hiện dưới dạng chữ rõ (Plaintext) trên bộ nhớ.

### 2. Xác Thực Đa Lớp Linh Hoạt (Multi-Factor Authentication - MFA)
* **Biometric Shield:** Tự động kích hoạt `BiometricPrompt` (Vân tay/Khuôn mặt) ngay khi mở ứng dụng để giải phóng khóa giải mã từ Keystore.
* **Mã PIN Dự Phòng Bảo Mật:** Cơ chế fallback bằng mã PIN được băm một chiều qua thuật toán **PBKDF2 kết hợp Salt ngẫu nhiên**, lưu trữ an toàn trong Jetpack DataStore nhằm chống dò mã (Bruteforce) khi máy bị trích xuất bộ nhớ.
* **Offline 2FA (TOTP):** Tích hợp xác thực lớp hai bằng cách liên kết với các ứng dụng Authenticator bên ngoài (Google/Microsoft Authenticator) dựa trên thuật toán **Time-Based One-Time Password (RFC 6238)** hoạt động hoàn toàn offline dưới máy.

### 3. Hệ Thống Giám Sát An Ninh Tập Trung (Hybrid Threat Monitoring)
* **RASP (Runtime Application Self-Protection):** Tích hợp bộ thư viện quét môi trường thời gian thực, phát hiện ngay lập tức nếu thiết bị đã bị Root, đang bật USB Debugging, chạy trên Trình giả lập (Emulator), hoặc có dấu hiệu bị Tampering.
* **Auto-Lock & Key Destruction:** Tự động xóa sạch Master Key trên RAM khi ứng dụng bị đẩy xuống nền (`onStop()`). Nếu phát hiện dò PIN hoặc xâm nhập liên tiếp, app tự kích hoạt cơ chế tự hủy (Xóa sạch SQLCipher DB).
* **Realtime Centralized Log Sync:** Khi phát hiện đe dọa, app lập tức khóa để tự vệ dưới local, đồng thời âm thầm đẩy một gói tin mã hóa chứa Metadata thiết bị và loại Threat lên hệ thống **Supabase (PostgreSQL Cloud)** để phục vụ việc giám sát tập trung trên Admin Portal.

---

## 🏗️ Kiến Trúc Hệ Thống (Architecture)

Dự án tuân thủ nghiêm ngặt mô hình **Clean Architecture** kết hợp kiến trúc **MVVM (Model-View-ViewModel)** và phân tách thư mục theo tính năng (**Package by Feature**):

* **Presentation Layer:** Xây dựng giao diện khai báo hoàn toàn bằng **Jetpack Compose** kết hợp Material 3 Adaptive Layout (tự động co giãn theo thiết bị), quản lý trạng thái UI thông qua **Kotlin Flow**.
* **Domain Layer:** Nơi chứa các nghiệp vụ cốt lõi (Business Logic) tách biệt hoàn toàn dưới dạng các `UseCases` độc lập để tối ưu cho việc viết Unit Test.
* **Data Layer:** Đóng vai trò thực thi kỹ thuật, giao tiếp trực tiếp với hệ thống mật mã Android, SQLCipher Room DB, DataStore, và gọi API REST kết nối đồng bộ dữ liệu bảo mật lên Cloud.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

* **Language:** Kotlin (Modern Android Development)
* **UI Framework:** Jetpack Compose & Material 3
* **Database:** Room Database + SQLCipher
* **Security SDKs:** Android Biometric API, Android Keystore, RootBeer (RASP)
* **Asynchronous:** Kotlin Coroutines & Flow
* **Cloud Backend:** Supabase (PostgreSQL Distributed Server)
* **Network Client:** OkHttp3
