package com.liah.doribottle.config.security.acl

import jakarta.persistence.*

@Entity
@Table(
    name = "acl_object_identity",
    uniqueConstraints = [UniqueConstraint(columnNames = ["object_id_class", "object_id_identity"])]
)
class AclObjectIdentity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "object_id_class", nullable = false)
    var objectIdClass: AclClass,

    @Column(name = "object_id_identity", nullable = false, length = 36)
    var objectIdIdentity: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_object")
    var parent: AclObjectIdentity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_sid")
    var owner: AclSid? = null,

    @Column(name = "entries_inheriting", nullable = false)
    var entriesInheriting: Boolean
)