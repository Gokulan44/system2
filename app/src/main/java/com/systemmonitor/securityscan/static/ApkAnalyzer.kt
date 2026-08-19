package com.systemmonitor.securityscan.static

import com.systemmonitor.securityscan.input.ScanTarget
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class StaticAnalysisResult(
    val packageName: String,
    val permissions: List<String>,
    val components: List<ParsedComponent>,
    val services: List<ParsedComponent>,
    val receivers: List<ParsedComponent>,
    val providers: List<ParsedComponent>,
    val usesCleartextTraffic: Boolean,
    val certificate: CertificateInfo?,
    val dexAnalysis: DexAnalysisResult?,
    val stringAnalysis: StringAnalysisResult?,
    val nativeAnalysis: NativeAnalysisResult?
)

@Singleton
class ApkAnalyzer @Inject constructor(
    private val manifestAnalyzer: ManifestAnalyzer,
    private val permissionAnalyzer: PermissionAnalyzer,
    private val componentAnalyzer: ComponentAnalyzer,
    private val serviceAnalyzer: ServiceAnalyzer,
    private val receiverAnalyzer: ReceiverAnalyzer,
    private val providerAnalyzer: ProviderAnalyzer,
    private val certificateAnalyzer: CertificateAnalyzer,
    private val dexAnalyzer: DexAnalyzer,
    private val stringAnalyzer: StringAnalyzer,
    private val nativeLibraryAnalyzer: NativeLibraryAnalyzer
) {
    fun analyze(target: ScanTarget): StaticAnalysisResult {
        val manifest = manifestAnalyzer.analyze(target)
        val permissions = permissionAnalyzer.analyze(manifest)
        val components = componentAnalyzer.analyze(manifest)
        val services = serviceAnalyzer.analyze(manifest)
        val receivers = receiverAnalyzer.analyze(manifest)
        val providers = providerAnalyzer.analyze(manifest)
        
        val cert = certificateAnalyzer.analyze(target)

        val file = File(target.apkPath)

        val dexResult = dexAnalyzer.analyze(file)
        val stringResult = stringAnalyzer.analyze(file)
        val nativeResult = nativeLibraryAnalyzer.analyze(file)

        return StaticAnalysisResult(
            packageName = manifest.packageName.ifEmpty { target.packageName },
            permissions = permissions,
            components = components,
            services = services,
            receivers = receivers,
            providers = providers,
            usesCleartextTraffic = manifest.usesCleartextTraffic,
            certificate = cert,
            dexAnalysis = dexResult,
            stringAnalysis = stringResult,
            nativeAnalysis = nativeResult
        )
    }
}
