package novayoung.log;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;

/**
 *
 * 该类的目的: 自动加载looklog相关的bean
 *
 * 注意!  该类仅在spring-boot项目中有效, spring-boot会通过META-INF/spring.factories文件, 找到novayoung.log.LoadBeans并调用initialize方法
 *
 */
public class LoadBeans implements ApplicationContextInitializer {



    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        if (applicationContext instanceof AbstractRefreshableConfigApplicationContext) {
            throw new IllegalStateException("unsupported!");
        }


        if ("AnnotationConfigEmbeddedWebApplicationContext".equals(applicationContext.getClass().getSimpleName())) {

            LookLogAppender.setApplicationContext(applicationContext);

            apppendAnnotationBean(applicationContext);
        }
    }

    private void apppendAnnotationBean(ConfigurableApplicationContext applicationContext) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(AnnotationScanBean.class);
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        beanDefinitionRegistry.registerBeanDefinition("annotationScanBean", beanDefinitionBuilder.getRawBeanDefinition());
    }

}
