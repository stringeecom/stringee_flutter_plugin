#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'stringee_flutter_plugin'
  s.version          = '0.0.1'
  s.summary          = 'stringee_flutter_plugin'
  s.description      = <<-DESC
Stringee plugin for flutter.
                       DESC
  s.homepage         = 'https://stringee.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Stringee' => 'info@stringee.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'

  s.dependency 'Flutter'
  s.dependency 'Stringee', '~> 1.6.0'
  s.static_framework = true

  s.ios.deployment_target = '8.0'
end

