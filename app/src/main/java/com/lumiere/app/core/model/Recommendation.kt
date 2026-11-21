package com.lumiere.app.core.model

import java.util.UUID

enum class Sensitivity { NORMAL, SENSITIVE, CRITICAL }
enum class InferenceMethod { RULE, PATTERN_7D, PATTERN_30D, PATTERN_90D }
enum class Confidence { CALIBRATED, HEURISTIC, UNKNOWN }
enum class ApprovalClass { A0_CONSULT, A1_PREPARE, A2_LOW_IMPACT, A3_RELEVANT, A4_HIGH_IMPACT }
enum class Impact { LOW, MEDIUM, HIGH }
enum class Reversibility { REVERSIBLE, PARTIAL, IRREVERSIBLE }
enum class RecommendationKind { PREVENTIVE, CORRECTIVE, OPTIMIZATION, OPPORTUNITY, INFORMATIVE, SAFETY }
enum class RecommendationState { PROPOSED, APPROVED, REJECTED, EXECUTED, PARTIAL, FAILED }

sealed interface FactSource {
    data class FromModule(
        val moduleId: String,
        val recordId: String,
        val contractVersion: String,
        val timestampMillis: Long
    ) : FactSource
}

data class Fact(
    val id: String = UUID.randomUUID().toString(),
    val statement: String,
    val source: FactSource,
    val sensitivity: Sensitivity = Sensitivity.NORMAL
)

data class Inference(
    val id: String = UUID.randomUUID().toString(),
    val statement: String,
    val method: InferenceMethod,
    val confidence: Confidence,
    val supportingFactIds: List<String>
)

data class Suggestion(
    val id: String = UUID.randomUUID().toString(),
    val statement: String,
    val approvalClass: ApprovalClass,
    val targetModules: Set<String>,
    val impact: Impact,
    val alternatives: List<String>,
    val reversibility: Reversibility,
    val expiresAtMillis: Long?
)

data class Recommendation(
    val id: String = UUID.randomUUID().toString(),
    val kind: RecommendationKind,
    val state: RecommendationState = RecommendationState.PROPOSED,
    val goal: String,
    val facts: List<Fact>,
    val inferences: List<Inference>,
    val suggestions: List<Suggestion>,
    val affectedModules: Set<String>,
    val createdAtMillis: Long
)
