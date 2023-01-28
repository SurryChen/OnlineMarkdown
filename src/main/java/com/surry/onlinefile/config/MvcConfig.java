package com.surry.onlinefile.config;

import com.surry.onlinefile.common.JacksonObjectMapper;
import com.surry.onlinefile.interceptor.LoginIntercepter;
import com.surry.onlinefile.interceptor.RefreshTokenIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * mvc的配置
 * WebMvcConfigurationSupport
 * webMvcConfiguration
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    LoginIntercepter loginIntercepter;
    @Autowired
    RefreshTokenIntercepter refreshTokenIntercepter;

    /**
     * 拦截器的配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        List<String> loginPath = new ArrayList<>();
        loginPath.add("/user/modify/**");
        loginPath.add("/user/getInformation");
        loginPath.add("/picture/**");
        loginPath.add("/article");
        loginPath.add("/article/requireAllJoinArticle");
        loginPath.add("/article/addWriter");
        loginPath.add("/article/requireOneArticle");
        loginPath.add("/article/deleteWriter");
        loginPath.add("/article/requireAllWriter");
        loginPath.add("/article/file");
        loginPath.add("/article/modifyContent");
        loginPath.add("/article/modifyName");
        registry.addInterceptor(refreshTokenIntercepter).addPathPatterns("/**").order(0);
        registry.addInterceptor(loginIntercepter).addPathPatterns(loginPath).order(1);

    }

    /**
     * 跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .maxAge(1800)
                .allowedOrigins("*");
    }

    /**
     * 静态资源的映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/picture/**").addResourceLocations("file:\\C:\\Users\\Administrator\\Desktop\\onlinefile" + File.separator + "picture" + File.separator);
        registry.addResourceHandler("/article/**").addResourceLocations("file:\\C:\\Users\\Administrator\\Desktop\\onlinefile" + File.separator + "article" + File.separator);

    }

    /**
     * 扩展mvc框架的消息转换器
     *
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
//        log.info("扩展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0, messageConverter);
    }

}
