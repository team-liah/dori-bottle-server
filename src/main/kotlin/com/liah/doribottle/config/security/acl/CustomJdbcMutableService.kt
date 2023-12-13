package com.liah.doribottle.config.security.acl

import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.AclCache
import org.springframework.security.acls.model.Sid
import javax.sql.DataSource

class CustomJdbcMutableService(
    dataSource: DataSource,
    lookupStrategy: LookupStrategy,
    aclCache: AclCache
) : JdbcMutableAclService(dataSource, lookupStrategy, aclCache) {
    private val selectObjectIdIdentityByAclSid: String = "select distinct " +
            "acl_object_identity.object_id_identity " +
            "from acl_object_identity " +
            "inner join acl_entry on acl_entry.acl_object_identity = acl_object_identity.id " +
            "inner join acl_class on acl_class.id = acl_object_identity.object_id_class " +
            "inner join acl_sid on acl_entry.sid = acl_sid.id " +
            "where acl_class.class = ? " +
            "and acl_sid.sid = ? " +
            "and acl_sid.principal = ? " +
            "and acl_entry.granting = true"

    private val selectObjectIdIdentityByAclSidIn: String = "select distinct " +
            "acl_object_identity.object_id_identity " +
            "from acl_object_identity " +
            "inner join acl_entry on acl_entry.acl_object_identity = acl_object_identity.id " +
            "inner join acl_class on acl_class.id = acl_object_identity.object_id_class " +
            "inner join acl_sid on acl_entry.sid = acl_sid.id " +
            "where acl_class.class = ? " +
            "and acl_sid.sid in (%s) " +
            "and acl_entry.granting = true"

    private val deleteAllEntry: String = "delete from acl_entry"
    private val deleteAllObjectIdentity: String = "delete from acl_object_identity"
    private val deleteAllSid: String = "delete from acl_sid"
    private val deleteAllClass: String = "delete from acl_class"

    fun getObjectIdIdentitiesBySid(type: String, sid: Sid): List<String> {
        return when (sid) {
            is PrincipalSid -> findObjectIdIdentitiesBySid(type, sid.principal, true)
            is GrantedAuthoritySid -> findObjectIdIdentitiesBySid(type, sid.grantedAuthority, false)
            else -> emptyList()
        }
    }

    fun getObjectIdIdentitiesBySidIn(type: String, vararg sids: Sid): List<String> {
        val sidNames = sids.mapNotNull { sid ->
            when (sid) {
                is PrincipalSid -> sid.principal
                is GrantedAuthoritySid -> sid.grantedAuthority
                else -> null
            }
        }
        if (sidNames.isEmpty()) return emptyList()
        return findObjectIdIdentitiesBySidIn(type, sidNames)
    }

//    fun deleteAll() {
//        this.jdbcOperations.update(this.deleteAllEntry)
//        this.jdbcOperations.update(this.deleteAllObjectIdentity)
//        this.jdbcOperations.update(this.deleteAllSid)
//        this.jdbcOperations.update(this.deleteAllClass)
//    }

    private fun findObjectIdIdentitiesBySid(type: String, sidName: String, sidIsPrincipal: Boolean): List<String> {
        return this.jdbcOperations.queryForList(
            this.selectObjectIdIdentityByAclSid,
            String::class.java,
            type,
            sidName,
            sidIsPrincipal
        )
    }

    private fun findObjectIdIdentitiesBySidIn(type: String, sidNames: List<String>): List<String> {
        val inSql = (1..sidNames.size).joinToString(", ") { "?" }
        return this.jdbcOperations.queryForList(
            String.format(this.selectObjectIdIdentityByAclSidIn, inSql),
            String::class.java,
            type,
            *sidNames.toTypedArray()
        )
    }
}