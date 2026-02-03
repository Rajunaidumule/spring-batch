package com.td.springBatch.config;

import com.td.springBatch.model.Employee;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class EmployeeProcessor implements ItemProcessor<Employee,Employee> {
    @Override
    public @Nullable Employee process(Employee item) throws Exception {
        return item;
    }
}
