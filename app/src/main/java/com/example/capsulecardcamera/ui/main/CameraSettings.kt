package com.example.capsulecardcamera.ui.main

import android.content.Context
import androidx.camera.core.CameraSelector
import java.util.Locale

private const val PreferencesName = "capsule_card_camera_settings"
private const val LanguageKey = "language"
private const val AlbumKey = "album"
private const val LensKey = "lens"
private const val CameraDisplayStyleKey = "camera_display_style"
private const val DynamicIslandCoverModeKey = "dynamic_island_cover_mode"
private const val SoundEffectsEnabledKey = "sound_effects_enabled"
private const val ShutterSoundStyleKey = "shutter_sound_style"
private const val SoundEffectVolumeKey = "sound_effect_volume"
private const val AiCapabilityNoticeAcknowledgedKey = "ai_capability_notice_acknowledged"

internal data class CameraPreferences(
  val language: CameraLanguage = CameraLanguage.System,
  val defaultAlbum: DefaultAlbum = DefaultAlbum.Capsule,
  val cameraLens: CameraLens = CameraLens.Front,
  val cameraDisplayStyle: CameraDisplayStyle = CameraDisplayStyle.PullList,
  val dynamicIslandCoverMode: DynamicIslandCoverMode = DynamicIslandCoverMode.Comfort,
  val soundEffectsEnabled: Boolean = true,
  val shutterSoundStyle: ShutterSoundStyle = ShutterSoundStyle.Classic,
  val soundEffectVolume: SoundEffectVolume = SoundEffectVolume.Normal,
)

internal enum class CameraLanguage(val storageKey: String) {
  System("system"),
  English("en"),
  ChineseSimplified("zh"),
}

internal enum class DefaultAlbum(
  val storageKey: String,
  val directoryName: String,
) {
  Capsule("capsule", "Capsule Card Camera"),
  Camera("camera", "Camera"),
  Framed("framed", "Framed Photos"),
}

internal enum class CameraLens(val storageKey: String) {
  Front("front"),
  Back("back"),
}

internal enum class CameraDisplayStyle(val storageKey: String) {
  PullList("pull_list"),
  StageList("stage_list"),
}

internal enum class DynamicIslandCoverMode(val storageKey: String) {
  Precise("precise"),
  Comfort("comfort"),
  Maximum("maximum"),
}

internal enum class ShutterSoundStyle(
  val storageKey: String,
  val playbackRate: Float,
  val gain: Float,
) {
  Classic("classic", playbackRate = 1f, gain = 1f),
  Crisp("crisp", playbackRate = 1.18f, gain = 0.9f),
  Soft("soft", playbackRate = 0.88f, gain = 0.72f),
}

internal enum class SoundEffectVolume(
  val storageKey: String,
  val gain: Float,
) {
  Soft("soft", gain = 0.36f),
  Normal("normal", gain = 0.68f),
  Loud("loud", gain = 1f),
}

private enum class ResolvedLanguage {
  English,
  ChineseSimplified,
}

internal data class CameraCopy(
  val settingsButtonContentDescription: String,
  val settingsTitle: String,
  val languageLabel: String,
  val albumLabel: String,
  val lensLabel: String,
  val systemLanguageLabel: String,
  val englishLanguageLabel: String,
  val chineseLanguageLabel: String,
  val capsuleAlbumLabel: String,
  val cameraAlbumLabel: String,
  val framedAlbumLabel: String,
  val frontLensLabel: String,
  val backLensLabel: String,
  val closeSettingsContentDescription: String,
  val frameSettingsContentDescription: String,
  val closePreviewContentDescription: String,
  val shutterContentDescription: String,
  val homePullHintTitle: String,
  val homePullHintSubtitle: String,
  val cameraDisplayStyleTitle: String,
  val cameraDisplayStylePullLabel: String,
  val cameraDisplayStyleStageLabel: String,
  val dynamicIslandCoverTitle: String,
  val dynamicIslandCoverPreciseLabel: String,
  val dynamicIslandCoverComfortLabel: String,
  val dynamicIslandCoverMaximumLabel: String,
  val defaultFrameTitle: String,
  val photoFrameTitle: String,
  val frameStyleTitle: String,
  val savePhotoWithFrame: String,
  val selectPhotoToSave: String,
  val savedToAlbumPrefix: String,
  val savedPhotosTemplate: String,
  val saveFailed: String,
  val storagePermissionDenied: String,
  val selectedPhotosTemplate: String,
  val clearSelectionLabel: String,
  val saveSelectedPhotos: String,
  val deleteSelectedPhotos: String,
  val aiInsightTitle: String,
  val aiIdle: String,
  val aiPreparing: String,
  val aiAnalyzing: String,
  val aiUnavailable: String,
  val aiFailed: String,
  val aiUnavailableNoticeTitle: String,
  val aiUnavailableNoticeBody: String,
  val aiUnavailableNoticeAction: String,
  val aiSubjectLabel: String,
  val aiSceneLabel: String,
  val aiColorsLabel: String,
  val aiConfidenceLabel: String,
  val aiConfidenceLowLabel: String,
  val aiConfidenceMediumLabel: String,
  val aiConfidenceHighLabel: String,
  val aiFrameSuggestionLabel: String,
  val aiFrameApplyLabel: String,
  val aiFrameAppliedLabel: String,
  val aiFrameGeneratorTitle: String,
  val aiFramePromptPlaceholder: String,
  val aiFrameGenerateLabel: String,
  val aiFrameRefineLabel: String,
  val aiFrameGenerating: String,
  val aiFrameGeneratedLabel: String,
  val aiFrameUnavailable: String,
  val aiFrameGenerationFailed: String,
  val myFramesTitle: String,
  val myFrameSelectedLabel: String,
  val frameManagerOpenLabel: String,
  val frameManagerDescription: String,
  val frameManagerPromptPlaceholder: String,
  val frameManagerGenerateLabel: String,
  val frameManagerGeneratingLabel: String,
  val frameManagerEmptyLabel: String,
  val frameManagerUseAsDefaultLabel: String,
  val frameManagerDefaultLabel: String,
  val aiModelLabel: String,
  val aiProviderGeminiLabel: String,
  val aiProviderDescription: String,
  val soundEffectsLabel: String,
  val soundEffectsOnLabel: String,
  val soundEffectsOffLabel: String,
  val shutterSoundLabel: String,
  val soundStyleClassicLabel: String,
  val soundStyleCrispLabel: String,
  val soundStyleSoftLabel: String,
  val soundVolumeLabel: String,
  val soundVolumeSoftLabel: String,
  val soundVolumeNormalLabel: String,
  val soundVolumeLoudLabel: String,
  val soundCreditLabel: String,
  val pullForFullScreenHint: String,
  val stampFrameLabel: String,
  val polaroidFrameLabel: String,
  val filmFrameLabel: String,
  val colorFrameLabel: String,
)

internal fun loadCameraPreferences(context: Context): CameraPreferences {
  val preferences = context.applicationContext.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
  return CameraPreferences(
    language = preferences.getString(LanguageKey, null).toCameraLanguage(),
    defaultAlbum = preferences.getString(AlbumKey, null).toDefaultAlbum(),
    cameraLens = preferences.getString(LensKey, null).toCameraLens(),
    cameraDisplayStyle = preferences.getString(CameraDisplayStyleKey, null).toCameraDisplayStyle(),
    dynamicIslandCoverMode = preferences.getString(DynamicIslandCoverModeKey, null).toDynamicIslandCoverMode(),
    soundEffectsEnabled = preferences.getBoolean(SoundEffectsEnabledKey, true),
    shutterSoundStyle = preferences.getString(ShutterSoundStyleKey, null).toShutterSoundStyle(),
    soundEffectVolume = preferences.getString(SoundEffectVolumeKey, null).toSoundEffectVolume(),
  )
}

internal fun saveCameraPreferences(
  context: Context,
  preferences: CameraPreferences,
) {
  context.applicationContext
    .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    .edit()
    .putString(LanguageKey, preferences.language.storageKey)
    .putString(AlbumKey, preferences.defaultAlbum.storageKey)
    .putString(LensKey, preferences.cameraLens.storageKey)
    .putString(CameraDisplayStyleKey, preferences.cameraDisplayStyle.storageKey)
    .putString(DynamicIslandCoverModeKey, preferences.dynamicIslandCoverMode.storageKey)
    .putBoolean(SoundEffectsEnabledKey, preferences.soundEffectsEnabled)
    .putString(ShutterSoundStyleKey, preferences.shutterSoundStyle.storageKey)
    .putString(SoundEffectVolumeKey, preferences.soundEffectVolume.storageKey)
    .apply()
}

internal fun isAiCapabilityNoticeAcknowledged(context: Context): Boolean =
  context
    .applicationContext
    .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    .getBoolean(AiCapabilityNoticeAcknowledgedKey, false)

internal fun acknowledgeAiCapabilityNotice(context: Context) {
  context
    .applicationContext
    .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    .edit()
    .putBoolean(AiCapabilityNoticeAcknowledgedKey, true)
    .apply()
}

internal fun CameraPreferences.copyText(): CameraCopy =
  when (language.resolve()) {
    ResolvedLanguage.ChineseSimplified ->
      CameraCopy(
        settingsButtonContentDescription = "设置",
        settingsTitle = "设置",
        languageLabel = "语言",
        albumLabel = "默认相册",
        lensLabel = "摄像头",
        systemLanguageLabel = "跟随系统",
        englishLanguageLabel = "English",
        chineseLanguageLabel = "简体中文",
        capsuleAlbumLabel = "卡片相册",
        cameraAlbumLabel = "Camera",
        framedAlbumLabel = "相框照片",
        frontLensLabel = "前置",
        backLensLabel = "后置",
        closeSettingsContentDescription = "关闭设置",
        frameSettingsContentDescription = "相框设置",
        closePreviewContentDescription = "关闭预览",
        shutterContentDescription = "快门",
        homePullHintTitle = "下拉打开相机",
        homePullHintSubtitle = "照片会打印到列表，并自动进行端侧分析",
        cameraDisplayStyleTitle = "拍照界面",
        cameraDisplayStylePullLabel = "列表相机",
        cameraDisplayStyleStageLabel = "拍照台",
        dynamicIslandCoverTitle = "灵动岛遮罩",
        dynamicIslandCoverPreciseLabel = "精确",
        dynamicIslandCoverComfortLabel = "舒适",
        dynamicIslandCoverMaximumLabel = "最大",
        defaultFrameTitle = "默认相框",
        photoFrameTitle = "照片相框",
        frameStyleTitle = "相框样式",
        savePhotoWithFrame = "保存带相框照片",
        selectPhotoToSave = "选择照片后保存",
        savedToAlbumPrefix = "已保存到",
        savedPhotosTemplate = "已保存 %d 张到 %s",
        saveFailed = "照片保存失败",
        storagePermissionDenied = "存储权限被拒绝",
        selectedPhotosTemplate = "已选 %d 张",
        clearSelectionLabel = "完成",
        saveSelectedPhotos = "保存所选",
        deleteSelectedPhotos = "删除",
        aiInsightTitle = "端侧 AI 标签",
        aiIdle = "等待分析",
        aiPreparing = "正在准备端侧模型",
        aiAnalyzing = "正在分析照片",
        aiUnavailable = "当前设备暂不可用",
        aiFailed = "分析失败，可稍后重试",
        aiUnavailableNoticeTitle = "没有找到端侧 AI",
        aiUnavailableNoticeBody = "当前设备暂时无法启动 Gemini Nano，一些智能相框、照片理解和自动美化功能不可用。拍照、相册、普通相框和保存等核心功能持续可用。",
        aiUnavailableNoticeAction = "继续使用",
        aiSubjectLabel = "主体",
        aiSceneLabel = "场景",
        aiColorsLabel = "颜色",
        aiConfidenceLabel = "置信度",
        aiConfidenceLowLabel = "低",
        aiConfidenceMediumLabel = "中",
        aiConfidenceHighLabel = "高",
        aiFrameSuggestionLabel = "AI 推荐相框",
        aiFrameApplyLabel = "使用推荐",
        aiFrameAppliedLabel = "已使用推荐",
        aiFrameGeneratorTitle = "Gemini 相框",
        aiFramePromptPlaceholder = "描述想要的感觉",
        aiFrameGenerateLabel = "生成",
        aiFrameRefineLabel = "调整",
        aiFrameGenerating = "正在生成相框",
        aiFrameGeneratedLabel = "已生成相框",
        aiFrameUnavailable = "当前设备暂不可用",
        aiFrameGenerationFailed = "生成失败，可稍后重试",
        myFramesTitle = "我的相框",
        myFrameSelectedLabel = "已选",
        frameManagerOpenLabel = "管理相框",
        frameManagerDescription = "创建自己的相框，并设置为拍摄默认相框。",
        frameManagerPromptPlaceholder = "写下相框风格",
        frameManagerGenerateLabel = "生成相框",
        frameManagerGeneratingLabel = "正在生成",
        frameManagerEmptyLabel = "还没有自定义相框",
        frameManagerUseAsDefaultLabel = "设为默认",
        frameManagerDefaultLabel = "默认",
        aiModelLabel = "端侧模型",
        aiProviderGeminiLabel = "Gemini Nano",
        aiProviderDescription = "使用系统 AICore 上的 Gemini Nano 在设备端完成照片理解和标签推理。",
        soundEffectsLabel = "音效",
        soundEffectsOnLabel = "开启",
        soundEffectsOffLabel = "关闭",
        shutterSoundLabel = "快门音色",
        soundStyleClassicLabel = "经典",
        soundStyleCrispLabel = "清脆",
        soundStyleSoftLabel = "柔和",
        soundVolumeLabel = "音量",
        soundVolumeSoftLabel = "低",
        soundVolumeNormalLabel = "中",
        soundVolumeLoudLabel = "高",
        soundCreditLabel = "音源：OpenGameArt CC0 camera shutter",
        pullForFullScreenHint = "继续下拉进入全屏相机",
        stampFrameLabel = "邮票",
        polaroidFrameLabel = "拍立得",
        filmFrameLabel = "胶片",
        colorFrameLabel = "彩色",
      )
    ResolvedLanguage.English ->
      CameraCopy(
        settingsButtonContentDescription = "Settings",
        settingsTitle = "Settings",
        languageLabel = "Language",
        albumLabel = "Default album",
        lensLabel = "Camera",
        systemLanguageLabel = "System",
        englishLanguageLabel = "English",
        chineseLanguageLabel = "Chinese",
        capsuleAlbumLabel = "Capsule",
        cameraAlbumLabel = "Camera",
        framedAlbumLabel = "Framed",
        frontLensLabel = "Front",
        backLensLabel = "Back",
        closeSettingsContentDescription = "Close settings",
        frameSettingsContentDescription = "Frame settings",
        closePreviewContentDescription = "Close preview",
        shutterContentDescription = "Shutter",
        homePullHintTitle = "Pull down to open camera",
        homePullHintSubtitle = "Photos print to the list and analyze on device",
        cameraDisplayStyleTitle = "Camera view",
        cameraDisplayStylePullLabel = "List camera",
        cameraDisplayStyleStageLabel = "Stage camera",
        dynamicIslandCoverTitle = "Dynamic island cover",
        dynamicIslandCoverPreciseLabel = "Precise",
        dynamicIslandCoverComfortLabel = "Comfort",
        dynamicIslandCoverMaximumLabel = "Maximum",
        defaultFrameTitle = "Default frame",
        photoFrameTitle = "Photo frame",
        frameStyleTitle = "Frame style",
        savePhotoWithFrame = "Save photo with frame",
        selectPhotoToSave = "Select a photo to save",
        savedToAlbumPrefix = "Saved to",
        savedPhotosTemplate = "Saved %d photos to %s",
        saveFailed = "Could not save photo",
        storagePermissionDenied = "Storage permission denied",
        selectedPhotosTemplate = "%d selected",
        clearSelectionLabel = "Done",
        saveSelectedPhotos = "Save selected",
        deleteSelectedPhotos = "Delete",
        aiInsightTitle = "On-device AI tags",
        aiIdle = "Waiting to analyze",
        aiPreparing = "Preparing on-device model",
        aiAnalyzing = "Analyzing photo",
        aiUnavailable = "Unavailable on this device",
        aiFailed = "Analysis failed. Try again later.",
        aiUnavailableNoticeTitle = "On-device AI not found",
        aiUnavailableNoticeBody = "This device cannot start Gemini Nano right now. Some smart frames, photo understanding, and auto enhancement features are unavailable. Capture, album, regular frames, and saving remain available.",
        aiUnavailableNoticeAction = "Continue",
        aiSubjectLabel = "Subject",
        aiSceneLabel = "Scene",
        aiColorsLabel = "Colors",
        aiConfidenceLabel = "Confidence",
        aiConfidenceLowLabel = "Low",
        aiConfidenceMediumLabel = "Medium",
        aiConfidenceHighLabel = "High",
        aiFrameSuggestionLabel = "AI frame pick",
        aiFrameApplyLabel = "Use pick",
        aiFrameAppliedLabel = "Applied",
        aiFrameGeneratorTitle = "Gemini frame",
        aiFramePromptPlaceholder = "Describe the mood",
        aiFrameGenerateLabel = "Generate",
        aiFrameRefineLabel = "Refine",
        aiFrameGenerating = "Generating frame",
        aiFrameGeneratedLabel = "Frame generated",
        aiFrameUnavailable = "Unavailable on this device",
        aiFrameGenerationFailed = "Could not generate. Try again later.",
        myFramesTitle = "My frames",
        myFrameSelectedLabel = "Selected",
        frameManagerOpenLabel = "Manage frames",
        frameManagerDescription = "Create custom frames and set one as the capture default.",
        frameManagerPromptPlaceholder = "Describe a frame style",
        frameManagerGenerateLabel = "Generate frame",
        frameManagerGeneratingLabel = "Generating",
        frameManagerEmptyLabel = "No custom frames yet",
        frameManagerUseAsDefaultLabel = "Set default",
        frameManagerDefaultLabel = "Default",
        aiModelLabel = "On-device model",
        aiProviderGeminiLabel = "Gemini Nano",
        aiProviderDescription = "Gemini Nano runs photo understanding and tag inference on device through system AICore.",
        soundEffectsLabel = "Sound effects",
        soundEffectsOnLabel = "On",
        soundEffectsOffLabel = "Off",
        shutterSoundLabel = "Shutter tone",
        soundStyleClassicLabel = "Classic",
        soundStyleCrispLabel = "Crisp",
        soundStyleSoftLabel = "Soft",
        soundVolumeLabel = "Volume",
        soundVolumeSoftLabel = "Low",
        soundVolumeNormalLabel = "Medium",
        soundVolumeLoudLabel = "High",
        soundCreditLabel = "Source: OpenGameArt CC0 camera shutter",
        pullForFullScreenHint = "Keep pulling for full-screen camera",
        stampFrameLabel = "Stamp",
        polaroidFrameLabel = "Polaroid",
        filmFrameLabel = "Film",
        colorFrameLabel = "Color",
      )
  }

internal fun DefaultAlbum.displayName(copy: CameraCopy): String =
  when (this) {
    DefaultAlbum.Capsule -> copy.capsuleAlbumLabel
    DefaultAlbum.Camera -> copy.cameraAlbumLabel
    DefaultAlbum.Framed -> copy.framedAlbumLabel
  }

internal fun CameraLens.cameraSelector(): CameraSelector =
  when (this) {
    CameraLens.Front -> CameraSelector.DEFAULT_FRONT_CAMERA
    CameraLens.Back -> CameraSelector.DEFAULT_BACK_CAMERA
  }

internal fun CameraDisplayStyle.displayName(copy: CameraCopy): String =
  when (this) {
    CameraDisplayStyle.PullList -> copy.cameraDisplayStylePullLabel
    CameraDisplayStyle.StageList -> copy.cameraDisplayStyleStageLabel
  }

internal fun DynamicIslandCoverMode.displayName(copy: CameraCopy): String =
  when (this) {
    DynamicIslandCoverMode.Precise -> copy.dynamicIslandCoverPreciseLabel
    DynamicIslandCoverMode.Comfort -> copy.dynamicIslandCoverComfortLabel
    DynamicIslandCoverMode.Maximum -> copy.dynamicIslandCoverMaximumLabel
  }

internal fun CameraCopy.frameStyleLabel(frameStyle: PhotoFrameStyle): String =
  when (frameStyle) {
    PhotoFrameStyle.Stamp -> stampFrameLabel
    PhotoFrameStyle.Polaroid -> polaroidFrameLabel
    PhotoFrameStyle.Film -> filmFrameLabel
    PhotoFrameStyle.ColorPop -> colorFrameLabel
  }

internal fun CameraCopy.confidenceLabel(confidence: PhotoInsightConfidence): String =
  when (confidence) {
    PhotoInsightConfidence.Low -> aiConfidenceLowLabel
    PhotoInsightConfidence.Medium -> aiConfidenceMediumLabel
    PhotoInsightConfidence.High -> aiConfidenceHighLabel
  }

internal fun CameraCopy.shutterSoundStyleLabel(style: ShutterSoundStyle): String =
  when (style) {
    ShutterSoundStyle.Classic -> soundStyleClassicLabel
    ShutterSoundStyle.Crisp -> soundStyleCrispLabel
    ShutterSoundStyle.Soft -> soundStyleSoftLabel
  }

internal fun CameraCopy.soundEffectVolumeLabel(volume: SoundEffectVolume): String =
  when (volume) {
    SoundEffectVolume.Soft -> soundVolumeSoftLabel
    SoundEffectVolume.Normal -> soundVolumeNormalLabel
    SoundEffectVolume.Loud -> soundVolumeLoudLabel
  }

private fun CameraLanguage.resolve(): ResolvedLanguage =
  when (this) {
    CameraLanguage.System ->
      if (Locale.getDefault().language.equals("zh", ignoreCase = true)) {
        ResolvedLanguage.ChineseSimplified
      } else {
        ResolvedLanguage.English
      }
    CameraLanguage.English -> ResolvedLanguage.English
    CameraLanguage.ChineseSimplified -> ResolvedLanguage.ChineseSimplified
  }

private fun String?.toCameraLanguage(): CameraLanguage =
  CameraLanguage.entries.firstOrNull { it.storageKey == this } ?: CameraLanguage.System

private fun String?.toDefaultAlbum(): DefaultAlbum =
  DefaultAlbum.entries.firstOrNull { it.storageKey == this } ?: DefaultAlbum.Capsule

private fun String?.toCameraLens(): CameraLens =
  CameraLens.entries.firstOrNull { it.storageKey == this } ?: CameraLens.Front

private fun String?.toCameraDisplayStyle(): CameraDisplayStyle =
  CameraDisplayStyle.entries.firstOrNull { it.storageKey == this } ?: CameraDisplayStyle.PullList

private fun String?.toDynamicIslandCoverMode(): DynamicIslandCoverMode =
  DynamicIslandCoverMode.entries.firstOrNull { it.storageKey == this } ?: DynamicIslandCoverMode.Comfort

private fun String?.toShutterSoundStyle(): ShutterSoundStyle =
  ShutterSoundStyle.entries.firstOrNull { it.storageKey == this } ?: ShutterSoundStyle.Classic

private fun String?.toSoundEffectVolume(): SoundEffectVolume =
  SoundEffectVolume.entries.firstOrNull { it.storageKey == this } ?: SoundEffectVolume.Normal
