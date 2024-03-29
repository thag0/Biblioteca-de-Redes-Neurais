package rna.inicializadores;

import rna.core.Tensor4D;

/**
 * Inicializador Glorot normalizado para uso dentro da biblioteca.
 */
public class GlorotNormal extends Inicializador{

   /**
    * Instância um inicializador Glorot normalizado para matrizes 
    * com seed
    * aleatória.
    */
   public GlorotNormal(){}

   /**
    * Instância um inicializador Glorot normalizado para matrizes.
    * @param seed seed usada pelo gerador de números aleatórios.
    */
   public GlorotNormal(long seed){
      super(seed);
   }

   @Override
   public void inicializar(Tensor4D tensor){
      double desvio = Math.sqrt(2.0 / (tensor.dim3() + tensor.dim4()));

      tensor.map((x) -> {
         return super.random.nextGaussian() * desvio;
      });
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1){
      double desvio = Math.sqrt(2.0 / (tensor.dim3() + tensor.dim4()));

      tensor.map3D(dim1, (x) -> {
         return super.random.nextGaussian() * desvio;
      });
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1, int dim2){
      double desvio = Math.sqrt(2.0 / (tensor.dim3() + tensor.dim4()));

      tensor.map2D(dim1, dim2, (x) -> {
         return super.random.nextGaussian() * desvio;
      });
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1, int dim2, int dim3){
      double desvio = Math.sqrt(2.0 / tensor.dim4());

      tensor.map1D(dim1, dim2, dim3, (x) -> {
         return super.random.nextGaussian() * desvio;
      });
   }
}
