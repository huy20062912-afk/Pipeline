# Báo Cáo Thực Hành: The Broken Pipeline

**Môn học:** Lập trình nâng cao (INT2204)  
**Nền tảng áp dụng:** Java, Maven, GitHub Actions, JUnit 5

## 📑 Mục tiêu bài tập
Dự án được cung cấp sẵn một hệ thống CI/CD (GitHub Actions) và quản lý gói (Maven) nhưng đang bị lỗi cấu hình có chủ đích. Mục tiêu của bài thực hành là rèn luyện kỹ năng đọc log hệ thống để phân tích, tìm ra nguyên nhân và khắc phục các vấn đề kỹ thuật từ cấp độ môi trường đến mã nguồn.

Dưới đây là chi tiết quá trình rà soát và khắc phục 4 lỗi (bao gồm 3 lỗi có sẵn và 1 lỗi tự tạo theo yêu cầu).

---

## 🛠️ Chi tiết các lỗi và phương án khắc phục

### 🔴 Lỗi 1: Cấu hình GitHub Actions thiếu bước tải mã nguồn (Checkout Code)
* **Vị trí lỗi:** File `.github/workflows/ci.yml` (trong khối `steps:`)
* **Đoạn log minh chứng:**
  ```text
  The goal you specified requires a project to execute but there is no POM in this directory (/). Please verify you invoked Maven from the correct directory.
* **Giải thích nguyên nhân kỹ thuật:**
Runner của GitHub Actions khởi tạo một môi trường Ubuntu ảo hoàn toàn trống. Tệp cấu hình gốc đã gọi trực tiếp lệnh mvn package mà bỏ qua thao tác tải mã nguồn từ kho lưu trữ (repository) về máy ảo. Do không có mã nguồn và không tìm thấy file cấu hình pom.xml, Maven báo lỗi không thể khởi chạy dự án.

* **Cách sửa:** Bổ sung action actions/checkout@v4 vào đầu danh sách các bước thực thi để tải code về trước khi setup JDK và chạy Maven.  

### 🔴 Lỗi 2: Khai báo sai phiên bản thư viện (Dependency Resolution Error)
* **Vị trí lỗi:** File pom.xml (tại thẻ <version> của dependency logback-classic)
  * **Đoạn log minh chứng:**
    ```text
    Error: Failed to execute goal on project shipping-app: Could not resolve dependencies for project com.lab:shipping-app:jar:1.0-SNAPSHOT
      Error: dependency: ch.qos.logback:logback-classic:jar:9.9.9 (compile)
      Error: Could not find artifact ch.qos.logback:logback-classic:jar:9.9.9 in central ([https://repo.maven.apache.org/maven2](https://repo.maven.apache.org/maven2))
* **Giải thích nguyên nhân kỹ thuật:**
* Maven tự động quản lý và tải các thư viện từ kho trung tâm (Maven Central Repository). Tuy nhiên, phiên bản 9.9.9 của logback-classic là một phiên bản ảo, không tồn tại trên hệ thống. Do Maven không thể tải được gói phụ thuộc này, quá trình build bị đánh dấu thất bại (Build Failure) trước cả khi bước vào giai đoạn biên dịch.
* **Cách sửa:** Cập nhật phiên bản thư viện về một version hợp lệ và tồn tại trên thực tế, ví dụ 1.4.14.
    ```text
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.4.14</version>
    </dependency>
### 🔴 Lỗi 3: Lỗi biên dịch do sai thư viện kiểm thử (Compilation Error)
* **Vị trí lỗi:** File src/test/java/com/lab/ShippingCalculatorTest.java (Các dòng import thư viện ở đầu file)
* **Đoạn log minh chứng:**
    ```text
  Error: COMPILATION ERROR :
    Error: /home/runner/work/Pipeline/Pipeline/src/test/java/com/lab/ShippingCalculatorTest.java:[4,30] package org.testng.annotations does not exist
    Error: /home/runner/work/Pipeline/Pipeline/src/test/java/com/lab/ShippingCalculatorTest.java:[7,25] package org.testng does not exist
* **Giải thích nguyên nhân kỹ thuật:**
* Có sự bất đồng nhất giữa cấu hình quản lý gói và mã nguồn. File pom.xml khai báo dự án sử dụng framework kiểm thử JUnit 5 (junit-jupiter). Tuy nhiên, file Java chứa các bài test lại cố gắng import các gói từ một framework khác là TestNG (org.testng). Vì TestNG không được khai báo trong Maven dependencies, trình biên dịch (compiler) không tìm thấy class tương ứng, dẫn đến lỗi cú pháp không thể dịch mã nguồn.
* **Cách sửa:** Xóa các câu lệnh import TestNG và thay thế bằng cấu trúc import chuẩn của thư viện JUnit 5.




### 🔴 Lỗi 4 (Tự tạo thêm): Sai logic kiểm thử (Assertion Failure)
* **Vị trí lỗi:** File `src/test/java/com/lab/ShippingCalculatorTest.java` (Trong phương thức `testStandard()`)
* **Đoạn log minh chứng:**
  ```text
  [ERROR] Failures: 
  [ERROR]   ShippingCalculatorTest.testStandard:15 expected: <99999.0> but was: <15000.0>
  [INFO] 
  [ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0

* **Giải thích nguyên nhân kỹ thuật:** * Cố tình thay đổi giá trị kỳ vọng (expected) trong lệnh assertEquals thành 99999.0 để làm sai lệch logic bài toán. Khi thực thi bài test qua maven-surefire-plugin, kết quả trả về thực tế của phương thức là 15000.0 không khớp với giá trị kỳ vọng 99999.0. Việc này khiến JUnit ném ra ngoại lệ AssertionFailedError. Hệ thống CI/CD ghi nhận có một bài kiểm thử không đạt nên báo đỏ
* **Cách sửa:** Trả giá trị kỳ vọng về lại 15000.0 để đảm bảo logic hàm tính toán hoạt động chính xác.
    ```text
    @Test
    void testStandard() {
    // Sửa lại cho đúng logic
    assertEquals(15000.0, calc.calculate(5, "STANDARD"));
    }