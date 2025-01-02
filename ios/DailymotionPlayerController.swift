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
  var isFullscreen: Bool = false
  var playerConstraints: [NSLayoutConstraint] = []
  
  private var originalParentView: UIView?
  private var originalConstraints: [NSLayoutConstraint]?
  
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
  
  
//  func toggleFullscreen() {
//      guard let player = playerView else { return }
//
//      if isFullscreen {
//          // back to normal
//          let normalWidth = self.view.bounds.size.width
//          let normalHeight = self.view.bounds.size.height
//
//          // Deactivate previous constraint
//          NSLayoutConstraint.deactivate(playerConstraints)
//
//          // constraint to normal
//          playerConstraints = [
//              player.topAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.topAnchor),
//              player.leadingAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.leadingAnchor),
//              player.widthAnchor.constraint(equalToConstant: normalWidth), // Ukuran normal sesuai ukuran parent view
//              player.heightAnchor.constraint(equalToConstant: normalHeight) // Ukuran normal sesuai ukuran parent view
//          ]
//
//          // activate new constraint
//          NSLayoutConstraint.activate(playerConstraints)
//
//          print("--Back to normal mode: \(normalWidth) x \(normalHeight)")
//      } else {
//          // Masuk ke mode fullscreen (ukuran layar penuh)
//        let screenWidth = UIScreen.main.bounds.size.height
//        let screenHeight = UIScreen.main.bounds.size.width
//
//          // Deactivate previous constraint
//          NSLayoutConstraint.deactivate(playerConstraints)
//
//          // Full screen constraint
//          playerConstraints = [
//              player.topAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.topAnchor),
//              player.leadingAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.leadingAnchor),
//              player.widthAnchor.constraint(equalToConstant: screenWidth),  // Ukuran layar penuh
//              player.heightAnchor.constraint(equalToConstant: screenHeight) // Ukuran layar penuh
//          ]
//
//          // Activate
//          NSLayoutConstraint.activate(playerConstraints)
//
//          print("--Enter to full screen mode: \(screenWidth) x \(screenHeight)")
//      }
//
//      // Toggle status fullscreen
//      isFullscreen.toggle()
//  }
  
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
//      toggleFullscreen()
      player.notifyFullscreenChanged()
  }

  func playerDidExitFullScreen(_ player: DMPlayerView) {
//      toggleFullscreen()
      player.notifyFullscreenChanged()
  }
  
  func playerWillPresentFullscreenViewController(_ player: DMPlayerView) -> UIViewController? {
    return nil
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
    print("--playerDidChangePresentationMode", player.isFullscreen)
  
    if ( self.parameters?.defaultFullscreenOrientation != .portrait) {
      return
    }
  
    if player.isFullscreen {
      // Save the player's original parent and constraints
      originalParentView = player.superview
      originalConstraints = player.constraints

      // Create the fullscreen black view
      let fullscreenView = UIView()
      fullscreenView.backgroundColor = .black
      fullscreenView.translatesAutoresizingMaskIntoConstraints = false
      fullscreenView.tag = 999 // Unique tag to identify the fullscreen view

      // Get the key window to add the fullscreen view
      if let keyWindow = UIApplication.shared.windows.first(where: { $0.isKeyWindow }) {
          keyWindow.addSubview(fullscreenView)

          // Set constraints to fill the entire screen
          NSLayoutConstraint.activate([
              fullscreenView.topAnchor.constraint(equalTo: keyWindow.topAnchor),
              fullscreenView.leadingAnchor.constraint(equalTo: keyWindow.leadingAnchor),
              fullscreenView.trailingAnchor.constraint(equalTo: keyWindow.trailingAnchor),
              fullscreenView.bottomAnchor.constraint(equalTo: keyWindow.bottomAnchor)
          ])

          // Move the player view to the fullscreen view
          player.removeFromSuperview()
          fullscreenView.addSubview(player)

          // Set constraints for the player within the fullscreen view
          player.translatesAutoresizingMaskIntoConstraints = false
          NSLayoutConstraint.activate([
              player.topAnchor.constraint(equalTo: fullscreenView.topAnchor),
              player.leadingAnchor.constraint(equalTo: fullscreenView.leadingAnchor),
              player.trailingAnchor.constraint(equalTo: fullscreenView.trailingAnchor),
              player.bottomAnchor.constraint(equalTo: fullscreenView.bottomAnchor)
          ])
      } else {
          print("Unable to find the key window.")
      }
    } else {
      // Exit fullscreen: Remove the fullscreen view
      if let keyWindow = UIApplication.shared.windows.first(where: { $0.isKeyWindow }),
        let fullscreenView = keyWindow.viewWithTag(999) {
          fullscreenView.removeFromSuperview()
      }

      // Restore the player to its original parent view
      if let originalParent = originalParentView, let originalConstraints = originalConstraints {
        player.removeFromSuperview()
        originalParent.addSubview(player)

        // Restore original constraints
        player.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.deactivate(player.constraints)
        NSLayoutConstraint.activate(originalConstraints)
      }
    }
  }
  
  func player(_ player: DMPlayerView, didChangeScaleMode scaleMode: String) {
    print( "--playerDidChangeScaleMode")
  }
}
