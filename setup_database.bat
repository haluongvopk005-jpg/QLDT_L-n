@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

if not defined PHONE_MANAGER_DB_HOST set "PHONE_MANAGER_DB_HOST=localhost"
if not defined PHONE_MANAGER_DB_USER set "PHONE_MANAGER_DB_USER=sa"
if not defined PHONE_MANAGER_DB_PASSWORD set "PHONE_MANAGER_DB_PASSWORD=123456"

if not exist "SQL_SETUP.sql" (
    echo Không tìm thấy SQL_SETUP.sql trong thư mục hiện tại.
    pause
    exit /b 1
)

where sqlcmd >nul 2>nul
if errorlevel 1 (
    echo Máy chưa có sqlcmd trong PATH.
    echo Hãy chạy SQL_SETUP.sql bằng SQL Server Management Studio.
    pause
    exit /b 1
)

echo Đang tạo hoặc cập nhật cơ sở dữ liệu PhoneManagerDB...
sqlcmd -f 65001 -S "%PHONE_MANAGER_DB_HOST%" -U "%PHONE_MANAGER_DB_USER%" -P "%PHONE_MANAGER_DB_PASSWORD%" -C -b -i "SQL_SETUP.sql"
if errorlevel 1 (
    echo.
    echo Lỗi khi chạy SQL_SETUP.sql.
    echo Kiểm tra SQL Server, tài khoản sa và mật khẩu trong file AppConfig.java.
    pause
    exit /b 1
)

echo.
echo Đã thiết lập cơ sở dữ liệu thành công.
pause
