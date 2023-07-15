import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'sstp_connect_platform_interface.dart';

/// An implementation of [SstpConnectPlatform] that uses method channels.
class MethodChannelSstpConnect extends SstpConnectPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('sstp_connect');
  final eventChannel = const EventChannel('sstp_connection_status');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> connect({
    required String hostname,
    required int port,
    required String username,
    required String password,
  }) async {
    return methodChannel.invokeMethod<String?>('connect', {
      'Hostname': hostname,
      'Port': port,
      'Username': username,
      'Password': password,
    });
  }

  @override
  Future<String?> disconnect() {
    return methodChannel.invokeMethod<String?>('disconnect');
  }

  @override
  Stream<bool> get onStateChanged => eventChannel.receiveBroadcastStream().cast<bool>();
}
