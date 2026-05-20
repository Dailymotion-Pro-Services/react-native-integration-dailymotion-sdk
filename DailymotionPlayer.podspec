require "json"
package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "DailymotionPlayer"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = "https://github.com/Dailymotion-Pro-Services/react-native-integration-dailymotion-sdk"
  s.license      = { :type => "MIT" }
  s.authors      = { "Dailymotion Pro Services" => "sdk@dailymotion.com" }
  s.platforms    = { :ios => "14.0" }
  s.source       = {
    :git => "https://github.com/Dailymotion-Pro-Services/react-native-integration-dailymotion-sdk.git",
    :tag => "v#{s.version}"
  }

  # Library source — host app project files excluded.
  # DailymotionPlayerBridge.h is included so CocoaPods' auto-generated umbrella
  # header exposes React-Core ObjC types to the pod's Swift files (bridging headers
  # are unsupported for framework/pod targets; umbrella is the correct mechanism).
  s.source_files = [
    "ios/DailymotionPlayerNativeView.swift",
    "ios/DailymotionPlayerViewFactory.swift",
    "ios/DailymotionPlayerViewFactory.m",
    "ios/DailymotionPlayerBridge.h",
  ]

  s.pod_target_xcconfig = {
    "SWIFT_VERSION"          => "5.0",
    # SPM packages build into PackageFrameworks — pod targets need this in their search path
    "FRAMEWORK_SEARCH_PATHS" => "$(inherited) $(BUILT_PRODUCTS_DIR)/PackageFrameworks",
  }

  s.dependency "React-Core"

  # IMPORTANT: DailymotionPlayerSDK is SPM-only (not on CocoaPods).
  # Consumers must add it to the Xcode project before building:
  #   Xcode → File → Add Package Dependencies → https://github.com/dailymotion/player-sdk-ios
end
