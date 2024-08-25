package com.tianji.common.autoconfigure.mybatis;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({MybatisPlusInterceptor.class, BaseMapper.class})
public class MybatisConfig {

    /**
     * @deprecated 存在任务更新数据导致updater写入0或null的问题，暂时废弃
     * @see MyBatisAutoFillInterceptor 通过自定义拦截器来实现自动注入creater和updater
     */
    // @Bean
    // @ConditionalOnMissingBean
    public BaseMetaObjectHandler baseMetaObjectHandler(){
        return new BaseMetaObjectHandler();
    }

    // 配置mybatis-plus的拦截器链
    // 声明动态表名 拦截器插件
    // DynamicTableNameInnerInterceptor 插件不是所有服务都用到，目前只有tj-learning服务声明了
    // @Autowired(required = false) 注入时声明非必须
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(@Autowired(required = false) DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        if (dynamicTableNameInnerInterceptor!=null) {
            // 声明了该动态表明的拦截器，需要加入到mp的拦截器链中，且要注意顺序（官网）
            interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        }
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(200L);
        // 配置分页插件
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        // 配置自动填充插件
        interceptor.addInnerInterceptor(new MyBatisAutoFillInterceptor());
        return interceptor;
    }
}
