package rna.avaliacao.metrica;

import rna.modelos.Modelo;

/**
 * Classe genérica para cálculos de métricas de avaliação da rede neural.
 * <p>
 *    Novas métricas devem implementar o método {@code calcular()}.
 * </p>
 */
abstract class Metrica{

   /**
    * Calcula a métrica de avaliação configurada.
    * @param rede rede neural.
    * @param entrada dados de entrada.
    * @param saida dados de saída relativos a entrada.
    * @return valor de avaliação de acordo com a métrica configurada
    */
   public double calcular(Modelo rede, Object[] entrada, Object[] saida){
      throw new UnsupportedOperationException(
         "É necessário implementar a métrica de avaliação da rede."
      );
   }


   /**
    * Calcula a métrica de avaliação configurada.
    * @param rede rede neural.
    * @param entrada dados de entrada.
    * @param saida dados de saída relativos a entrada.
    * @return valor de avaliação de acordo com a métrica configurada
    */
   public int[][] calcularMatriz(Modelo rede, double[][] entrada, double[][] saida){
      throw new UnsupportedOperationException(
         "É necessário implementar a métrica de avaliação da rede."
      );
   }


   /**
    * <p>
    *    Auxiliar.
    * </p>
    * Encontra o índice com o maior valor contido no array fornecido
    * @param dados array contendo os dados
    * @return índice com o maior valor contido nos dados.
    */
   protected int indiceMaiorValor(double[] dados){
      int indiceMaiorValor = 0;
      double maiorValor = dados[0];
  
      for(int i = 1; i < dados.length; i++){
         if (dados[i] > maiorValor) {
            maiorValor = dados[i];
            indiceMaiorValor = i;
         }
      }
  
      return indiceMaiorValor;
   }

   /**
   /**
    * <p>
    *    Auxiliar.
    * </p>
    * Calcula a matriz de confusão.
    * @param rede
    * @param entradas
    * @param saidas
    * @return
    */
   protected int[][] matrizConfusao(Modelo rede, Object[] entradas, Object[] saidas){
      if(saidas instanceof double[][] == false){
         throw new IllegalArgumentException(
            "Objeto esperado para saída é double[][], recebido " + saidas.getClass().getTypeName()
         );
      }

      double[][] s = (double[][]) saidas;

      int nClasses = s[0].length;
      int[][] matriz = new int[nClasses][nClasses];
      double[] saidaRede = new double[rede.camadaSaida().tamanhoSaida()];

      for(int i = 0; i < entradas.length; i++){
         rede.calcularSaida(entradas[i]);
         saidaRede = rede.saidaParaArray();

         int real = this.indiceMaiorValor(s[i]);
         int previsto = this.indiceMaiorValor(saidaRede);

         matriz[real][previsto]++;
      }

      return matriz;
   } 
}
