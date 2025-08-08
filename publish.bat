@echo off
setlocal enabledelayedexpansion

REM PortalTransform 发布脚本 (Windows版)
REM 用途：构建项目，推送到GitHub并自动创建release

echo 🚀 开始发布流程...

REM 检查是否有未提交的更改
git status --porcelain > temp_status.txt
set /p status_output=<temp_status.txt
del temp_status.txt

if not "!status_output!"=="" (
    echo ⚠️  检测到未提交的更改，请先提交所有更改
    git status
    exit /b 1
)

REM 获取当前版本
for /f "tokens=2 delims==" %%i in ('findstr "mod_version=" gradle.properties') do set CURRENT_VERSION=%%i
echo 📦 当前版本: !CURRENT_VERSION!

REM 询问是否更新版本
set /p update_version="是否需要更新版本? (y/n): "
if /i "!update_version!"=="y" (
    set /p new_version="请输入新版本号 (当前: !CURRENT_VERSION!): "
    
    REM 更新版本号
    powershell -Command "(Get-Content gradle.properties) -replace 'mod_version=.*', 'mod_version=!new_version!' | Set-Content gradle.properties"
    
    echo ✅ 版本已更新为: !new_version!
    set CURRENT_VERSION=!new_version!
    
    REM 提交版本更新
    git add gradle.properties
    git commit -m "chore: bump version to !new_version!"
)

REM 构建项目
echo 🔨 开始构建项目...
call gradlew clean build

if !errorlevel! neq 0 (
    echo ❌ 构建失败，请检查错误信息
    exit /b 1
)

echo ✅ 构建成功

REM 创建并推送标签
set TAG_NAME=v!CURRENT_VERSION!
echo 🏷️  创建标签: !TAG_NAME!

git tag -l | findstr /b "!TAG_NAME!$" > nul
if !errorlevel! equ 0 (
    set /p recreate_tag="⚠️  标签 !TAG_NAME! 已存在，是否删除并重新创建? (y/n): "
    if /i "!recreate_tag!"=="y" (
        git tag -d "!TAG_NAME!"
        git push origin --delete "!TAG_NAME!" 2>nul
    ) else (
        echo ❌ 发布取消
        exit /b 1
    )
)

git tag "!TAG_NAME!"

REM 推送代码和标签
echo 📤 推送到GitHub...
git push origin
git push origin "!TAG_NAME!"

echo 🎉 发布完成！
echo 📋 摘要：
echo    - 版本: !CURRENT_VERSION!
echo    - 标签: !TAG_NAME!
echo    - GitHub Actions将自动创建release
echo.
echo 📝 请访问GitHub查看构建状态和release

pause