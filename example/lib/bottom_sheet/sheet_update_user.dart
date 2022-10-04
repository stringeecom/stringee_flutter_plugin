import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

class SheetUpdateUser extends StatelessWidget {
  late UserInfo userInfo;
  late final Function(UserInfo userInfo) onPressed;

  SheetUpdateUser(StringeeUser user) {
    userInfo = new UserInfo(
      name: user.name,
      email: user.email,
      avatar: user.avatarUrl,
      phone: user.phone,
      location: user.location,
      browser: user.browser,
      platform: user.platform,
      device: user.device,
      ipAddress: user.ipAddress,
      hostName: user.hostName,
      userAgent: user.userAgent,
    );
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Padding(
      padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom + 10.0,
          left: 10.0,
          right: 10.0,
          top: 10.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Name: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.name = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.name),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter name'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Email: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.email = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.email),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter email'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Avatar: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.avatar = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.avatar),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter avatar'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Phone: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.phone = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.phone),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter phone'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Location: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.location = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.location),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter location'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Browser: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.browser = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.browser),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter browser'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Platform: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.platform = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.platform),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter platform'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Device: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.device = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.device),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter device'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Ip address: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.ipAddress = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.ipAddress),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter ip address'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'Host name: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.hostName = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.hostName),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter host name'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'User agent: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      userInfo.userAgent = value.trim();
                    },
                    controller: TextEditingController(text: userInfo.userAgent),
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration:
                        InputDecoration.collapsed(hintText: 'Enter user agent'),
                  ),
                )
              ],
            ),
          ),
          Divider(
            color: Colors.black,
          ),
          Container(
            height: 40.0,
            width: 175.0,
            margin: EdgeInsets.only(top: 10.0),
            child: new ElevatedButton(
              onPressed: () {
                onPressed(userInfo);
                FocusScope.of(context).requestFocus(new FocusNode());
                Navigator.pop(context);
              },
              child: Text('Update'),
            ),
          ),
        ],
      ),
    );
  }

  void show(BuildContext context, Function(UserInfo userInfo) onPressed) async {
    this.onPressed = onPressed;
    showModalBottomSheet(
      isScrollControlled: true,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.only(
            topRight: Radius.circular(10.0), topLeft: Radius.circular(10.0)),
      ),
      context: context,
      builder: (context) {
        return this;
      },
    );
  }
}
