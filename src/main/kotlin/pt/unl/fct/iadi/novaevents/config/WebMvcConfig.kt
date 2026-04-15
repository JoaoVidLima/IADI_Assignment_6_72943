package pt.unl.fct.iadi.novaevents.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.unl.fct.iadi.novaevents.interceptor.RequestLoggingInterceptor

@Configuration
class WebMvcConfig(
    private val requestLoggingInterceptor: RequestLoggingInterceptor // This might be the loop
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestLoggingInterceptor)
    }
}