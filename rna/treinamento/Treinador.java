package rna.treinamento;

import rna.modelos.Modelo;

/**
 * Disponibilzia uma interface para usar os métodos de treino e treino em
 * lote da Rede Neural.
 */
public class Treinador{

   /**
    * Auxiliar na verificação do cálculo do histórico de custos.
    */
   public boolean calcularHistorico = false;
   
   /**
    * Auxiliar para o treinador.
    */
   AuxiliarTreino aux;

   /**
    * Operador do treino sequencial.
    */
   Treino treino;

   /**
    * Operador do treino em lotes.
    */
   TreinoLote treinoLote;

   /**
    * Responsável por organizar os tipos dos modelos.
    */
   public Treinador(){
      aux =        new AuxiliarTreino();
      treino =     new Treino(calcularHistorico);
      treinoLote = new TreinoLote(calcularHistorico);
   }

   /**
    * Configura a seed inicial do gerador de números aleatórios.
    * @param seed nova seed.
    */
   public void configurarSeed(long seed){
      this.treino.configurarSeed(seed);
      this.treinoLote.configurarSeed(seed);
   }

   /**
    * Configura o cálculo para o histórico de perdas durante o treinamento.
    * @param calcularHistorico calcular ou não o histórico de custo.
    */
   public void configurarHistoricoCusto(boolean calcularHistorico){
      this.calcularHistorico = calcularHistorico;
      treino.configurarHistorico(calcularHistorico);
      treinoLote.configurarHistorico(calcularHistorico);
   }

   /**
    * Treina o modelo ajustando seus parâmetros treináveis usando
    * os dados fornecidos.
    * @param modelo modelo que será treinada.
    * @param entradas dados de entrada para o treino.
    * @param saidas dados de saída correspondente as entradas para o treino.
    * @param epochs quantidade de épocas de treinamento.
    * @param logs logs para perda durante as épocas de treinamento.
    */
   public void treino(Modelo modelo, Object[] entradas, Object[] saidas, int epochs, boolean logs){
      executar(modelo, entradas, saidas, epochs, 0, logs);
   }

   /**
    * Treina a rede neural calculando os erros dos neuronios, seus gradientes para cada peso e 
    * passando essas informações para o otimizador configurado ajustar os pesos.
    * @param modelo rede neural que será treinada.
    * @param entradas dados de entrada para o treino.
    * @param saidas dados de saída correspondente as entradas para o treino.
    * @param epochs quantidade de épocas de treinamento.
    * @param tamLote tamanho do lote.
    * @param logs logs para perda durante as épocas de treinamento.
    */
   public void treino(Modelo modelo, Object[] entradas, Object[] saidas, int epochs, int tamLote, boolean logs){
      executar(modelo, entradas, saidas, epochs, tamLote, logs);
   }

   /**
    * Executa a função de treino de acordo com os valores configurados.
    * @param modelo rede neural que será treinada.
    * @param entradas dados de entrada para o treino.
    * @param saidas dados de saída correspondente as entradas para o treino.
    * @param epochs quantidade de épocas de treinamento.
    * @param tamLote tamanho do lote.
    * @param logs logs para perda durante as épocas de treinamento.
    */
   private void executar(Modelo modelo, Object[] entradas, Object[] saidas, int epochs, int tamLote, boolean logs){
      if(entradas.length < saidas.length){
         throw new IllegalArgumentException(
            "Os dados de entrada e saída devem conter a mesma quantidade de amostras, " +
            "entrada = " + entradas.length + ", saida = " + saidas.length
         );
      }

      if(tamLote > 1){
         treinoLote.treinar(
            modelo,
            entradas.clone(),
            saidas.clone(),
            epochs,
            tamLote,
            logs
         );
         treinoLote.ultimoUsado = true;
      
      }else{
         treino.treinar(
            modelo,
            entradas.clone(), 
            saidas.clone(), 
            epochs,
            logs
         );
         treino.ultimoUsado = true;
      }

      treino.ultimoUsado = treinoLote.ultimoUsado ? false : true;
   }

   /**
    * Retorna uma lista contendo os valores de custo da rede
    * a cada época de treinamento.
    * @return lista com os custo por época durante a fase de treinamento.
    */
   public double[] obterHistorico(){
      return treino.ultimoUsado ? treino.historico() : treinoLote.historico();
   }
   
}
