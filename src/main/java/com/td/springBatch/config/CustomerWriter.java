package com.td.springBatch.config;

import com.td.springBatch.model.Employee;
import com.td.springBatch.repo.EmployeeRepository;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CustomerWriter implements ItemWriter<Employee> {
    @Autowired
    private EmployeeRepository repository;

    @Override
    public void write(Chunk<? extends Employee> list) throws Exception {
        System.out.println("Thread name" + Thread.currentThread().getName());
        repository.saveAll(list);
    }
}
