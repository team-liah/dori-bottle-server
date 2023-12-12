package com.liah.doribottle.config.security.acl

import jakarta.persistence.*

@Entity
@Table(
    name = "acl_entry",
    uniqueConstraints = [UniqueConstraint(columnNames = ["acl_object_identity", "ace_order"])]
)
class AclEntry (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acl_object_identity", nullable = false)
    var aclObjectIdentity: AclObjectIdentity,

    @Column(name = "ace_order", nullable = false)
    var aceOrder: Int,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sid", nullable = false)
    var sid: AclSid,

    @Column(name = "mask", nullable = false, columnDefinition = "INTEGER")
    var mask: Int,

    @Column(name = "granting", nullable = false)
    var granting: Boolean,

    @Column(name = "audit_success", nullable = false)
    var auditSuccess: Boolean,

    @Column(name = "audit_failure", nullable = false)
    var auditFailure: Boolean
)