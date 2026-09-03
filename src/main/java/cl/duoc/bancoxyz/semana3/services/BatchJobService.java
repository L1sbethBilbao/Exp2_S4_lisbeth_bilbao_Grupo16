package cl.duoc.bancoxyz.semana3.services;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.semana3.exceptions.JobLaunchException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BatchJobService {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job transaccionesDiariasJob;
    private final Job interesesMensualesJob;
    private final Job estadosCuentaAnualesJob;

    public BatchJobService(JobLauncher jobLauncher,
                           JobExplorer jobExplorer,
                           @Qualifier("transaccionesDiariasJob") Job transaccionesDiariasJob,
                           @Qualifier("interesesMensualesJob") Job interesesMensualesJob,
                           @Qualifier("estadosCuentaAnualesJob") Job estadosCuentaAnualesJob) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.transaccionesDiariasJob = transaccionesDiariasJob;
        this.interesesMensualesJob = interesesMensualesJob;
        this.estadosCuentaAnualesJob = estadosCuentaAnualesJob;
    }

    public JobExecution ejecutarTransacciones() {
        return lanzar(transaccionesDiariasJob);
    }

    public JobExecution ejecutarIntereses() {
        return lanzar(interesesMensualesJob);
    }

    public JobExecution ejecutarEstadosCuenta() {
        return lanzar(estadosCuentaAnualesJob);
    }

    private JobExecution lanzar(Job job) {
        try {
            JobParametersBuilder parametersBuilder = new JobParametersBuilder(jobExplorer)
                    .getNextJobParameters(job);
            log.info("Lanzando Job {}", job.getName());
            return jobLauncher.run(job, parametersBuilder.toJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException
                | JobInstanceAlreadyCompleteException | JobParametersInvalidException ex) {
            log.error("No fue posible iniciar el Job {}", job.getName(), ex);
            throw new JobLaunchException("No fue posible iniciar el Job " + job.getName() + ": " + ex.getMessage(), ex);
        }
    }
}
