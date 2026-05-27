#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'stringee_plugin'
  s.version          = '0.0.1'
  s.summary          = 'stringee_plugin'
  s.description      = <<-DESC
Stringee plugin for flutter.
                       DESC
  s.homepage         = 'https://stringee.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Stringee' => 'info@stringee.com' }
  s.source           = { :path => '.' }
  s.source_files = 'stringee_plugin/Sources/stringee_plugin/**/*.{h,m}'
  s.public_header_files = 'stringee_plugin/Sources/stringee_plugin/include/**/*.h'

  s.dependency 'Flutter'
  s.dependency 'Stringee', '~> 2.0.2'
  s.static_framework = true

  s.ios.deployment_target = '13.0'
end
