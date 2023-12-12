package com.liah.doribottle.config.security.acl

import com.liah.doribottle.domain.user.Role
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.acls.AclPermissionEvaluator
import org.springframework.security.acls.domain.*
import org.springframework.security.acls.jdbc.BasicLookupStrategy
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.PermissionGrantingStrategy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import javax.sql.DataSource

@Configuration
class AclContext(
    private val dataSource: DataSource,
    private val cacheManager: CacheManager
) {
    @Bean
    fun methodSecurityExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        val permissionEvaluator = AclPermissionEvaluator(aclService())
        expressionHandler.setPermissionEvaluator(permissionEvaluator)
        return expressionHandler
    }

    @Bean
    fun aclService(): JdbcMutableAclService {
        val aclService = JdbcMutableAclService(dataSource, lookupStrategy(), aclCache())
        aclService.setClassIdentityQuery("SELECT @@IDENTITY")
        aclService.setSidIdentityQuery("SELECT @@IDENTITY")
        aclService.setAclClassIdSupported(true)
        return aclService
    }

    @Bean
    fun aclAuthorizationStrategy(): AclAuthorizationStrategy {
        return AclAuthorizationStrategyImpl(SimpleGrantedAuthority(Role.ADMIN.key))
    }

    @Bean
    fun permissionGrantingStrategy(): PermissionGrantingStrategy {
        return DefaultPermissionGrantingStrategy(ConsoleAuditLogger())
    }

    @Bean
    fun aclCache(): SpringCacheBasedAclCache {
        return SpringCacheBasedAclCache(
            cacheManager.getCache("aclCache"),
            permissionGrantingStrategy(),
            aclAuthorizationStrategy()
        )
    }

    @Bean
    fun lookupStrategy(): LookupStrategy {
        val lookupStrategy = BasicLookupStrategy(
            dataSource,
            aclCache(),
            aclAuthorizationStrategy(),
            ConsoleAuditLogger()
        )
        lookupStrategy.setAclClassIdSupported(true)

        return lookupStrategy
    }
}