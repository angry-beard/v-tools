package com.v.core.base.config.swagger;

import com.v.core.base.constants.VConstant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@ConfigurationProperties("swagger")
public class SwaggerProperties {
    /**
     * swagger会解析的包路径
     **/
    private List<String> basePackages = new ArrayList<>(Collections.singletonList(VConstant.BASE_PACKAGES));
    /**
     * swagger会解析的url规则
     **/
    private List<String> basePath = new ArrayList<>();
    /**
     * 在basePath基础上需要排除的url规则
     **/
    private List<String> excludePath = new ArrayList<>();
    /**
     * 标题
     **/
    private String title = "V 接口文档系统";
    /**
     * 描述
     **/
    private String description = "V 接口文档系统";
    /**
     * 版本
     **/
    private String version = "1.0.0";
    /**
     * 许可证
     **/
    private String license = "";
    /**
     * 许可证URL
     **/
    private String licenseUrl = "";
    /**
     * 服务条款URL
     **/
    private String termsOfServiceUrl = "";

    /**
     * host信息
     **/
    private String host = "";
    /**
     * 联系人信息
     */
    private Contact contact = new Contact();
    /**
     * 全局统一鉴权配置
     **/
    private Authorization authorization = new Authorization();

    @Data
    @NoArgsConstructor
    public static class Contact {

        /**
         * 联系人
         **/
        private String name = "xx";
        /**
         * 联系人url
         **/
        private String url = "";
        /**
         * 联系人email
         **/
        private String email = "xx@163.com";

    }

    @Data
    @NoArgsConstructor
    public static class Authorization {

        /**
         * 鉴权策略ID，需要和SecurityReferences ID保持一致
         */
        private String name = "";

        /**
         * 需要开启鉴权URL的正则
         */
        private String authRegex = "^.*$";

        /**
         * 鉴权作用域列表
         */
        private List<AuthorizationScope> authorizationScopeList = new ArrayList<>();

        private List<String> tokenUrlList = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class AuthorizationScope {
        /**
         * 作用域名称
         */
        private String scope = "";

        /**
         * 作用域描述
         */
        private String description = "";
    }
}
