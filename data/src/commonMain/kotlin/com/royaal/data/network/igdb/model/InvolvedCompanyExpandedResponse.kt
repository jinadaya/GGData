package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvolvedCompanyExpandedResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("company") val company: CompanyExpandedResponse? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("developer") val developer: Boolean? = null,
    @SerialName("game") val game: GameExpandedResponse? = null,
    @SerialName("porting") val porting: Boolean? = null,
    @SerialName("publisher") val publisher: Boolean? = null,
    @SerialName("supporting") val supporting: Boolean? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
)
