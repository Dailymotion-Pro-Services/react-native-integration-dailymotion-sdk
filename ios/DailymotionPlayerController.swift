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

class DailymotionPlayerController: UIViewController, ObservableObject, DMVideoDelegate, DMAdDelegate{
  
  var playerId: String?
  var videoId: String = ""
  
  var playerView: DMPlayerView?
  var parameters: DMPlayerParameters?
  
  
  // Initialize the class with playerId and videoId
  init(playerId: String?, videoId: String, parameters: DMPlayerParameters? = nil) {
    self.playerId = playerId
    self.videoId = videoId
    self.parameters = parameters ?? DMPlayerParameters(mute: false, defaultFullscreenOrientation: .portrait)
    
    super.init(nibName: nil, bundle: nil)
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  
  override func viewDidLoad() {
    super.viewDidLoad()
    self.view.backgroundColor = .red
    self.view.clipsToBounds = true
    Task {
      await initPlayer()
    }
  }
  
  
  func initPlayer(with parameters: DMPlayerParameters? = nil) async {
    do {
      let playerView = try await Dailymotion.createPlayer(playerId: playerId ?? "xix5x", videoId: videoId, playerParameters: (parameters ?? self.parameters)!, playerDelegate: self, videoDelegate: self, adDelegate: self, logLevels: [.all])
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
    self.view.addSubview(playerView)
    
    let constraints = [
      playerView.topAnchor.constraint(equalTo: self.view.topAnchor),
      playerView.leadingAnchor.constraint(equalTo:self.view.leadingAnchor),
      playerView.widthAnchor.constraint(equalToConstant: self.view.bounds.size.width),
      playerView.heightAnchor.constraint(equalToConstant: self.view.bounds.size.height),
    ]
    
    NSLayoutConstraint.activate(constraints)
    print("--Player view added", self.playerView!)
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


extension DailymotionPlayerController: DMPlayerDelegate {
  func player(_ player: DailymotionPlayerSDK.DMPlayerView, openUrl url: URL) {
    
  }
  
  func playerDidRequestFullscreen(_ player: DMPlayerView) {
    // Move the player in fullscreen State
    // Call notifyFullscreenChanged() the player will update his state
    player.notifyFullscreenChanged()
  }
  
  func playerDidExitFullScreen(_ player: DMPlayerView) {
    // Move the player in initial State
    // Call notifyFullscreenChanged() the player will update his state
    player.notifyFullscreenChanged()
  }
  
  func playerWillPresentFullscreenViewController(_ player: DMPlayerView) -> UIViewController? {
    return self
  }
  
  func playerWillPresentAdInParentViewController(_ player: DMPlayerView) -> UIViewController {
    return self
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
  
  func player(_ player: DMPlayerView, didChangePresentationMode presentationMode: DMPlayerView.PresentationMode) {
    print( "--playerDidChangePresentationMode", player.isFullscreen)
  }
  
  func player(_ player: DMPlayerView, didChangeScaleMode scaleMode: String) {
    print( "--playerDidChangeScaleMode")
  }
}
