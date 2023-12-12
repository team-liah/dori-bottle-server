package com.liah.doribottle.config.security.acl

import jakarta.persistence.*

@Entity
@Table(
    name = "acl_sid",
    uniqueConstraints = [UniqueConstraint(columnNames = ["sid", "principal"])]
)
class AclSid (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column(name = "sid", nullable = false, length = 100)
    var sid: String,

    @Column(name = "principal", nullable = false)
    var principal: Boolean
)