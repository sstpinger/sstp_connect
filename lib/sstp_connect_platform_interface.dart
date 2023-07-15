import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'sstp_connect_method_channel.dart';

abstract class SstpConnectPlatform extends PlatformInterface {
  /// Constructs a SstpConnectPlatform.
  SstpConnectPlatform() : super(token: _token);

  static final Object _token = Object();

  static SstpConnectPlatform _instance = MethodChannelSstpConnect();

  /// The default instance of [SstpConnectPlatform] to use.
  ///
  /// Defaults to [MethodChannelSstpConnect].
  static SstpConnectPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [SstpConnectPlatform] when
  /// they register themselves.
  static set instance(SstpConnectPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError();
  }

  Future<String?> connect({
    required String hostname,
    required int port,
    required String username,
    required String password,
  }) {
    throw UnimplementedError();
  }

  Future<String?> disconnect() {
    throw UnimplementedError();
  }

  Stream<bool> get onStateChanged => throw UnimplementedError();
}
