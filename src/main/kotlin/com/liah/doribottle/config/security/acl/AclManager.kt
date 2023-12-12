package com.liah.doribottle.config.security.acl

import com.liah.doribottle.domain.common.AclEntity
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.security.acls.model.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class AclManager(
    private val aclService: JdbcMutableAclService
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

    fun addPermissionForUser(aclEntity: AclEntity, permission: Permission, principal: UUID) {
        val sid = PrincipalSid(principal.toString()) as Sid
        addPermissionForSid(aclEntity, permission, sid)
    }

    fun addPermissionForUsers(aclEntity: AclEntity, permission: Permission, principals: Collection<UUID>) {
        val sids = principals.map { PrincipalSid(it.toString()) }.toSet()
        addPermissionForSids(aclEntity, permission, sids)
    }

    fun addPermissionForAuthority(aclEntity: AclEntity, permission: Permission, authority: String) {
        val sid = GrantedAuthoritySid(authority) as Sid
        addPermissionForSid(aclEntity, permission, sid)
    }

    fun addPermissionForAuthorities(aclEntity: AclEntity, permission: Permission, authorities: Collection<String>) {
        val sids = authorities.map { GrantedAuthoritySid(it) }.toSet()
        addPermissionForSids(aclEntity, permission, sids)
    }

    fun removePermissionForUser(aclEntity: AclEntity, permission: Permission, principal: UUID) {
        val sid = PrincipalSid(principal.toString()) as Sid
        removePermissionForSid(aclEntity, permission, sid)
    }

    fun removePermissionForUsers(aclEntity: AclEntity, permission: Permission, principals: Collection<UUID>) {
        val sids = principals.map { PrincipalSid(it.toString()) }.toSet()
        removePermissionForSids(aclEntity, permission, sids)
    }

    fun removePermissionForAuthority(aclEntity: AclEntity, permission: Permission, authority: String) {
        val sid = GrantedAuthoritySid(authority) as Sid
        removePermissionForSid(aclEntity, permission, sid)
    }

    fun removePermissionForAuthorities(aclEntity: AclEntity, permission: Permission, authorities: Collection<String>) {
        val sids = authorities.map { GrantedAuthoritySid(it) }.toSet()
        removePermissionForSids(aclEntity, permission, sids)
    }

    private fun addPermissionForSid(aclEntity: AclEntity, permission: Permission, sid: Sid) {
        val acl = getOrCreateAcl(aclEntity.toOi())
        grantPermissionForSid(acl, permission, sid, true)
    }

    private fun addPermissionForSids(aclEntity: AclEntity, permission: Permission, sids: Set<Sid>) {
        val acl = getOrCreateAcl(aclEntity.toOi())
        grantPermissionForSids(acl, permission, sids, true)
    }

    private fun removePermissionForSid(aclEntity: AclEntity, permission: Permission, sid: Sid) {
        val acl = getOrCreateAcl(aclEntity.toOi())
        revokePermissionForSid(acl, permission, sid)
    }

    private fun removePermissionForSids(aclEntity: AclEntity, permission: Permission, sids: Set<Sid>) {
        val acl = getOrCreateAcl(aclEntity.toOi())
        revokePermissionForSids(acl, permission, sids)
    }

    private fun getOrCreateAcl(oi: ObjectIdentity): MutableAcl {
        return try {
            aclService.readAclById(oi) as MutableAcl
        } catch (e: NotFoundException) {
            aclService.createAcl(oi)
        }
    }

    private fun grantPermissionForSid(acl: MutableAcl, permission: Permission, sid: Sid, granting: Boolean) {
        acl.insertAce(acl.entries.size, permission, sid, granting)
        aclService.updateAcl(acl)
    }

    private fun grantPermissionForSids(acl: MutableAcl, permission: Permission, sids: Set<Sid>, granting: Boolean) {
        sids.forEach { sid -> acl.insertAce(acl.entries.size, permission, sid, granting) }
        aclService.updateAcl(acl)
    }

    private fun revokePermissionForSid(acl: MutableAcl, permission: Permission, sid: Sid) {
        val aclIndex = acl.entries.indexOfFirst { it.permission == permission && it.sid == sid }
        if (aclIndex > -1) {
            acl.deleteAce(aclIndex)
        }
        aclService.updateAcl(acl)
    }

    private fun revokePermissionForSids(acl: MutableAcl, permission: Permission, sids: Set<Sid>) {
        sids.forEach { sid ->
            val aclIndex = acl.entries.indexOfFirst { it.permission == permission && it.sid == sid }
            if (aclIndex > -1) {
                acl.deleteAce(aclIndex)
            }
        }
        aclService.updateAcl(acl)
    }
}