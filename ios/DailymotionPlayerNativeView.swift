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
      initPlayer()
    }
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  func initPlayer() {
      if videoId.isEmpty || playerId.isEmpty {
        return
      }
    
      // Add additional customisation to the player by using DMPlayerParameters struct
      let playerParams = DMPlayerParameters(mute: true, defaultFullscreenOrientation: .portrait)
    
      // Using Dailymotion singleton instance in order to create the player
      // In order to get all the functionalities like fullscreen, ad support and open url pass and implement player delegate to the initialisation as bellow or after initialisation using playerView.playerDelegate = self
      // Add your player ID that was created in Dailymotion Partner HQ
      print("--Player ID: ", playerId)
      print("--Video ID: ", videoId)
    
      Dailymotion.createPlayer(playerId: playerId, videoId: videoId, playerParameters: playerParams , playerDelegate: self, logLevels: [.all]) { [weak self]  playerView, error in
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
                
                  // set video to fullscreen
                  playerView.setFullscreen(fullscreen: true)
                
                  // Add the Player View to view hierarchy
                  self.addPlayerView(playerView: playerView)
              }
          }
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
        initPlayer()
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
