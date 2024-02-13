package rna.inicializadores;

import rna.core.Mat;

/**
 * Inicializador Xavier para uso dentro da biblioteca.
 */
public class GlorotUniforme extends Inicializador{

   /**
    * Instância um inicializador Xavier para matrizes com seed
    * aleatória.
    */
   public GlorotUniforme(){}
   
   /**
    * Instância um inicializador Xavier para matrizes.
    * @param seed seed usada pelo gerador de números aleatórios.
    */
   public GlorotUniforme(long seed){
      super(seed);
   }

   /**
    * Aplica o algoritmo de inicialização Xavier na matriz fornecida.
    * @param m matriz que será inicializada.
    * @param x valor utilizado apenas por outros otimizadores.
    */
   @Override
   public void inicializar(Mat m){
      double limite = Math.sqrt(6.0 / (m.lin() + m.col()));
      m.map((x) -> (
         super.random.nextDouble() * (2 * limite) - limite 
      ));
   }
}