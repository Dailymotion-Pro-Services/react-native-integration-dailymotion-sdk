"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const config_plugins_1 = require("@expo/config-plugins");
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
function withAndroidMavenRepo(config) {
    return (0, config_plugins_1.withProjectBuildGradle)(config, (mod) => {
        if (mod.modResults.contents.includes(ANDROID_MAVEN_NAME)) {
            return mod;
        }
        const mavenBlock = [
            `        maven {`,
            `            name = "${ANDROID_MAVEN_NAME}"`,
            `            url = uri("${ANDROID_MAVEN_URL}")`,
            `        }`,
        ].join('\n');
        mod.modResults.contents = mod.modResults.contents.replace(/(allprojects\s*\{\s*repositories\s*\{)/, `$1\n${mavenBlock}`);
        return mod;
    });
}
// ---------------------------------------------------------------------------
// iOS: add DailymotionPlayerSDK SPM package to the app target
// ---------------------------------------------------------------------------
function addSpmPackage(project, repositoryURL, version, productName) {
    var _a, _b, _c, _d, _e, _f, _g, _h;
    const objects = project.hash.project.objects;
    // Idempotency — bail if package reference already present
    const existingRefs = (_a = objects['XCRemoteSwiftPackageReference']) !== null && _a !== void 0 ? _a : {};
    const alreadyAdded = Object.entries(existingRefs).some(([key, ref]) => !key.endsWith('_comment') && ref.repositoryURL === `"${repositoryURL}"`);
    if (alreadyAdded)
        return;
    // 1. XCRemoteSwiftPackageReference
    const pkgRefUUID = project.generateUuid();
    objects['XCRemoteSwiftPackageReference'] = (_b = objects['XCRemoteSwiftPackageReference']) !== null && _b !== void 0 ? _b : {};
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
    const pbxProjects = (_c = objects['PBXProject']) !== null && _c !== void 0 ? _c : {};
    const projectKey = Object.keys(pbxProjects).find((k) => !k.endsWith('_comment'));
    if (!projectKey)
        throw new Error(`[${PLUGIN_NAME}] PBXProject not found`);
    const pbxProject = pbxProjects[projectKey];
    if (!Array.isArray(pbxProject.packageReferences)) {
        pbxProject.packageReferences = [];
    }
    pbxProject.packageReferences.push({ value: pkgRefUUID, comment: productName });
    // 3. XCSwiftPackageProductDependency
    const prodDepUUID = project.generateUuid();
    objects['XCSwiftPackageProductDependency'] = (_d = objects['XCSwiftPackageProductDependency']) !== null && _d !== void 0 ? _d : {};
    objects['XCSwiftPackageProductDependency'][prodDepUUID] = {
        isa: 'XCSwiftPackageProductDependency',
        package: pkgRefUUID,
        productName,
    };
    objects['XCSwiftPackageProductDependency'][`${prodDepUUID}_comment`] = productName;
    // 4. Find the app target (productType = com.apple.product-type.application)
    const nativeTargets = (_e = objects['PBXNativeTarget']) !== null && _e !== void 0 ? _e : {};
    const appTargetKey = Object.keys(nativeTargets).find((k) => {
        if (k.endsWith('_comment'))
            return false;
        return nativeTargets[k].productType === '"com.apple.product-type.application"';
    });
    if (!appTargetKey)
        throw new Error(`[${PLUGIN_NAME}] No app target found in Xcode project`);
    const appTarget = nativeTargets[appTargetKey];
    // 5. Add packageProductDependency to target
    if (!Array.isArray(appTarget.packageProductDependencies)) {
        appTarget.packageProductDependencies = [];
    }
    appTarget.packageProductDependencies.push({ value: prodDepUUID, comment: productName });
    // 6. PBXBuildFile for the product
    const buildFileUUID = project.generateUuid();
    objects['PBXBuildFile'] = (_f = objects['PBXBuildFile']) !== null && _f !== void 0 ? _f : {};
    objects['PBXBuildFile'][buildFileUUID] = {
        isa: 'PBXBuildFile',
        productRef: { value: prodDepUUID, comment: productName },
    };
    objects['PBXBuildFile'][`${buildFileUUID}_comment`] = `${productName} in Frameworks`;
    // 7. Add build file to the app target's PBXFrameworksBuildPhase
    const frameworksPhases = (_g = objects['PBXFrameworksBuildPhase']) !== null && _g !== void 0 ? _g : {};
    const buildPhaseRefs = (_h = appTarget.buildPhases) !== null && _h !== void 0 ? _h : [];
    const frameworksPhaseKey = buildPhaseRefs
        .map((ref) => (typeof ref === 'string' ? ref : ref.value))
        .find((uuid) => frameworksPhases[uuid] != null);
    if (frameworksPhaseKey) {
        const phase = frameworksPhases[frameworksPhaseKey];
        if (!Array.isArray(phase.files))
            phase.files = [];
        phase.files.push({ value: buildFileUUID, comment: `${productName} in Frameworks` });
    }
}
function withIosSpm(config, { iosSdkVersion = IOS_SDK_DEFAULT_VERSION }) {
    return (0, config_plugins_1.withXcodeProject)(config, (mod) => {
        addSpmPackage(mod.modResults, IOS_SPM_URL, iosSdkVersion, IOS_SPM_PRODUCT);
        return mod;
    });
}
// ---------------------------------------------------------------------------
// Root plugin
// ---------------------------------------------------------------------------
const withDailymotionPlayer = (config, options = {}) => {
    config = withAndroidMavenRepo(config);
    config = withIosSpm(config, options);
    return config;
};
exports.default = (0, config_plugins_1.createRunOncePlugin)(withDailymotionPlayer, PLUGIN_NAME, '1.0.0');
