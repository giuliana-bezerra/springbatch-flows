package br.com.giulianabezerra.sbconditionalflow;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job job(@Qualifier("importarClientes") Step importarClientes,
      @Qualifier("processarPendenciasDosClientesFlowStep") Step processarPendenciasDosClientesFlowStep) {
    return jobBuilderFactory
        .get("job")
        .start(importarClientes)
        .next(processarPendenciasDosClientesFlowStep)
        .build();
  }

  @Bean
  public Step processarPendenciasDosClientesFlowStep(
      Flow processarPendenciasDosClientesFlow) {
    return stepBuilderFactory.get("processarPendenciasDosClientesFlowStep").flow(processarPendenciasDosClientesFlow)
        .build();
  }

  @Bean
  public Flow processarPendenciasDosClientesFlow(
      @Qualifier("marcarClientesComPendencias") Step marcarClientesComPendencias,
      @Qualifier("notificarClientesComPendencias") Step notificarClientesComPendencias) {
    return new FlowBuilder<Flow>("processarPendenciasDosClientes")
        .start(marcarClientesComPendencias)
        .next(notificarClientesComPendencias)
        .build();
  }

  @Bean
  public Step importarClientes() {
    return stepBuilderFactory
        .get("importarClientes")
        .tasklet(
            (StepContribution contribution, ChunkContext chunkContext) -> {
              System.out.println(
                  "1. Lê clientes de um arquivo\n" +
                      "2. Carrega clientes numa base de dados.");
              return RepeatStatus.FINISHED;
            })
        .build();
  }

  @Bean
  public Step marcarClientesComPendencias() {
    return stepBuilderFactory
        .get("marcarClientesComPendencias")
        .tasklet(
            (StepContribution contribution, ChunkContext chunkContext) -> {
              System.out.println(
                  "1. Lê clientes de uma base\n" +
                      "2. Marca os clientes com pendências de cadastro");
              return RepeatStatus.FINISHED;
            })
        .build();
  }

  @Bean
  public Step notificarClientesComPendencias() {
    return stepBuilderFactory
        .get("notificarClientesComPendencias")
        .tasklet(
            (StepContribution contribution, ChunkContext chunkContext) -> {
              System.out.println(
                  "1. Lê clientes com pendências no cadastro\n" +
                      "2. Envia emails solicitando ajuste");
              return RepeatStatus.FINISHED;
            })
        .build();
  }
}
