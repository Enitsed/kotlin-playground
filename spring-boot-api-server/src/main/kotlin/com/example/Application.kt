package com.example

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(Info()
                .title("Spring Boot Users API")
                .version("1.0.0")
                .description("A comprehensive REST API for user management built with Spring Boot and Kotlin")
            )
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
