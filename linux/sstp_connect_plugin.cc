#include "include/sstp_connect/sstp_connect_plugin.h"

#include <flutter_linux/flutter_linux.h>
#include <gtk/gtk.h>
#include <sys/utsname.h>

#include <cstring>

#include "sstp_connect_plugin_private.h"

#define SSTP_CONNECT_PLUGIN(obj) \
  (G_TYPE_CHECK_INSTANCE_CAST((obj), sstp_connect_plugin_get_type(), \
                              SstpConnectPlugin))

struct _SstpConnectPlugin {
  GObject parent_instance;
};

G_DEFINE_TYPE(SstpConnectPlugin, sstp_connect_plugin, g_object_get_type())

// Called when a method call is received from Flutter.
static void sstp_connect_plugin_handle_method_call(
    SstpConnectPlugin* self,
    FlMethodCall* method_call) {
  g_autoptr(FlMethodResponse) response = nullptr;

  const gchar* method = fl_method_call_get_name(method_call);

  if (strcmp(method, "getPlatformVersion") == 0) {
    response = get_platform_version();
  } else {
    response = FL_METHOD_RESPONSE(fl_method_not_implemented_response_new());
  }

  fl_method_call_respond(method_call, response, nullptr);
}

FlMethodResponse* get_platform_version() {
  struct utsname uname_data = {};
  uname(&uname_data);
  g_autofree gchar *version = g_strdup_printf("Linux %s", uname_data.version);
  g_autoptr(FlValue) result = fl_value_new_string(version);
  return FL_METHOD_RESPONSE(fl_method_success_response_new(result));
}

static void sstp_connect_plugin_dispose(GObject* object) {
  G_OBJECT_CLASS(sstp_connect_plugin_parent_class)->dispose(object);
}

static void sstp_connect_plugin_class_init(SstpConnectPluginClass* klass) {
  G_OBJECT_CLASS(klass)->dispose = sstp_connect_plugin_dispose;
}

static void sstp_connect_plugin_init(SstpConnectPlugin* self) {}

static void method_call_cb(FlMethodChannel* channel, FlMethodCall* method_call,
                           gpointer user_data) {
  SstpConnectPlugin* plugin = SSTP_CONNECT_PLUGIN(user_data);
  sstp_connect_plugin_handle_method_call(plugin, method_call);
}

void sstp_connect_plugin_register_with_registrar(FlPluginRegistrar* registrar) {
  SstpConnectPlugin* plugin = SSTP_CONNECT_PLUGIN(
      g_object_new(sstp_connect_plugin_get_type(), nullptr));

  g_autoptr(FlStandardMethodCodec) codec = fl_standard_method_codec_new();
  g_autoptr(FlMethodChannel) channel =
      fl_method_channel_new(fl_plugin_registrar_get_messenger(registrar),
                            "sstp_connect",
                            FL_METHOD_CODEC(codec));
  fl_method_channel_set_method_call_handler(channel, method_call_cb,
                                            g_object_ref(plugin),
                                            g_object_unref);

  g_object_unref(plugin);
}
