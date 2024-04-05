import 'package:flutter/material.dart';
import 'dart:async';
import 'package:usage_stats/usage_stats.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<EventUsageInfo> events = [];
  List<UsageInfo> eventsInfo = [];
  List<UsageStats> eventsStats = [];
  Map<String?, NetworkInfo?> _netInfoMap = {};

  @override
  void initState() {
    super.initState();

    initUsage();
  }

  Future<void> initUsage() async {
    try {
      UsageStats.grantUsagePermission();

      DateTime endDate = DateTime.now();
      DateTime startDate = endDate.subtract(const Duration(days: 1));

      List<EventUsageInfo> queryEvents =
      await UsageStats.queryEvents(startDate, endDate);
      List<NetworkInfo> networkInfos = await UsageStats.queryNetworkUsageStats(
        startDate,
        endDate,
        networkType: NetworkType.all,
      );
      Set<String> uniquePackages = <String>{};

      Map<String?, NetworkInfo?> netInfoMap = { for (var v in networkInfos) v.packageName : v };
      eventsInfo = await UsageStats.queryUsageStats(startDate, endDate);
      List<EventUsageInfo> newList = queryEvents.reversed.toList();
      setState(() {
        for (var usage in newList) {
          if (!uniquePackages.contains(usage.packageName)) {
            events.add(usage);
            uniquePackages.add(usage.packageName!);
          }
        }
        _netInfoMap = netInfoMap;
      });
    } catch (err) {
      print(err);
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text("Usage Stats"), actions: const [
          IconButton(
            onPressed: UsageStats.grantUsagePermission,
            icon: Icon(Icons.settings),
          )
        ]),
        body: RefreshIndicator(
          onRefresh: initUsage,
          child: ListView.separated(
            itemBuilder: (context, index) {
              var event = events[index];
              var networkInfo = _netInfoMap[event.packageName];
              return ListTile(
                title: Text(event.packageName!),
                subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                        "Last time used: ${DateTime.fromMillisecondsSinceEpoch(int.parse(event.timeStamp!)).toIso8601String()}"),
                    networkInfo == null
                        ? const Text("Unknown network usage")
                        : Text("Received bytes: ${networkInfo.rxTotalBytes}\n" "Transferred bytes : ${networkInfo.txTotalBytes}"),
                  ],
                ),
              );
            },
            separatorBuilder: (context, index) => Divider(),
            itemCount: events.length,
          ),
        ),
      ),
    );
  }
}
