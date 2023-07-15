import 'package:flutter_test/flutter_test.dart';
import 'package:sstp_connect/sstp_connect.dart';
import 'package:sstp_connect/sstp_connect_platform_interface.dart';
import 'package:sstp_connect/sstp_connect_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockSstpConnectPlatform
    with MockPlatformInterfaceMixin
    implements SstpConnectPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final SstpConnectPlatform initialPlatform = SstpConnectPlatform.instance;

  test('$MethodChannelSstpConnect is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelSstpConnect>());
  });

  test('getPlatformVersion', () async {
    SstpConnect sstpConnectPlugin = SstpConnect();
    MockSstpConnectPlatform fakePlatform = MockSstpConnectPlatform();
    SstpConnectPlatform.instance = fakePlatform;

    expect(await sstpConnectPlugin.getPlatformVersion(), '42');
  });
}
