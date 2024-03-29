package rna.otimizadores;

import rna.camadas.Camada;
import rna.core.OpArray;

/**
 * <h2>
 *    Gradient Descent
 * </h2>
 * Classe que implementa o algoritmo de Descida do Gradiente para otimização de redes neurais.
 * Atualiza diretamente os pesos da rede com base no gradiente.
 * <p>
 *    O Gradiente descendente funciona usando a seguinte expressão:
 * </p>
 * <pre>
 *    v -= g * tA
 * </pre>
 * Onde:
 * <p>
 *    {@code v} - variável que será otimizadada.
 * </p>
 *    {@code g} - gradiente correspondente a variável que será otimizada.
 * </p>
 * <p>
 *    {@code tA} - taxa de aprendizagem do otimizador.
 * </p>
 */
public class GD extends Otimizador{

   /**
    * Operador de arrays.
    */
   OpArray opArr = new OpArray();

   /**
    * Valor de taxa de aprendizagem do otimizador.
    */
   private double taxaAprendizagem;

   /**
    * Inicializa uma nova instância de otimizador da <strong> Descida do Gradiente </strong>
    * usando os valores de hiperparâmetros fornecidos.
    * @param tA taxa de aprendizagem do otimizador.
    */
   public GD(double tA){
      if(tA <= 0){
         throw new IllegalArgumentException(
            "\nTaxa de aprendizagem (" + tA + "), inválida."
         );
      }

      this.taxaAprendizagem = tA;
   }

   /**
    * Inicializa uma nova instância de otimizador da <strong> Descida do Gradiente </strong>.
    * <p>
    *    Os hiperparâmetros do GD serão inicializados com os valores padrão.
    * </p>
    */
   public GD(){
      this(0.1);
   }

   @Override
   public void construir(Camada[] camadas){
      //esse otimizador não precisa de parâmetros adicionais
      this.construido = true;//otimizador pode ser usado
   }

   @Override
   public void atualizar(Camada[] camadas){
      verificarConstrucao();
      
      for(Camada camada : camadas){
         if(camada.treinavel == false) continue;

         double[] kernel = camada.kernelParaArray();
         double[] gradK = camada.gradKernelParaArray();
         
         opArr.multEscalar(gradK, taxaAprendizagem, gradK);
         opArr.sub(kernel, gradK, kernel);
         camada.editarKernel(kernel);

         if(camada.temBias()){
            double[] bias = camada.biasParaArray();
            double[] gradB = camada.gradBias();
            
            opArr.multEscalar(gradB, taxaAprendizagem, gradB);
            opArr.sub(bias, gradB, bias);
            camada.editarBias(bias);
         }
      } 
   }

   @Override
   public String info(){
      super.verificarConstrucao();
      super.construirInfo();
      
      super.addInfo("TaxaAprendizagem: " + this.taxaAprendizagem);

      return super.info();
   }
   
}
