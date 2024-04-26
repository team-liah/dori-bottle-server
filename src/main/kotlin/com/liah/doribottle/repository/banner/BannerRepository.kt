package com.liah.doribottle.repository.banner

import com.liah.doribottle.domain.banner.Banner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BannerRepository : JpaRepository<Banner, UUID>