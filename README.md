## Xsocks for Android

A xSocks client for Android.

Most source code sources https://github.com/lparam/xSocks.



### PREREQUISITES

* JDK 1.8+
* Android SDK r23+
* Android Studio 4.0+ (optional)

### BUILD

* Set environment variable `ANDROID_HOME`
* Set environment variable `ANDROID_NDK_HOME`
* Create your key following the instructions at http://developer.android.com/guide/publishing/app-signing.html#cert
* Create your sign.gradle file like this

```
* Build native binaries
```bash
    git submodule update --init
    make
```

#### Gradle Build
```bash
    gradle clean assembleRelease
```

#### Android Studio
* Import the project in Android Studio
* Make Project in Android Studio

### LICENSE

Copyright (C) 2020 dosgo

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
