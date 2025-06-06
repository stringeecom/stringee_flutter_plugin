import 'package:flutter/material.dart';

class CircleButton extends StatelessWidget {
  final Icon? icon;
  final Color? primary;
  final VoidCallback? onPressed;

  const CircleButton({
    Key? key,
    required this.icon,
    required this.primary,
    required this.onPressed,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return ElevatedButton(
      onPressed: onPressed,
      style: ElevatedButton.styleFrom(
        padding: EdgeInsets.all(15.0),
        backgroundColor: primary,
        shape: CircleBorder(),
      ),
      child: icon,
    );
  }
}
