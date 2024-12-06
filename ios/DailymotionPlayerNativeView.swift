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
class DailymotionPlayerNativeView:  UIView {
  
  private var playerController: DailymotionPlayerController?
  
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
    setupView()
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  private func setupView() {
    
    if(playerId.isEmpty){
      return
    }
    
    // Remove all existing subviews to prevent duplicates.
    self.subviews.forEach { $0.removeFromSuperview() }

    
    let defaultParameters = DMPlayerParameters(mute: false, defaultFullscreenOrientation: .landscapeRight)
    
    playerController = DailymotionPlayerController(
      playerId: playerId,
      videoId: videoId,
      parameters: defaultParameters
    )
    
    playerController!.view.translatesAutoresizingMaskIntoConstraints = false
    
    self.addSubview(playerController!.view)
    
    NSLayoutConstraint.activate([
      playerController!.view.topAnchor.constraint(equalTo: self.topAnchor),
      playerController!.view.bottomAnchor.constraint(equalTo: self.bottomAnchor),
      playerController!.view.leadingAnchor.constraint(equalTo: self.leadingAnchor),
      playerController!.view.trailingAnchor.constraint(equalTo: self.trailingAnchor),
    ])
    
  }
  
  override func layoutSubviews() {
    super.layoutSubviews()
      
    print("--layoutSubviews-DailymotionPlayerNativeView")

    NSLayoutConstraint.deactivate(playerController!.view.constraints)
      
    self.addSubview(playerController!.view)
      
    NSLayoutConstraint.activate([
      playerController!.view.topAnchor.constraint(equalTo: self.topAnchor),
      playerController!.view.bottomAnchor.constraint(equalTo: self.bottomAnchor),
      playerController!.view.leadingAnchor.constraint(equalTo: self.leadingAnchor),
      playerController!.view.trailingAnchor.constraint(equalTo: self.trailingAnchor),
    ])
     
  }
  
  private func updateViewIfNeeded() {
    // Ensure both videoId and playerId are present before calling setupView
    if !videoId.isEmpty && !playerId.isEmpty {
      setupView()
    }
  }
  
//  override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
//    guard let onClick = self.onClick else { return }
//    
//    let params: [String : Any] = ["value1":"react demo","value2":1]
//    onClick(params)
//  }
  
}
