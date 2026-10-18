package info.somrat.todokotlin.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    @get:JsonProperty("object")
    val obj: T? = null,
) {
    companion object {
        fun <T> ok(
            message: String,
            obj: T? = null,
        ) = ApiResponse(true, message, obj)

        fun failure(message: String) = ApiResponse<Nothing>(false, message)
    }
}

data class PageResponse<T>(
    val success: Boolean = true,
    val message: String = "",
    val totalItems: Long,
    val totalPage: Int,
    val currentPage: Int,
    val data: List<T>,
)
