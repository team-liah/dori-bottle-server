package com.liah.doribottle.repository.inquiry

import com.liah.doribottle.domain.inquiry.Inquiry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InquiryRepository : JpaRepository<Inquiry, UUID>