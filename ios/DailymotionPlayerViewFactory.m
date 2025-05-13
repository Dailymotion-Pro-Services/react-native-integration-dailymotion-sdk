#import <Foundation/Foundation.h>
#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(DailymotionPlayerNative, RCTViewManager)
RCT_EXPORT_VIEW_PROPERTY(playerId, NSString)
RCT_EXPORT_VIEW_PROPERTY(videoId, NSString)
RCT_EXPORT_VIEW_PROPERTY(playlistId, NSString)
RCT_EXPORT_VIEW_PROPERTY(playerParameters, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(onEvent, RCTBubblingEventBlock)
// RCT_EXPORT_VIEW_PROPERTY(onClick, RCTBubblingEventBlock)

RCT_EXTERN_METHOD(loadContent:(nonnull NSNumber *)reactTag videoId:(NSString *)videoId playlistId:(NSString *)playlistId startTime:(nonnull NSNumber *)startTime)
RCT_EXTERN_METHOD(play:(nonnull NSNumber *)reactTag)
RCT_EXTERN_METHOD(pause:(nonnull NSNumber *)reactTag)
RCT_EXTERN_METHOD(setMute:(nonnull NSNumber *)reactTag mute:(nonnull BOOL)mute)
RCT_EXTERN_METHOD(setQuality:(nonnull NSNumber *)reactTag level:(nonnull NSString *)level)
RCT_EXTERN_METHOD(seekTo:(nonnull NSNumber *)reactTag to:(nonnull NSNumber *)to)
RCT_EXTERN_METHOD(setScaleMode:(nonnull NSNumber *)reactTag config:(nonnull NSString *)config)
RCT_EXTERN_METHOD(setFullscreen:(nonnull NSNumber *)reactTag fullscreen:(nonnull BOOL)fullscreen orientation:(NSString *)orientation)
RCT_EXTERN_METHOD(setSubtitles:(nonnull NSNumber *)reactTag code:(nonnull NSString *)code)
RCT_EXTERN_METHOD(setPlaybackSpeed:(nonnull NSNumber *)reactTag speed:(nonnull double *)speed)
@end
