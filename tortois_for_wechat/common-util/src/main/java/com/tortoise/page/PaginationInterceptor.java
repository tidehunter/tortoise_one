package com.tortoise.page;


import org.apache.ibatis.builder.xml.dynamic.ForEachSqlNode;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by tortoise on 16/11/9.
 */
@Intercepts({@Signature(type=Executor.class,method="query",args={ MappedStatement.class, java.lang.Object.class, RowBounds.class, ResultHandler.class })})
public class PaginationInterceptor implements Interceptor {

    private static final Logger logger = Logger.getLogger(PaginationInterceptor.class);

    Dialect dialect = new OracleDialect();

    public java.lang.Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement=(MappedStatement)invocation.getArgs()[0];

        if(null != mappedStatement){
            String queryMethod = mappedStatement.getId();
            if(!Pattern.compile("queryAll").matcher(queryMethod).find()){
                return invocation.proceed();
            }
        }

        PageContext page = PageContext.getContext();

        if(null == page){
            return invocation.proceed();
        }

        java.lang.Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql().trim();
        RowBounds rowBounds = (RowBounds)invocation.getArgs()[2];
        java.lang.Object parameterObject = boundSql.getParameterObject();

        if(boundSql==null || boundSql.getSql()==null || "".equals(boundSql.getSql()))
            return null;
        if(page!=null)
        {
            //query total pages
            int totpage = 0;
            if (totpage==0)
            {
                try {
                    StringBuffer countSql  = new StringBuffer(originalSql.length()+100 );
                    countSql.append("select count(1) from (").append(originalSql).append(")");
                    System.out.println(countSql.toString());
                    Connection connection=mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection()  ;
                    PreparedStatement countStmt = connection.prepareStatement(countSql.toString());
                    setParameters(countStmt,mappedStatement,boundSql,parameterObject);
                    ResultSet rs = countStmt.executeQuery();
                    if (rs.next()) {
                        page.setTotalRows(rs.getInt(1));
                    }
                    rs.close();
                    countStmt.close();
                    connection.close();
                } catch (Exception e) {
                    logger.error("get total counts error!");
                }

            }

            if(rowBounds == null || rowBounds == RowBounds.DEFAULT){
                rowBounds= new RowBounds(page.getPageSize()*(page.getCurrentPage()-1),page.getPageSize());
            }
            //initalization
            page.init(page.getTotalRows(),page.getPageSize(),page.getCurrentPage());
            //page query
            Dialect dialect = new OracleDialect();
            String pagesql=dialect.getLimitString(originalSql, rowBounds.getOffset(), rowBounds.getLimit());
            invocation.getArgs()[2] = new RowBounds(RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
            BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), pagesql,boundSql.getParameterMappings(),boundSql.getParameterObject());
            for (ParameterMapping mapping : boundSql.getParameterMappings())
            {
                String prop = mapping.getProperty();
                if (boundSql.hasAdditionalParameter(prop))
                {
                    newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
                }
            }
            MappedStatement newMs = copyFromMappedStatement(mappedStatement,new BoundSqlSqlSource(newBoundSql));

            invocation.getArgs()[0]= newMs;
        }

        return invocation.proceed();
    }
    public static class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(java.lang.Object parameterObject) {
            return boundSql;
        }
    }
    public java.lang.Object plugin(java.lang.Object arg0) {
        // TODO Auto-generated method stub
        return Plugin.wrap(arg0, this);
    }
    public void setProperties(Properties arg0) {
        // TODO Auto-generated method stub


    }

    /**
     * set SQL param(?),refer org.apache.ibatis.executor.parameter.DefaultParameterHandler
     *
     * @param ps
     * @param mappedStatement
     * @param boundSql
     * @param parameterObject
     * @throws SQLException
     */
    private void setParameters(PreparedStatement ps,MappedStatement mappedStatement,BoundSql boundSql, java.lang.Object parameterObject) throws SQLException {
        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null: configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    java.lang.Object value;
                    String propertyName = parameterMapping.getProperty();
                    PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                    if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)&& boundSql.hasAdditionalParameter(prop.getName())) {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null) {
                            value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
                        }
                    } else {
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null) {
                        throw new ExecutorException("There was no TypeHandler found for parameter "+ propertyName + " of statement "+ mappedStatement.getId());
                    }
                    typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
                }
            }
        }
    }

    private MappedStatement copyFromMappedStatement(MappedStatement ms,
                                                    SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(),
                ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        builder.keyProperty(ms.getKeyProperty());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.cache(ms.getCache());
        MappedStatement newMs = builder.build();
        return newMs;
    }

}
