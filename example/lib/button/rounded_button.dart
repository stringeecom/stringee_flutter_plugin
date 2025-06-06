import 'package:flutter/material.dart';

class RoundedButton extends StatelessWidget {
  final VoidCallback onPressed;
  final Icon icon;
  final double radius;
  final Color color;

  const RoundedButton({
    Key? key,
    required this.icon,
    required this.color,
    required this.radius,
    required this.onPressed,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return ElevatedButton(
        style: ButtonStyle(
            padding:
                WidgetStateProperty.all<EdgeInsetsGeometry>(EdgeInsets.only(
              left: 10.0,
              right: 10.0,
              bottom: 5.0,
              top: 5.0,
            )),
            backgroundColor: WidgetStateProperty.all<Color>(color),
            shape: WidgetStateProperty.all<RoundedRectangleBorder>(
                RoundedRectangleBorder(
                    borderRadius: BorderRadius.all(Radius.circular(radius)),
                    side: BorderSide(color: color)))),
        onPressed: onPressed,
        child: icon);
  }
}
