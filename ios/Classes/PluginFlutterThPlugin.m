#import "PluginFlutterThPlugin.h"
#if __has_include(<plugin_flutter_th/plugin_flutter_th-Swift.h>)
#import <plugin_flutter_th/plugin_flutter_th-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "plugin_flutter_th-Swift.h"
#endif

@implementation PluginFlutterThPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftPluginFlutterThPlugin registerWithRegistrar:registrar];
}
@end
