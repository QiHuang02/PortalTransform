#!/bin/bash

# PortalTransform 发布脚本
# 用途：构建项目，推送到GitHub并自动创建release

set -e

echo "🚀 开始发布流程..."

# 检查是否有未提交的更改
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️  检测到未提交的更改，请先提交所有更改"
    git status
    exit 1
fi

# 获取当前版本
CURRENT_VERSION=$(grep 'mod_version=' gradle.properties | cut -d'=' -f2)
echo "📦 当前版本: $CURRENT_VERSION"

# 询问是否更新版本
echo "是否需要更新版本? (y/n)"
read -r update_version
if [ "$update_version" = "y" ] || [ "$update_version" = "Y" ]; then
    echo "请输入新版本号 (当前: $CURRENT_VERSION):"
    read -r new_version
    
    # 更新版本号
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/mod_version=.*/mod_version=$new_version/" gradle.properties
    else
        # Linux
        sed -i "s/mod_version=.*/mod_version=$new_version/" gradle.properties
    fi
    
    echo "✅ 版本已更新为: $new_version"
    CURRENT_VERSION=$new_version
    
    # 提交版本更新
    git add gradle.properties
    git commit -m "chore: bump version to $new_version"
fi

# 构建项目
echo "🔨 开始构建项目..."
./gradlew clean build

if [ $? -ne 0 ]; then
    echo "❌ 构建失败，请检查错误信息"
    exit 1
fi

echo "✅ 构建成功"

# 创建并推送标签
TAG_NAME="v$CURRENT_VERSION"
echo "🏷️  创建标签: $TAG_NAME"

if git tag -l | grep -q "^$TAG_NAME$"; then
    echo "⚠️  标签 $TAG_NAME 已存在，是否删除并重新创建? (y/n)"
    read -r recreate_tag
    if [ "$recreate_tag" = "y" ] || [ "$recreate_tag" = "Y" ]; then
        git tag -d "$TAG_NAME"
        git push origin --delete "$TAG_NAME" 2>/dev/null || true
    else
        echo "❌ 发布取消"
        exit 1
    fi
fi

git tag "$TAG_NAME"

# 推送代码和标签
echo "📤 推送到GitHub..."
git push origin
git push origin "$TAG_NAME"

echo "🎉 发布完成！"
echo "📋 摘要："
echo "   - 版本: $CURRENT_VERSION"
echo "   - 标签: $TAG_NAME"
echo "   - GitHub Actions将自动创建release"
echo ""
echo "📝 请访问GitHub查看构建状态和release："
echo "   https://github.com/$(git config --get remote.origin.url | sed 's/.*github.com[:/]\([^/]*\/[^/]*\)\.git/\1/')/actions"