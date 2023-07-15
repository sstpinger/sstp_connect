import 'sstp_connect_platform_interface.dart';

class SstpConnect {
  Future<String?> getPlatformVersion() {
    return SstpConnectPlatform.instance.getPlatformVersion();
  }

  static Future<String?> connect({
    required String hostname,
    int port = 443,
    String username = "vpn",
    String password = "vpn",
  }) {
    return SstpConnectPlatform.instance.connect(
      hostname: hostname,
      port: port,
      username: username,
      password: password,
    );
  }

  static Future<String?> disconnect() {
    return SstpConnectPlatform.instance.disconnect();
  }

  static Stream<bool> get onStateChanged => SstpConnectPlatform.instance.onStateChanged;
}
