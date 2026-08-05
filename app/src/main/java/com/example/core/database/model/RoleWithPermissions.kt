package com.example.core.database.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.core.database.entity.PermissionEntity
import com.example.core.database.entity.RoleEntity
import com.example.core.database.entity.RolePermissionCrossRef

data class RoleWithPermissions(
    @Embedded val role: RoleEntity,
    @Relation(
        parentColumn = "roleId",
        entityColumn = "permissionId",
        associateBy = Junction(RolePermissionCrossRef::class)
    )
    val permissions: List<PermissionEntity>
)
