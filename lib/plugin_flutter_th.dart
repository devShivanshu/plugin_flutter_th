
import 'dart:async';

import 'package:agora_rtc_engine/rtc_engine.dart';
import 'package:flutter/services.dart';

class PluginFlutterTh {
  static const MethodChannel _channel =
      const MethodChannel('plugin_flutter_th');

  static Future<Null> shareScreen(RtcEngine engine) async {
    await _channel.invokeMethod('switchOnScreenShare', {"engine": "engine"});

  }
  static Future<Null> stopScreenShare() async {
    await _channel.invokeMethod('switchOffScreenShare');

  }
  static Future<String> generateToken(String appId, String appCertificate,String channelName, int uid) async {
    return await _channel.invokeMethod('generateToken',{"appId": appId,"appCertificate" : appCertificate,"channelName" : channelName, "uid" :uid});

  }
}
