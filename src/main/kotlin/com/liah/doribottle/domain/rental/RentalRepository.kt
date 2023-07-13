package com.liah.doribottle.domain.rental

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RentalRepository : JpaRepository<Rental, UUID>