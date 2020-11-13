import 'dart:async';
import 'dart:math';

import 'package:flutter/material.dart';
//import 'package:flutter_video_call/flutter_video_call.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:plugin_flutter_th/plugin_flutter_th.dart';
import 'package:plugin_flutter_th_example/settings.dart';

import 'agoraEngine.dart';


class IndexPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => IndexState();
}

class IndexState extends State<IndexPage> {
  /// create a channelController to retrieve text value
  String channelName;
  Random _rnd = Random();
  final _chars = 'AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890';
  @override
  void dispose() {
    // dispose input controller

    super.dispose();
  }
  String getRandomString(int length) => String.fromCharCodes(Iterable.generate(
      length, (_) => _chars.codeUnitAt(_rnd.nextInt(_chars.length))));
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Flutter Video Call'),
      ),
      body: Center(
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          height: 400,
          child: Column(
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 20),
                child: Row(
                  children: <Widget>[
                    Expanded(
                      child: RaisedButton(
                        onPressed: generateTokenAndJoin,
                        child: Text('Join'),
                        color: Colors.blueAccent,
                        textColor: Colors.white,
                      ),
                    )
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<String> generateTokenAndJoin() async {
    String token = "";
    String randomChannel =  getRandomString(5);
    token =  await PluginFlutterTh.generateToken(APP_ID, certificate,randomChannel, 0);
    await _handleCameraAndMic();
    // push video page with given channel name
    await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CallPage(
          token: token, channelName:randomChannel
         ),
      ),
    );
    return token;
  }

  onShareScreen()async  {
    await PluginFlutterTh.shareScreen;

    //FlutterPluginVideo.showToast;
  }

  Future<void> _handleCameraAndMic() async {
    await PermissionHandler().requestPermissions(
      [PermissionGroup.camera, PermissionGroup.microphone],
    );
  }
}
