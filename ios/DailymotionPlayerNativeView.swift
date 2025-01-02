//
//  DailymotionPlayerController.swift
//  Runner
//
//  Created by Arryangga Putra on 15-03-2024.
//

import Foundation
import UIKit
import DailymotionPlayerSDK
import SwiftUI

@objc(DailymotionPlayerNativeView)
class DailymotionPlayerNativeView:  UIView, DMVideoDelegate, DMAdDelegate {
  weak var parentViewController: UIViewController?
  
  var playerView: DMPlayerView?
  
  @objc var status = false {
    didSet {
      updateViewIfNeeded()
    }
  }
  
  @objc var videoId = "" {
    didSet {
      updateViewIfNeeded()
    }
  }
  
  @objc var playerId = "" {
    didSet {
      updateViewIfNeeded()
    }
  }
  
  
  @objc var onClick: RCTBubblingEventBlock?
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    Task {
      await initPlayer()
    }
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  

  func initPlayer(with parameters: DMPlayerParameters? = nil) async {
    do {
      let playerView = try await Dailymotion.createPlayer(playerId: playerId, videoId: videoId, playerParameters: (parameters ?? DMPlayerParameters(mute: false, defaultFullscreenOrientation: .portrait))!, playerDelegate: self, videoDelegate: self, adDelegate: self, logLevels: [.all])
      addPlayerView(playerView: playerView)
    } catch {
      handlePlayerError(error: error)
    }
  }
  
  
  private func addPlayerView(playerView: DMPlayerView) {
    self.playerView = playerView
    
    /**
     Add [player wrapper] as a subview of a parent
     */
    self.addSubview(playerView)
    
    let constraints = [
      playerView.topAnchor.constraint(equalTo: self.topAnchor),
      playerView.leadingAnchor.constraint(equalTo:self.leadingAnchor),
      playerView.widthAnchor.constraint(equalToConstant: self.bounds.size.width),
      playerView.heightAnchor.constraint(equalToConstant: self.bounds.size.height),
    ]
    
    NSLayoutConstraint.activate(constraints)
    print("--Player view added", self.playerView!)
  }

  private func updateViewIfNeeded() {
    // Ensure both videoId and playerId are present before calling setupView
    if !videoId.isEmpty && !playerId.isEmpty {
      Task {
        await initPlayer()
      }
    }
  }
  
  func play() {
    self.playerView?.play()
  }
  
  func pause() {
    self.playerView?.pause()
  }
  
  func load(videoId: String) {
    self.playerView?.loadContent(videoId: videoId)
  }
  
  
  func handlePlayerError(error: Error) {
    switch(error) {
    case PlayerError.advertisingModuleMissing :
      break;
    case PlayerError.stateNotAvailable :
      break;
    case PlayerError.underlyingRemoteError(error: let error):
      let error = error as NSError
      if let errDescription = error.userInfo[NSLocalizedDescriptionKey],
         let errCode = error.userInfo[NSLocalizedFailureReasonErrorKey],
         let recovery = error.userInfo[NSLocalizedRecoverySuggestionErrorKey] {
        print("Player Error : Description: \(errDescription), Code: \(errCode), Recovery : \(recovery) ")
        
      } else {
        print("Player Error : \(error)")
      }
      break
    case PlayerError.requestTimedOut:
      print(error.localizedDescription)
      break
    case PlayerError.unexpected:
      print(error.localizedDescription)
      break
    case PlayerError.internetNotConnected:
      print(error.localizedDescription)
      break
    case PlayerError.playerIdNotFound:
      print(error.localizedDescription)
      break
    case PlayerError.otherPlayerRequestError:
      print(error.localizedDescription)
      break
    default:
      print(error.localizedDescription)
      break
    }
  }
  

}


extension DailymotionPlayerNativeView: DMPlayerDelegate {
  func player(_ player: DailymotionPlayerSDK.DMPlayerView, openUrl url: URL) {
    
  }
  
  func playerDidRequestFullscreen(_ player: DMPlayerView) {
      player.notifyFullscreenChanged()
  }

  func playerDidExitFullScreen(_ player: DMPlayerView) {
      player.notifyFullscreenChanged()
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
  
  func player(_ player: DMPlayerView, didChangePresentationMode presentationMode: DMPlayerView.PresentationMode) {
    print("--playerDidChangePresentationMode", player.isFullscreen)
  }
  
  func player(_ player: DMPlayerView, didChangeVideo changedVideoEvent: PlayerVideoChangeEvent) {
    print( "--playerDidChangeVideo")
  }
  
  func player(_ player: DMPlayerView, didChangeVolume volume: Double, _ muted: Bool) {
    print( "--playerDidChangeVolume")
  }
  
  func playerDidCriticalPathReady(_ player: DMPlayerView) {
    print( "--playerDidCriticalPathReady")
  }
  
  func player(_ player: DMPlayerView, didReceivePlaybackPermission playbackPermission: PlayerPlaybackPermission) {
    print( "--playerDidReceivePlaybackPermission")
  }
  

  func player(_ player: DMPlayerView, didChangeScaleMode scaleMode: String) {
    print( "--playerDidChangeScaleMode")
  }
  
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
}
