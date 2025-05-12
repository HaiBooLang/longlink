//package com.pyc.shortlink.project.config;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//
//import javax.sql.DataSource;
//
//@Configuration
//@MapperScan(sqlSessionFactoryRef = "kgsSqlSessionFactory")
//public class KGSDataSourceConfig {
//
//    @Value("${kgs.datasource.url}")
//    private String url;
//
//    @Value("${kgs.datasource.username}")
//    private String username;
//
//    @Value("${kgs.datasource.password}")
//    private String password;
//
//    @Value("${kgs.datasource.driver-class-name}")
//    private String driverClassName;
//
//    @Bean(name = "kgsDataSource")
//    public DataSource kgsDataSource() {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setUrl(url);
//        dataSource.setUsername(username);
//        dataSource.setPassword(password);
//        dataSource.setDriverClassName(driverClassName);
//        return dataSource;
//    }
//
////    @Bean(name = "kgsSqlSessionFactory")
////    public SqlSessionFactory kgsSqlSessionFactory(@Qualifier("kgsDataSource") DataSource dataSource) throws Exception {
////        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
////        factoryBean.setDataSource(dataSource);
////        return factoryBean.getObject();
////    }
////
////    @Bean(name = "kgsTransactionManager")
////    public DataSourceTransactionManager kgsTransactionManager(@Qualifier("kgsDataSource") DataSource dataSource) {
////        return new DataSourceTransactionManager(dataSource);
////    }
//
//    @Bean(name = "kgsJdbcTemplate")
//    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
//}
