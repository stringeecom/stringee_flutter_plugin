import 'package:flutter_test/flutter_test.dart';
import 'package:stringee_flutter_plugin_example/main.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('renders the main sample navigation',
      (WidgetTester tester) async {
    await tester.pumpWidget(MyApp());

    expect(find.text('Stringee flutter sample'), findsOneWidget);
    expect(find.text('Call'), findsOneWidget);
    expect(find.text('Chat'), findsOneWidget);
    expect(find.text('Live chat'), findsOneWidget);
    expect(find.text('Conference'), findsOneWidget);
  });
}
