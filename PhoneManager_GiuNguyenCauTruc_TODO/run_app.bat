@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

if not exist "target\PhoneManager.jar" (
    echo Không tìm thấy target\PhoneManager.jar.
    echo Hãy đóng gói ứng dụng bằng Maven trong IntelliJ IDEA.
    pause
    exit /b 1
)

java -Dfile.encoding=UTF-8 -jar "target\PhoneManager.jar"
if errorlevel 1 (
    echo.
    echo Ứng dụng bị dừng hoặc không kết nối được cơ sở dữ liệu.
    echo Hãy chạy setup_database.bat trước, rồi kiểm tra SQL Server đang mở.
    pause
    exit /b 1
)
