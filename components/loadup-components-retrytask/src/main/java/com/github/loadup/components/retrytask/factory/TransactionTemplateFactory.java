package com.github.loadup.components.retrytask.factory;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class TransactionTemplateFactory {
    private Map<String, TransactionTemplate> transactionTemplates = new HashMap();

    public TransactionTemplate obtainTemplate(String bizType) {
        if (this.transactionTemplates.get(bizType) != null) {
            return this.transactionTemplates.get(bizType);
        } else {
            synchronized (this) {
                if (this.transactionTemplates.get(bizType) != null) {
                    return this.transactionTemplates.get(bizType);
                }

                TransactionTemplate transactionTemplate = new TransactionTemplate();
                DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
                transactionTemplate.setTransactionManager(transactionManager);
                this.transactionTemplates.put(bizType, transactionTemplate);
            }

            return this.transactionTemplates.get(bizType);
        }
    }
}