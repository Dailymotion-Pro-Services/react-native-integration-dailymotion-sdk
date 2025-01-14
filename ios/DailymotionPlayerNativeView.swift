import Foundation
import UIKit
import DailymotionPlayerSDK
import SwiftUI

@objc(DailymotionPlayerNativeView)
class DailymotionPlayerNativeView: UIView, DMVideoDelegate, DMAdDelegate {
  // MARK: Properties
  weak var parentViewController: UIViewController?
  var playerView: DMPlayerView?
  
  // MARK: React Props & Methods
  @objc var playerId = "" {
    didSet {
      updateViewIfNeeded()
    }
  }

  @objc var videoId = "" {
    didSet {
      updateViewIfNeeded()
    }
  }

  @objc var playlistId = "" {
    didSet {
      updateViewIfNeeded()
    }
  }

  @objc var playerParameters: NSDictionary = [:] {
    didSet {
      updateViewIfNeeded()
    }
  }

  @objc var onEvent: RCTBubblingEventBlock?
  

  @objc func loadContent(videoId: String, playlistId: String, startTime: NSNumber) {
    self.playerView?.loadContent(videoId: videoId, playlistId: playlistId, startTime: TimeInterval(truncating: startTime))
  }
  
  @objc func play() {
    self.playerView?.play()
  }
  
  @objc func pause() {
    self.playerView?.pause()
  }

  @objc func setFullscreen(_ fullscreen: Bool, orientation: String = "portrait") {
    let orientationEnum: DMPlayerFullscreenOrientation
    switch orientation {
      case "landscapeLeft":
        orientationEnum = .landscapeLeft
      case "landscapeRight":
        orientationEnum = .landscapeRight
      case "portrait":
        orientationEnum = .portrait
      case "upsideDown":
        orientationEnum = .upsidedown
      default:
        orientationEnum = .landscapeLeft
    }
    self.playerView?.setFullscreen(fullscreen: fullscreen, orientation: orientationEnum)
  }

  @objc func setSubtitles(_ code: String) {
    self.playerView?.setSubtitles(code: code)
  }

  @objc func setQuality(_ quality: String) {
    self.playerView?.setQuality(level: quality)
  }
  
  @objc func seekTo(_ to: NSNumber) {
    self.playerView?.seek(to: TimeInterval(truncating: to))
  }

  @objc func setMute(_ mute: Bool) {
    self.playerView?.setMute(mute: mute)
  }

  @objc func setPlaybackSpeed(_ speed: Double) {
    print("Setting playback speed to \(speed)")
    self.playerView?.setPlaybackSpeed(speed: speed)
  }
  
  @objc func setScaleMode(_ mode: String) {
    let scaleModeEnum: DMPlayerParameters.ScaleMode
    switch mode {
      case "fit":
        scaleModeEnum = .fit
      case "fill":
        scaleModeEnum = .fill
      case "fillLeft":
        scaleModeEnum = .fillLeft
      case "fillRight":
        scaleModeEnum = .fillRight
      case "fillTop":
        scaleModeEnum = .fillTop
      case "fillBottom":
        scaleModeEnum = .fillBottom
      default:
        scaleModeEnum = .fit
    }
    self.playerView?.setScaleMode(scaleMode: scaleModeEnum)
  }

  // MARK: Method Invocation
  func invokeMethod(_ methodName: String, with argument: Any?) {
    switch methodName {
    case "loadContent":
      if let args = argument as? [String: Any] {
        if let videoId = args["videoId"] as? String, let playlistId = args["playlistId"] as? String, let startTime = args["startTime"] as? NSNumber {
          self.loadContent(videoId: videoId, playlistId: playlistId, startTime: startTime)
        }
      }
    case "play":
      self.play()
    case "pause":
      self.pause()
    case "setMute":
      if let mute = argument as? Bool {
        self.setMute(mute)
      }
    case "setQuality":
      if let quality = argument as? String {
        self.setQuality(quality)
      }
    case "seekTo":
      if let to = argument as? NSNumber {
        self.seekTo(to)
      }
    case "setScaleMode":
      if let mode = argument as? String {
        self.setScaleMode(mode)
      }
    case "setFullscreen":
      if let args = argument as? [String: Any] {
        if let fullscreen = args["fullscreen"] as? Bool,
          let orientation = args["orientation"] as? String {
          self.setFullscreen(fullscreen, orientation: orientation)
        }
      }
    case "setSubtitles":
      if let code = argument as? String {
        self.setSubtitles(code)
      }
    case "setPlaybackSpeed":
      if let speed = argument as? Double {
        self.setPlaybackSpeed(speed)
      }
    default:
      print("Error: Method \(methodName) not implemented")
    }
  }
  
  // MARK: Initialization
  override init(frame: CGRect) {
    super.init(frame: frame)
    Task {
      initPlayer()
    }
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  // MARK: Player Initialization
  func initPlayer() {
      if videoId.isEmpty || playerId.isEmpty {
        return
      }
    
      // Using Dailymotion singleton instance in order to create the player
      // In order to get all the functionalities like fullscreen, ad support and open url pass and implement player delegate to the initialisation as bellow or after initialisation using playerView.playerDelegate = self
      // Add your player ID that was created in Dailymotion Partner HQ
      print("--Player ID: ", playerId)
      print("--Video ID: ", videoId)
    
      Dailymotion.createPlayer(
        playerId: playerId,
        videoId: videoId,
        playerParameters: buildPlayerParameters(),
        playerDelegate: self,
        videoDelegate: self,
        adDelegate: self,
        logLevels: [.all]
      ) { [weak self] playerView, error in
          // Wait for Player initialisation and check if self is still allocated
          guard let self = self else {
            return
          }
          // Checking first if the createPlayer returned an error
          if let error = error {
            self.handlePlayerError(error: error)
          } else {
            // Since playerView is optional because of a possible error it must be unwrapped first
            if let playerView = playerView {
                // Add the Player View to view hierarchy
                self.addPlayerView(playerView: playerView)
            }
          }
      }
  }

  private func buildPlayerParameters() -> DMPlayerParameters {
    var mute = false
    var startTime: TimeInterval = 0
    var loop = false
    var scaleMode: DMPlayerParameters.ScaleMode = .fit
    var defaultFullscreenOrientation: DMPlayerFullscreenOrientation = .landscapeLeft
    var allowPIP = true
    var allowIDFA = true
    
    if let muteValue = playerParameters["mute"] as? Bool {
        mute = muteValue
    }
    
    if let startTimeValue = playerParameters["startTime"] as? NSNumber {
        startTime = TimeInterval(truncating: startTimeValue)
    }
    
    if let loopValue = playerParameters["loop"] as? Bool {
        loop = loopValue
    }
    
    if let scaleModeValue = playerParameters["scaleMode"] as? String {
        scaleMode = DMPlayerParameters.ScaleMode(rawValue: scaleModeValue) ?? .fit
    }
    
    if let orientationValue = playerParameters["defaultFullscreenOrientation"] as? String {
        switch orientationValue {
        case "landscapeLeft":
            defaultFullscreenOrientation = .landscapeLeft
        case "landscapeRight":
            defaultFullscreenOrientation = .landscapeRight
        case "portrait":
            defaultFullscreenOrientation = .portrait
        case "upsideDown":
            defaultFullscreenOrientation = .upsidedown
        default:
            defaultFullscreenOrientation = .landscapeLeft
        }
    }
    
    if let allowIDFAValue = playerParameters["allowIDFA"] as? Bool {
        allowIDFA = allowIDFAValue
    }

    if let allowPIPValue = playerParameters["allowPIP"] as? Bool {
        allowPIP = allowPIPValue
    }
    
    return DMPlayerParameters(
        scaleMode: scaleMode,
        mute: mute,
        startTime: startTime,
        loop: loop,
        allowIDFA: allowIDFA,
        allowPIP: allowPIP,
        defaultFullscreenOrientation: defaultFullscreenOrientation
    )
  }
  
  // MARK: Player View
  private func addPlayerView(playerView: DMPlayerView) {
    self.playerView = playerView
    self.addSubview(playerView)
    
    playerView.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      playerView.topAnchor.constraint(equalTo: self.topAnchor),
      playerView.leadingAnchor.constraint(equalTo: self.leadingAnchor),
      playerView.widthAnchor.constraint(equalToConstant: self.bounds.size.width),
      playerView.heightAnchor.constraint(equalToConstant: self.bounds.size.height),
    ])
    print("--Player view added", self.playerView!)
  }
  
  private func updateViewIfNeeded() {
    if !videoId.isEmpty && !playerId.isEmpty {
      Task {
        initPlayer()
      }
    }
  }
  
  // MARK: Error Handling
  private func handlePlayerError(error: Error) {
    switch error {
    case PlayerError.advertisingModuleMissing:
      break
    case PlayerError.stateNotAvailable:
      break
    case PlayerError.underlyingRemoteError(let error):
      print("Player Error: \(error)")
    default:
      print("Player Error: \(error.localizedDescription)")
    }
  }
  
  // MARK: Event Handling
  func sendEvent(_ eventName: String, _ data: [String: Any] = [:]) {
    guard let onEvent = self.onEvent else { return }
    var eventData = data
    eventData["event"] = eventName
    onEvent(eventData)
  }
}

// MARK: DMPlayerDelegate
extension DailymotionPlayerNativeView: DMPlayerDelegate {
  func findViewController() -> UIViewController? {
      var responder: UIResponder? = self
      while let currentResponder = responder {
          if let viewController = currentResponder as? UIViewController {
              return viewController
          }
          responder = currentResponder.next
      }
      return nil
  }

  func playerWillPresentFullscreenViewController(_ player: DMPlayerView) -> UIViewController? {
    guard let viewController = self.findViewController() else {
      fatalError("No parent view controller found in the view hierarchy.")
    }
    return viewController
  }

  func playerWillPresentAdInParentViewController(_ player: DMPlayerView) -> UIViewController {
    guard let viewController = self.findViewController() else {
      fatalError("No parent view controller found in the view hierarchy.")
    }
    return viewController
  }
  
  func player(_ player: DailymotionPlayerSDK.DMPlayerView, openUrl url: URL) {
    sendEvent("playerOpenUrl", ["url": url.absoluteString])
  }
  
  func player(_ player: DMPlayerView, didFailWithError error: Error) {
    sendEvent("playerDidFailWithError", ["error": error.localizedDescription])
  }
  
  func player(_ player: DMPlayerView, didChangeControls isVisible: Bool) {
    sendEvent("playerDidChangeControls", ["isVisible": isVisible])
  }
  
  func playerDidStart(_ player: DMPlayerView) {
    sendEvent("playerDidStart")
  }
  
  func playerDidEnd(_ player: DMPlayerView) {
    sendEvent("playerDidEnd")
  }
  
  func player(_ player: DMPlayerView, didChangeVideo changedVideoEvent: PlayerVideoChangeEvent) {
    sendEvent("playerDidChangeVideo", ["videoId": changedVideoEvent.videoId])
  }
  
  func player(_ player: DMPlayerView, didChangeVolume volume: Double, _ muted: Bool) {
    sendEvent("playerDidChangeVolume", ["volume": volume, "muted": muted])
  }
  
  func playerDidCriticalPathReady(_ player: DMPlayerView) {
    sendEvent("playerDidCriticalPathReady")
  }
  
  func player(_ player: DMPlayerView, didReceivePlaybackPermission playbackPermission: PlayerPlaybackPermission) {
    sendEvent("playerDidReceivePlaybackPermission", ["playbackPermission": playbackPermission])
  }
  
  func player(_ player: DMPlayerView, didChangePresentationMode presentationMode: DMPlayerView.PresentationMode) {
    sendEvent("playerDidChangePresentationMode", ["presentationMode": presentationMode])
  }
  
  func player(_ player: DMPlayerView, didChangeScaleMode scaleMode: String) {
    sendEvent("playerDidChangeScaleMode", ["scaleMode": scaleMode])
  }
}

// MARK: DMVideoDelegate
extension DailymotionPlayerNativeView {
  func video(_ player: DMPlayerView, didChangeSubtitles subtitles: String) {
    sendEvent("videoDidChangeSubtitles", ["subtitles": subtitles])
  }
  
  func video(_ player: DMPlayerView, didReceiveSubtitlesList subtitlesList: [String]) {
    sendEvent("videoDidReceiveSubtitlesList", ["subtitlesList": subtitlesList])
  }
  
  func video(_ player: DMPlayerView, didChangeDuration duration: Double) {
    sendEvent("videoDidChangeDuration", ["duration": duration])
  }
  
  func videoDidEnd(_ player: DMPlayerView) {
    sendEvent("videoDidEnd")
  }
  
  func videoDidPause(_ player: DMPlayerView) {
    sendEvent("videoDidPause")
  }
  
  func videoDidPlay(_ player: DMPlayerView) {
    sendEvent("videoDidPlay")
  }
  
  func videoIsPlaying(_ player: DMPlayerView) {
    sendEvent("videoIsPlaying")
  }
  
  func video(_ player: DMPlayerView, isInProgress progressTime: Double) {
    sendEvent("videoIsInProgress", ["progressTime": progressTime])
  }
  
  func video(_ player: DMPlayerView, didReceiveQualitiesList qualities: [String]) {
    sendEvent("videoDidReceiveQualitiesList", ["qualities": qualities])
  }
  
  func video(_ player: DMPlayerView, didChangeQuality quality: String) {
    sendEvent("videoDidChangeQuality", ["quality": quality])
  }
  
  func video(_ player: DMPlayerView, didSeekEnd time: Double) {
    sendEvent("videoDidSeekEnd")
  }
  
  func video(_ player: DMPlayerView, didSeekStart time: Double) {
    sendEvent("videoDidSeekStart", ["time": time])
  }
  
  func videoDidStart(_ player: DMPlayerView) {
    sendEvent("videoDidStart", ["time": time])
  }
  
  func video(_ player: DMPlayerView, didChangeTime time: Double) {
    sendEvent("videoDidChangeTime", ["time": time])
  }
  
  func videoIsBuffering(_ player: DMPlayerView) {
    sendEvent("videoIsBuffering")
  }
}
// MARK: DMAdDelegate
extension DailymotionPlayerNativeView {
  func adDidReceiveCompanions(_ player: DMPlayerView) {
    sendEvent("adDidReceiveCompanions")
  }
  
  func ad(_ player: DMPlayerView, didChangeDuration duration: Double) {
    sendEvent("didChangeDuration", ["duration": duration])
  }
  
  func ad(_ player: DMPlayerView, didEnd adEndEvent: PlayerAdEndEvent) {
    sendEvent("adDidEnd", ["adEndEvent": adEndEvent])
  }
  
  func adDidPause(_ player: DMPlayerView) {
    sendEvent("adDidPause")
  }
  
  func adDidPlay(_ player: DMPlayerView) {
    sendEvent("adDidPlay")
  }
  
  func ad(_ player: DMPlayerView, didStart type: String, _ position: String) {
    sendEvent("adDidStart", ["type": type, "position": position])
  }
  
  func ad(_ player: DMPlayerView, didChangeTime time: Double) {
    sendEvent("adDidChangeTime", ["time": time])
  }
  
  func adDidImpression(_ player: DMPlayerView) {
    sendEvent("adDidImpression")
  }
  
  func ad(_ player: DMPlayerView, adDidLoaded adLoadedEvent: PlayerAdLoadedEvent) {
    sendEvent("adDidLoaded", ["adLoadedEvent": adLoadedEvent])
  }
  
  func adDidClick(_ player: DMPlayerView) {
    sendEvent("adDidClick")
  }
}
