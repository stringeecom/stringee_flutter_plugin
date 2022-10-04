import 'package:flutter/material.dart';

class SheetInputOneRow extends StatelessWidget {
  String title;
  String hint;
  String action;
  late String value = '';
  late final Function(String value) onPressed;

  SheetInputOneRow(this.title, this.hint, this.action);

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
                  '$title: ',
                  style: new TextStyle(
                    color: Colors.black,
                    fontSize: 18.0,
                  ),
                ),
                Flexible(
                  child: TextField(
                    onChanged: (String value) {
                      this.value = value.trim();
                    },
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                    decoration: InputDecoration.collapsed(hintText: hint),
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
                onPressed(value);
                FocusScope.of(context).requestFocus(new FocusNode());
                Navigator.pop(context);
              },
              child: Text(action),
            ),
          ),
        ],
      ),
    );
  }

  void show(BuildContext context, Function(String value) onPressed) async {
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
