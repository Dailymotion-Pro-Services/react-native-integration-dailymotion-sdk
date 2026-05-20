module.exports = {
  dependency: {
    platforms: {
      android: {
        sourceDir: './android/dailymotionplayer',
        packageImportPath: 'import com.newreactnativedailymotionsdk.DailymotionPlayer.DailymotionPlayerViewFactory;',
        packageInstance: 'new DailymotionPlayerViewFactory()',
      },
      ios: {
        podspecPath: './DailymotionPlayer.podspec',
      },
    },
  },
};
