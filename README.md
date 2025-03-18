LPA SDK DEMO

### 1.0.4

EuiccBleDevice添加设置蓝牙传输间隔时间的方法，单位为ms,支持最大可设置为1000ms

### 1.0.5

添加支持32位广播uuid。
由于Android平台限制service data不能超过31字节，service uuid 由128bit改成32bit，可以容纳更长的ble name。

### 1.0.7(droidlib-1.0.6.aar)

1. 添加扫码流程。
2. 添加ds功能(ds不需要再获取metadata)。
3. 升级lib和grpc，优化createLPAdClient。
4. sdk添加X509 trust manager，fix（信息安全扫描检测失败项为SSL通信客户端检测信任任意证书）。
5. sdk添加认证缓存。

**需要捕获createLPAdClient的异常。**

**lib需要升级：**

- implementation "io.grpc:grpc-okhttp:1.64.0"
- implementation "io.grpc:grpc-protobuf-lite:1.64.0"
- implementation "io.grpc:grpc-stub:1.64.0"

### 1.0.8(droidlib-1.0.7.aar)

增加设置蓝牙传输协议的数据长度占用字节数，默认2字节，仅支持2和4字节。

### 1.0.8.1(droidlib-1.0.7.1.aar)

lib新增patch的能力。