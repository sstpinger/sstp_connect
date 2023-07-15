#ifndef FLUTTER_PLUGIN_SSTP_CONNECT_PLUGIN_H_
#define FLUTTER_PLUGIN_SSTP_CONNECT_PLUGIN_H_

#include <flutter/method_channel.h>
#include <flutter/plugin_registrar_windows.h>

#include <memory>

namespace sstp_connect {

class SstpConnectPlugin : public flutter::Plugin {
 public:
  static void RegisterWithRegistrar(flutter::PluginRegistrarWindows *registrar);

  SstpConnectPlugin();

  virtual ~SstpConnectPlugin();

  // Disallow copy and assign.
  SstpConnectPlugin(const SstpConnectPlugin&) = delete;
  SstpConnectPlugin& operator=(const SstpConnectPlugin&) = delete;

  // Called when a method is called on this plugin's channel from Dart.
  void HandleMethodCall(
      const flutter::MethodCall<flutter::EncodableValue> &method_call,
      std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> result);
};

}  // namespace sstp_connect

#endif  // FLUTTER_PLUGIN_SSTP_CONNECT_PLUGIN_H_
