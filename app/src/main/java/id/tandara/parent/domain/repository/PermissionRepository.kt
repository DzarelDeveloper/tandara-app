package id.tandara.parent.domain.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.domain.model.LeaveRequest

interface PermissionRepository {
    suspend fun submitLeaveRequest(request: LeaveRequest): ApiResult<LeaveRequest>
    suspend fun getLeaveHistory(studentId: String): ApiResult<List<LeaveRequest>>
}
