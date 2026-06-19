# 开发

## 1. 克隆仓库

```bash
git clone https://github.com/Ujhhgtg/WeKit.git --recursive
```

## 2. 安装系统依赖

### A. Arch Linux

```bash
# 确保已在 /etc/pacman.conf 中启用 multilib 软件源
yay -Syu lib32-glibc rustup
rustup toolchain install stable
rustup default stable
rustup target add x86_64-linux-android aarch64-linux-android armv7-linux-androideabi i686-linux-android
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "ndk;$(grep '^ndk' ./gradle/libs.versions.toml | sed 's/.*= "\(.*\)"/\1/')"
```

### B. Debian 系

```bash
sudo apt update -y && sudo apt full-upgrade -y
sudo apt install gcc-multilib rustup
rustup toolchain install stable
rustup default stable
rustup target add x86_64-linux-android aarch64-linux-android armv7-linux-androideabi i686-linux-android
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "ndk;$(grep '^ndk' ./gradle/libs.versions.toml | sed 's/.*= "\(.*\)"/\1/')"
```

### C. Windows

没试过, 不提供支持, 自行解决, 解决不了的话去用 Linux 或者 WSL

## 3. 构建

构建期间会自动编译 Rust 原生库, 无须手动编译

```bash
chmod +x ./gradlew
./gradlew :app:assembleRelease
```

## 4. 安装

```bash
adb install ./app/build/outputs/apk/release/app-arm64-v8a-release.apk
# --- 或 ---
./gradlew :app:installRelease

# 可选: 应用基准配置 (Baseline Profile)
adb shell cmd package compile -m speed-profile dev.ujhhgtg.wekit
```
