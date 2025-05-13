import Foundation
import SwiftUI

@objc(DailymotionPlayerNative)
class DailymotionPlayerViewFactory: RCTViewManager {
  
  override static func requiresMainQueueSetup() -> Bool {
    return true
  }

  override func view() -> UIView! {
    return DailymotionPlayerNativeView()
  }

  func dispatchToView(
    reactTag: NSNumber,
    methodName: String,
    argument: Any?
  ) {
    DispatchQueue.main.async {
      guard let view = self.bridge.uiManager.view(forReactTag: reactTag) else {
        print("Error: View not found")
        return
      }
      if let view = view as? DailymotionPlayerNativeView {
        view.invokeMethod(methodName, with: argument)
      }
    }
  }

  @objc func loadContent(_ reactTag: NSNumber, videoId: String, playlistId: String?, startTime: NSNumber) {
    dispatchToView(reactTag: reactTag, methodName: "loadContent", argument: [
      "videoId": videoId,
      "playlistId": playlistId ?? "",
      "startTime": startTime,
    ])
  }

  @objc func play(_ reactTag: NSNumber) {
    dispatchToView(reactTag: reactTag, methodName: "play", argument: nil)
  }

  @objc func pause(_ reactTag: NSNumber) {
    dispatchToView(reactTag: reactTag, methodName: "pause", argument: nil)
  }

  @objc func setMute(_ reactTag: NSNumber, mute: Bool) {
    dispatchToView(reactTag: reactTag, methodName: "setMute", argument: mute)
  }

  @objc func setQuality(_ reactTag: NSNumber, level: String) {
    dispatchToView(reactTag: reactTag, methodName: "setQuality", argument: level)
  }

  @objc func seekTo(_ reactTag: NSNumber, to: NSNumber) {
    dispatchToView(reactTag: reactTag, methodName: "seekTo", argument: to)
  }

  @objc func setScaleMode(_ reactTag: NSNumber, config: String) {
    dispatchToView(reactTag: reactTag, methodName: "setScaleMode", argument: config)
  }

  @objc func setFullscreen(_ reactTag: NSNumber, fullscreen: Bool, orientation: String?) {
    dispatchToView(reactTag: reactTag, methodName: "setFullscreen", argument: [
        "fullscreen": fullscreen,
        "orientation": orientation ?? "portrait"
    ])
  }

  @objc func setSubtitles(_ reactTag: NSNumber, code: String) {
    dispatchToView(reactTag: reactTag, methodName: "setSubtitles", argument: code)
  }

  @objc func setPlaybackSpeed(_ reactTag: NSNumber, speed: Double) {
    dispatchToView(reactTag: reactTag, methodName: "setPlaybackSpeed", argument: speed)
  }
}
