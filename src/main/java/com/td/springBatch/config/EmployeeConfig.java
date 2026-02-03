package com.td.springBatch.config;

import com.td.springBatch.model.Employee;
import com.td.springBatch.partititon.EmployeePartittion;
import com.td.springBatch.repo.EmployeeRepository;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class EmployeeConfig {

    @Autowired
    EmployeeRepository repository;
    @Autowired
    JobRepository jobRepository;
    @Autowired
    private CustomerWriter writer;

    @Bean
    public FlatFileItemReader<Employee> reader() {
        FlatFileItemReader<Employee> reader = new FlatFileItemReader<>(lineMapper());
        reader.setLinesToSkip(1);
        reader.setName("CSV-READER");
        reader.setResource(new ClassPathResource("empdata.csv"));
        return reader;
    }

    @Bean
    public LineMapper <Employee> lineMapper(){
        BeanWrapperFieldSetMapper beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper();
        beanWrapperFieldSetMapper.setTargetType(Employee.class);

        DelimitedLineTokenizer lineTokenizer =  new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setNames("employeeId","empName","empDept");
        lineTokenizer.setStrict(false);
        DefaultLineMapper lineMapper = new DefaultLineMapper();
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        lineMapper.setLineTokenizer(lineTokenizer);

        return lineMapper;
    }

    @Bean
    public EmployeeProcessor processor() {
        return new EmployeeProcessor();
    }


    @Bean
    public Step step() {
        return new StepBuilder("employee-step", jobRepository)
                .<Employee,Employee>chunk(5)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step masterStep() {
        return new StepBuilder("master-step", jobRepository)
                .partitioner(step().getName(),partittion())
                .partitionHandler(partitionHandler())
                .build();
    }
    @Bean
    public Job job() {
        return new JobBuilder("employee-job",jobRepository).start(masterStep()).build();
    }

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setPrestartAllCoreThreads(true);
        executor.setThreadNamePrefix("AsyncTasker-");
        executor.initialize();
        return executor;
    }


    @Bean
    public EmployeePartittion partittion(){
        return new EmployeePartittion();
    }
    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(2);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(step());
        return taskExecutorPartitionHandler;
    }

}
