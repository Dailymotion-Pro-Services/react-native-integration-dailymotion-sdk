import {
  ConfigPlugin,
  withProjectBuildGradle,
  withXcodeProject,
  createRunOncePlugin,
} from '@expo/config-plugins';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

type Options = {
  iosSdkVersion?: string;
};

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const PLUGIN_NAME = 'react-native-dailymotion-sdk';
const IOS_SDK_DEFAULT_VERSION = '1.4.11';
const IOS_SPM_URL = 'https://github.com/dailymotion/player-sdk-ios';
const IOS_SPM_PRODUCT = 'DailymotionPlayerSDK';
const ANDROID_MAVEN_NAME = 'DailymotionMavenRelease';
const ANDROID_MAVEN_URL = 'https://mvn.dailymotion.com/repository/releases/';

// ---------------------------------------------------------------------------
// Android: inject Dailymotion Maven repo into allprojects { repositories }
// ---------------------------------------------------------------------------

function withAndroidMavenRepo(config: Parameters<ConfigPlugin>[0]) {
  return withProjectBuildGradle(config, (mod) => {
    if (mod.modResults.contents.includes(ANDROID_MAVEN_NAME)) {
      return mod;
    }
    const mavenBlock = [
      `        maven {`,
      `            name = "${ANDROID_MAVEN_NAME}"`,
      `            url = uri("${ANDROID_MAVEN_URL}")`,
      `        }`,
    ].join('\n');

    mod.modResults.contents = mod.modResults.contents.replace(
      /(allprojects\s*\{\s*repositories\s*\{)/,
      `$1\n${mavenBlock}`
    );
    return mod;
  });
}

// ---------------------------------------------------------------------------
// iOS: add DailymotionPlayerSDK SPM package to the app target
// ---------------------------------------------------------------------------

function addSpmPackage(
  project: any,
  repositoryURL: string,
  version: string,
  productName: string
): void {
  const objects = project.hash.project.objects as Record<string, Record<string, any>>;

  // Idempotency — bail if package reference already present
  const existingRefs = objects['XCRemoteSwiftPackageReference'] ?? {};
  const alreadyAdded = Object.entries(existingRefs).some(
    ([key, ref]) => !key.endsWith('_comment') && ref.repositoryURL === `"${repositoryURL}"`
  );
  if (alreadyAdded) return;

  // 1. XCRemoteSwiftPackageReference
  const pkgRefUUID = project.generateUuid() as string;
  objects['XCRemoteSwiftPackageReference'] = objects['XCRemoteSwiftPackageReference'] ?? {};
  objects['XCRemoteSwiftPackageReference'][pkgRefUUID] = {
    isa: 'XCRemoteSwiftPackageReference',
    repositoryURL: `"${repositoryURL}"`,
    requirement: {
      kind: 'exactVersion',
      version: `"${version}"`,
    },
  };
  objects['XCRemoteSwiftPackageReference'][`${pkgRefUUID}_comment`] = productName;

  // 2. Register on PBXProject packageReferences
  const pbxProjects = objects['PBXProject'] ?? {};
  const projectKey = Object.keys(pbxProjects).find((k) => !k.endsWith('_comment'));
  if (!projectKey) throw new Error(`[${PLUGIN_NAME}] PBXProject not found`);
  const pbxProject = pbxProjects[projectKey];
  if (!Array.isArray(pbxProject.packageReferences)) {
    pbxProject.packageReferences = [];
  }
  pbxProject.packageReferences.push({ value: pkgRefUUID, comment: productName });

  // 3. XCSwiftPackageProductDependency
  const prodDepUUID = project.generateUuid() as string;
  objects['XCSwiftPackageProductDependency'] = objects['XCSwiftPackageProductDependency'] ?? {};
  objects['XCSwiftPackageProductDependency'][prodDepUUID] = {
    isa: 'XCSwiftPackageProductDependency',
    package: pkgRefUUID,
    productName,
  };
  objects['XCSwiftPackageProductDependency'][`${prodDepUUID}_comment`] = productName;

  // 4. Find the app target (productType = com.apple.product-type.application)
  const nativeTargets = objects['PBXNativeTarget'] ?? {};
  const appTargetKey = Object.keys(nativeTargets).find((k) => {
    if (k.endsWith('_comment')) return false;
    return nativeTargets[k].productType === '"com.apple.product-type.application"';
  });
  if (!appTargetKey) throw new Error(`[${PLUGIN_NAME}] No app target found in Xcode project`);
  const appTarget = nativeTargets[appTargetKey];

  // 5. Add packageProductDependency to target
  if (!Array.isArray(appTarget.packageProductDependencies)) {
    appTarget.packageProductDependencies = [];
  }
  appTarget.packageProductDependencies.push({ value: prodDepUUID, comment: productName });

  // 6. PBXBuildFile for the product
  const buildFileUUID = project.generateUuid() as string;
  objects['PBXBuildFile'] = objects['PBXBuildFile'] ?? {};
  objects['PBXBuildFile'][buildFileUUID] = {
    isa: 'PBXBuildFile',
    productRef: { value: prodDepUUID, comment: productName },
  };
  objects['PBXBuildFile'][`${buildFileUUID}_comment`] = `${productName} in Frameworks`;

  // 7. Add build file to the app target's PBXFrameworksBuildPhase
  const frameworksPhases = objects['PBXFrameworksBuildPhase'] ?? {};
  const buildPhaseRefs: Array<{ value: string } | string> = appTarget.buildPhases ?? [];
  const frameworksPhaseKey = buildPhaseRefs
    .map((ref) => (typeof ref === 'string' ? ref : ref.value))
    .find((uuid) => frameworksPhases[uuid] != null);

  if (frameworksPhaseKey) {
    const phase = frameworksPhases[frameworksPhaseKey];
    if (!Array.isArray(phase.files)) phase.files = [];
    phase.files.push({ value: buildFileUUID, comment: `${productName} in Frameworks` });
  }
}

function withIosSpm(config: Parameters<ConfigPlugin>[0], { iosSdkVersion = IOS_SDK_DEFAULT_VERSION }: Options) {
  return withXcodeProject(config, (mod) => {
    addSpmPackage(mod.modResults, IOS_SPM_URL, iosSdkVersion, IOS_SPM_PRODUCT);
    return mod;
  });
}

// ---------------------------------------------------------------------------
// Root plugin
// ---------------------------------------------------------------------------

const withDailymotionPlayer: ConfigPlugin<Options> = (config, options = {}) => {
  config = withAndroidMavenRepo(config);
  config = withIosSpm(config, options);
  return config;
};

export default createRunOncePlugin(withDailymotionPlayer, PLUGIN_NAME, '1.0.0');
