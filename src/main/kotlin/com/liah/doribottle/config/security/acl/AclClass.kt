package com.liah.doribottle.config.security.acl

import jakarta.persistence.*

@Entity
@Table(name = "acl_class")
class AclClass (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @Column(name = "class", nullable = false, length = 100, unique = true)
    var className: String,
    @Column(name = "class_id_type", length = 100)
    var classIdType: String?
)