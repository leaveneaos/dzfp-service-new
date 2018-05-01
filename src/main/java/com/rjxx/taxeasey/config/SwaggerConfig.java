package com.rjxx.taxeasey.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by rj-wyh on 2017/4/10.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket buildQRAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("openAPI")
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.rjxx.taxeasey.controller.common"))
                .paths(PathSelectors.any())//过滤的接口
                .build()
                .apiInfo(buildQRInfo());
    }

    private ApiInfo buildQRInfo() {
        return new ApiInfoBuilder()
                .title("开票通对外API")
                .description("开票通2.0")
                .termsOfServiceUrl("https://gitee.com/wyhtoString/projects")
                .contact("wangyahui")
                .version("2.0")
                .build();
    }
}
