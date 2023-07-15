#include "include/sstp_connect/sstp_connect_plugin_c_api.h"

#include <flutter/plugin_registrar_windows.h>

#include "sstp_connect_plugin.h"

void SstpConnectPluginCApiRegisterWithRegistrar(
    FlutterDesktopPluginRegistrarRef registrar) {
  sstp_connect::SstpConnectPlugin::RegisterWithRegistrar(
      flutter::PluginRegistrarManager::GetInstance()
          ->GetRegistrar<flutter::PluginRegistrarWindows>(registrar));
}
