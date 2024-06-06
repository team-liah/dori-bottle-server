package com.liah.doribottle.domain.common

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp
import java.io.Serializable

@Entity
@RevisionEntity
@Table(name = "revinfo")
class LongRevisionEntity : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev_id")
    var id: Long? = null

    @RevisionTimestamp
    @Column(name = "revtstmp")
    var timestamp: Long? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
