import 'package:flutter/material.dart';
import 'package:sstp_connect/sstp_connect.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final hostnameController = TextEditingController();

  _connect() async {
    try {
      final [String hostname, String port] = hostnameController.text.split(":");

      await SstpConnect.connect(
        hostname: hostname,
        port: int.parse(port),
      );
    } catch (e) {
      print(e);
    }
  }

  _disconnect() async {
    try {
      await SstpConnect.disconnect();
    } catch (e) {
      print(e);
    }
  }

  _reconnect() async {
    try {
      await _disconnect();
      await _connect();
    } catch (e) {
      print(e);
    }
  }

  @override
  void dispose() {
    hostnameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            TextField(controller: hostnameController),
            ElevatedButton(onPressed: _connect, child: const Icon(Icons.done)),
            ElevatedButton(onPressed: _disconnect, child: const Icon(Icons.stop)),
            ElevatedButton(onPressed: _reconnect, child: const Icon(Icons.restart_alt)),
            StreamBuilder(
              stream: SstpConnect.onStateChanged,
              builder: (context, snapshot) {
                return Text("status: ${snapshot.data}");
              },
            )
          ],
        ),
      ),
    );
  }
}
