package com.example.core.database.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.core.database.entity.RoleEntity
import com.example.core.database.entity.UserEntity
import com.example.core.database.entity.UserRoleCrossRef

data class UserWithRoles(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "roleId",
        associateBy = Junction(UserRoleCrossRef::class)
    )
    val roles: List<RoleEntity>
)
