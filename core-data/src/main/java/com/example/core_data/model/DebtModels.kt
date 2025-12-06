package com.example.core_data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Long,
    val name: String? = null,
    val surname: String? = null
)

@Serializable
data class DebtInfoDTO(
    val user: UserDTO,
    val totalAmount: Double? = null
)

@Serializable
data class TravelDebtsResponseDTO(
    val debts: List<DebtInfoDTO> = emptyList(),
    val creditors: List<DebtInfoDTO> = emptyList()
)
