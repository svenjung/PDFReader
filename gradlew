The target system is: Android - 1 - aarch64
The host system is: Windows - 10.0.17134 - AMD64
Determining if the C compiler works passed with the following output:
Change Dir: C:/Users/svenj/AndroidProjects/devsample/NativeCrashHandler/app/.externalNativeBuild/cmake/release/arm64-v8a/CMakeFiles/CMakeTmp

Run Build Command:"C:\Users\svenj\AppData\Local\Android\Sdk\cmake\3.6.4111459\bin\ninja.exe" "cmTC_96676"
[1/2] Building C object CMakeFiles/cmTC_96676.dir/testCCompiler.c.o
[2/2] Linking C executable cmTC_96676


Detecting C compiler ABI info compiled with the following output:
Change Dir: C:/Users/svenj/AndroidProjects/devsample/NativeCrashHandler/app/.externalNativeBuild/cmake/release/arm64-v8a/CMakeFiles/CMakeTmp

Run Build Command:"C:\Users\svenj\AppData\Local\Android\Sdk\cmake\3.6.4111459\bin\ninja.exe" "cmTC_eb83a"
[1/2] Building C object CMakeFiles/cmTC_eb83a.dir/CMakeCCompilerABI.c.o
[2/2] Linking C executable cmTC_eb83a
Android (5058415 based on r339409) clang version 8.0.2 (https://android.googlesource.com/toolchain/clang 40173bab62ec746213857d083c0e8b0abb568790) (https://android.googlesource.com/toolchain/llvm 7a6618d69e7e8111e1d49dc9e7813767c5ca756a) (based on LLVM 8.0.2svn)
Target: aarch64-none-linux-android21
Thread model: posix
InstalledDir: C:\Users\svenj\AppData\Local\Android\Sdk\ndk-bundle\toolchains\llvm\prebuilt\windows-x86_64\bin
Found candidate GCC installation: C:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/lib/gcc/aarch64-linux-android\4.9.x
Selected GCC installation: C:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/lib/gcc/aarch64-linux-android/4.9.x
Candidate multilib: .;@m64
Selected multilib: .;@m64
 "C:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/lib/gcc/aarch64-linux-android/4.9.x/../../../../aarch64-linux-android/bin\\ld" --sysroot=C:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/sysroot -pie --fix-cortex-a53-843419 --enable-new-dtags --eh-frame-hdr -m aarch64linux -dynamic-linker /system/bin/linker64 -o cmTC_eb83a "C:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/lib/aarch64-linux-android/21\\crtbegin_dynamic.o" "-LC:\\Users\\svenj\\AppData\\Local\\Android\\Sdk\\ndk-bundle\\toolchains\\llvm\\prebuilt\\windows-x86_64\\lib64\\clang\\8.0.2\\lib\\linux\\aarch64" -LC:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/lib/gcc/aarch64-linux-android/4.9.x -LC:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/lib/gcc/aarch64-linux-android/4.9.x/../../../../aarch64-linux-android/lib/../lib64 -LC:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/lib/aarch64-linux-android/21 -LC:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/lib/aarch64-linux-android -LC:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/lib/gcc/aarch64-linux-android/4.9.x/../../../../aarch64-linux-android/lib -LC:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/lib --exclude-libs libgcc.a --exclude-libs libatomic.a --build-id --warn-shared-textrel --fatal-warnings --no-undefined -z noexecstack -z relro -z now --gc-sections CMakeFiles/cmTC_eb83a.dir/CMakeCCompilerABI.c.o -lgcc -ldl -lc -lgcc -ldl "C:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/lib/aarch64-linux-android/21\\crtend_android.o"


Parsed C implicit link information from above output:
  link line regex: [^( *|.*[/\])(aarch64-linux-android-ld\.exe|([^/\]+-)?ld|collect2)[^/\]*( |$)]
  ignore line: [Change Dir: C:/Users/svenj/AndroidProjects/devsample/NativeCrashHandler/app/.externalNativeBuild/cmake/release/arm64-v8a/CMakeFiles/CMakeTmp]
  ignore line: []
  ignore line: [Run Build Command:"C:\Users\svenj\AppData\Local\Android\Sdk\cmake\3.6.4111459\bin\ninja.exe" "cmTC_eb83a"]
  ignore line: [[1/2] Building C object CMakeFiles/cmTC_eb83a.dir/CMakeCCompilerABI.c.o]
  ignore line: [[2/2] Linking C executable cmTC_eb83a]
  ignore line: [Android (5058415 based on r339409) clang version 8.0.2 (https://android.googlesource.com/toolchain/clang 40173bab62ec746213857d083c0e8b0abb568790) (https://android.googlesource.com/toolchain/llvm 7a6618d69e7e8111e1d49dc9e7813767c5ca756a) (based on LLVM 8.0.2svn)]
  ignore line: [Target: aarch64-none-linux-android21]
  ignore line: [Thread model: posix]
  ignore line: [InstalledDir: C:\Users\svenj\AppData\Local\Android\Sdk\ndk-bundle\toolchains\llvm\prebuilt\windows-x86_64\bin]
  ignore line: [Found candidate GCC installation: C:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/lib/gcc/aarch64-linux-android\4.9.x]
  ignore line: [Selected GCC installation: C:/Users/svenj/AppData/Local/Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/lib/gcc/aarch64-linux-android/4.9.x]
  ignore line: [Candidate multilib: .]
  ignore line: [@m64]
  ignore line: [Se