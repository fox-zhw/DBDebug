
## Android Debug Database是一个强大的库，用于调试Android应用程序中的数据库和SharedPreferences

### Android Debug Database 可以在浏览器中以非常简单的方式直接查看数据库和SharedPreferences

### Android Debug Database可以做什么?

* 查看所有的数据库
* 查看应用程序中使用的SharedPreferences中的所有数据
* 运行sql语句查询，更新，删除数据。
* 直接编辑数据库中的值.
* 直接编辑shared preferences 中的值
* 直接在数据库中添加一行数据
* 直接在shared preferences 文件中添加 key-value
* 删除数据库中的一行数据 删除shared preferences的数据
* 检索
* 排序
* 下载数据库文件
* 调试内存数据库

### 所有的功能都不需要root权限

### 使用 Android Debug Database

添加依赖

```groovy
debugImplementation 'com.amitshekhar.android:debug-db:1.0.6'
```

调试加密的数据库

```groovy
debugImplementation 'com.amitshekhar.android:debug-db-encrypt:1.0.6'
```

使用 `debugImplementation` 在debug版本中编译

启动应用会在logcat中看到下面的log :

* D/DebugDB: Open http://XXX.XXX.X.XXX:8080 in your browser

* 也可以在代码中使用 `DebugDB.getAddressLog();`获取地址url

在浏览器中打开这个链接

重要:

* 需要安卓设备和电脑连接到同一局域网络 (Wifi or LAN).
* 如果使用usb调试，需要使用命令 `adb forward tcp:8080 tcp:8080` （将 PC端的 8000端口 的TCP请求转发到 Android设备的 8080端口）

注意      : 如果想使用除8080端口以外的其他端口
            在app build.gradle 中的buildTypes 下做以下更改

```groovy
debug {
    resValue("string", "PORT_NUMBER", "8081")
}
```

会看到像这样的 :

### 查看数据库和SharedPreferences xml文件中的值

<img src=https://raw.githubusercontent.com/amitshekhariitbhu/Android-Debug-Database/master/assets/debugdb.png >

### 编辑数据库和SharedPreferences xml文件

<img src=https://raw.githubusercontent.com/amitshekhariitbhu/Android-Debug-Database/master/assets/debugdb_edit.png >

### 使用模拟器

* 默认模拟器: 命令行执行 `adb forward tcp:8080 tcp:8080`  在浏览器打开 http://localhost:8080
* Genymotion模拟器: 打开 bridge 选项

### 防止错过logcat中的地址url, 可以使用toast展示地址url

由于这个库是自动初始化的，如果想要获得地址url log，
需要使用反射添加以下方法并调用(我们必须这样做，避免在release版本构建中出现构建错误，因为这个库将不会包含在release版本)。

```java
public static void showDebugDBAddressLogToast(Context context) {
    if (BuildConfig.DEBUG) {
       try {
            Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
            Method getAddressLog = debugDB.getMethod("getAddressLog");
            Object value = getAddressLog.invoke(null);
            Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
       } catch (Exception ignore) {

       }
    }
}
```

### 添加自定义的数据库文件

这个库是自动初始化的, 如果想调试自定义数据库, 需要添加以下方法并调用

```java
public static void setCustomDatabaseFiles(Context context) {
    if (BuildConfig.DEBUG) {
        try {
            Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
            Class[] argTypes = new Class[]{HashMap.class};
            Method setCustomDatabaseFiles = debugDB.getMethod("setCustomDatabaseFiles", argTypes);
            HashMap<String, Pair<File, String>> customDatabaseFiles = new HashMap<>();
            // set your custom database files
            customDatabaseFiles.put(ExtTestDBHelper.DATABASE_NAME,
                    new Pair<>(new File(context.getFilesDir() + "/" + ExtTestDBHelper.DIR_NAME +
                                                    "/" + ExtTestDBHelper.DATABASE_NAME), ""));
            setCustomDatabaseFiles.invoke(null, customDatabaseFiles);
        } catch (Exception ignore) {

        }
    }
}
```

### 添加内存数据库

这个库是自动初始化的, 如果想调试内存数据库, 需要添加以下方法并调用

```java
public static void setInMemoryRoomDatabases(SupportSQLiteDatabase... database) {
    if (BuildConfig.DEBUG) {
        try {
            Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
            Class[] argTypes = new Class[]{HashMap.class};
            HashMap<String, SupportSQLiteDatabase> inMemoryDatabases = new HashMap<>();
            // set your inMemory databases
            inMemoryDatabases.put("InMemoryOne.db", database[0]);
            Method setRoomInMemoryDatabase = debugDB.getMethod("setInMemoryRoomDatabases", argTypes);
            setRoomInMemoryDatabase.invoke(null, inMemoryDatabases);
        } catch (Exception ignore) {

        }
    }
}
```