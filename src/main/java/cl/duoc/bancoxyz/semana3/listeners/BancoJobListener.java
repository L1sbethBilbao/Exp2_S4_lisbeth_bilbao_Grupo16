package cl.duoc.bancoxyz.semana3.listeners;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

import lombok.extern.slf4j.Slf4j;

/**
 * Resume leidos / escritos / omitidos y particiones worker al finalizar cada Job.
 */
@Slf4j
public class BancoJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(">> Iniciando Job particionado '{}' (ejecucion #{})",
                jobExecution.getJobInstance().getJobName(), jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long leidos = jobExecution.getStepExecutions().stream()
                .filter(se -> !se.getStepName().contains(":partition"))
                .mapToLong(StepExecution::getReadCount)
                .sum();
        long escritos = jobExecution.getStepExecutions().stream()
                .filter(se -> !se.getStepName().contains(":partition"))
                .mapToLong(StepExecution::getWriteCount)
                .sum();
        long saltados = jobExecution.getStepExecutions().stream()
                .filter(se -> !se.getStepName().contains(":partition"))
                .mapToLong(StepExecution::getSkipCount)
                .sum();
        long particiones = jobExecution.getStepExecutions().stream()
                .filter(se -> se.getStepName().contains(":partition"))
                .count();

        log.info("==================================================");
        log.info(">> Job '{}' finalizado con estado {}. Particiones={}, Leidos={}, Escritos={}, Saltados={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                particiones,
                leidos,
                escritos,
                saltados);
        jobExecution.getStepExecutions().forEach(step ->
                log.info("  Step [{}] leidos={} escritos={} omitidos={} estado={}",
                        step.getStepName(),
                        step.getReadCount(),
                        step.getWriteCount(),
                        step.getSkipCount(),
                        step.getStatus()));
        log.info("==================================================");
    }
}
