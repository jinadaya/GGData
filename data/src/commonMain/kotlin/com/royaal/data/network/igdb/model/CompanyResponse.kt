package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompanyResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("change_date") val changeDate: Long? = null,
    @SerialName("change_date_category") val changeDateCategory: Int? = null,
    @SerialName("change_date_format") val changeDateFormat: Long? = null,
    @SerialName("changed_company_id") val changedCompanyId: Long? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("country") val country: Int? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("developed") val developed: List<Long> = emptyList(),
    @SerialName("logo") val logo: Long? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("parent") val parent: Long? = null,
    @SerialName("published") val published: List<Long> = emptyList(),
    @SerialName("slug") val slug: String? = null,
    @SerialName("start_date") val startDate: Long? = null,
    @SerialName("start_date_category") val startDateCategory: Int? = null,
    @SerialName("start_date_format") val startDateFormat: Long? = null,
    @SerialName("status") val status: Long? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("websites") val websites: List<Long> = emptyList(),
)