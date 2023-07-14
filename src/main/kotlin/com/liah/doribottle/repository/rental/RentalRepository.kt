package com.liah.doribottle.repository.rental

import com.liah.doribottle.domain.rental.Rental
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RentalRepository : JpaRepository<Rental, UUID>