import 'package:flutter/material.dart';

class CircleButton extends StatelessWidget {
  Icon? icon;
  Color? primary;
  VoidCallback? onPressed;

  CircleButton({
    required this.icon,
    required this.primary,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return ElevatedButton(
      onPressed: onPressed,
      child: icon,
      style: ElevatedButton.styleFrom(
        padding: EdgeInsets.all(15.0),
        backgroundColor: primary,
        shape: CircleBorder(),
      ),
    );
  }
}
