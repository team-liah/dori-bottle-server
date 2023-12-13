package com.liah.doribottle.config.security.acl

import com.liah.doribottle.domain.common.AclEntity
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class AclManager(
    private val aclService: CustomJdbcMutableService
) {
    fun getAuthorizedUsers(aclEntity: AclEntity, permission: Permission): List<UUID> {
        val acl = getOrCreateAcl(aclEntity.toOi())
        return acl.entries
            .filter { it.sid is PrincipalSid && it.permission.mask >= permission.mask }
            .map { UUID.fromString((it.sid as PrincipalSid).principal) }
    }

    fun getAuthorizedAuthorities(aclEntity: AclEntity, permission: Permission): List<String> {
        val acl = getOrCreateAcl(aclEntity.toOi())
        return acl.entries
            .filter { it.sid is GrantedAuthoritySid && it.permission.mask >= permission.mask }
            .map { (it.sid as GrantedAuthoritySid).grantedAuthority }
    }

    fun getAuthorizedACL(aclEntity: AclEntity, principal: UUID?, vararg authorities: String): List<UUID> {
        val oi = aclEntity.toOi()
        val principalSid = principal?.let { PrincipalSid(it.toString()) }
        val grantedAuthoritySids = authorities.map { GrantedAuthoritySid(it) }.toTypedArray()
        val sids = if (principalSid == null) {
            listOf(*grantedAuthoritySids)
        } else {
            listOf(principalSid, *grantedAuthoritySids)
        }.toTypedArray()

        return aclService.getObjectIdIdentitiesIn(oi.type, *sids)
            .map { UUID.fromString(it) }
    }

    fun addPermissionForUsers(aclEntity: AclEntity, permission: Permission, vararg principals: UUID) {
        val sids = principals.map { PrincipalSid(it.toString()) }.toTypedArray()
        addPermissionForSids(aclEntity, permission, *sids)
    }

    fun addPermissionForAuthorities(aclEntity: AclEntity, permission: Permission, vararg authorities: String) {
        val sids = authorities.map { GrantedAuthoritySid(it) }.toTypedArray()
        addPermissionForSids(aclEntity, permission, *sids)
    }

    fun removePermissionForUsers(aclEntity: AclEntity, permission: Permission, vararg principals: UUID) {
        val sids = principals.map { PrincipalSid(it.toString()) }.toTypedArray()
        removePermissionForSids(aclEntity, permission, *sids)
    }

    fun removePermissionForAuthorities(aclEntity: AclEntity, permission: Permission, vararg authorities: String) {
        val sids = authorities.map { GrantedAuthoritySid(it) }.toTypedArray()
        removePermissionForSids(aclEntity, permission, *sids)
    }

    private fun addPermissionForSids(aclEntity: AclEntity, permission: Permission, vararg sids: Sid) {
        val acl = getOrCreateAcl(aclEntity.toOi())
        grantPermissionForSids(acl, permission, *sids)
    }

    private fun removePermissionForSids(aclEntity: AclEntity, permission: Permission, vararg sids: Sid) {
        val acl = getOrCreateAcl(aclEntity.toOi())
        revokePermissionForSids(acl, permission, *sids)
    }

    private fun getOrCreateAcl(oi: ObjectIdentity): MutableAcl {
        return try {
            aclService.readAclById(oi) as MutableAcl
        } catch (e: NotFoundException) {
            aclService.createAcl(oi)
        }
    }

    private fun grantPermissionForSids(acl: MutableAcl, permission: Permission, vararg sids: Sid) {
        if (sids.isEmpty()) return
        sids.forEach { sid -> acl.insertAce(acl.entries.size, permission, sid, true) }
        aclService.updateAcl(acl)
    }

    private fun revokePermissionForSids(acl: MutableAcl, permission: Permission, vararg sids: Sid) {
        if (sids.isEmpty()) return
        sids.forEach { sid ->
            val aclIndex = acl.entries.indexOfFirst { it.permission == permission && it.sid == sid }
            if (aclIndex > -1) {
                acl.deleteAce(aclIndex)
            }
        }
        aclService.updateAcl(acl)
    }
}