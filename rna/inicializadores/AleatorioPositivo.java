package rna.inicializadores;

import rna.core.Tensor4D;

/**
 * Inicializador de valores aleatórios positivos para uso dentro da biblioteca.
 */
public class AleatorioPositivo extends Inicializador{

   private double max = 0;

   /**
    * Instancia um inicializador de valores aleatórios positivos
    * com seed aleatória.
    * @param max valor máximo de aleatorização.
    */
   public AleatorioPositivo(double max){
      if(max <= 0){
         throw new IllegalArgumentException(
            "O valor máximo deve ser maior que zero."
         );
      }

      this.max = max;
   }

   /**
    * Instancia um inicializador de valores aleatórios positivos.
    * @param max valor máximo de aleatorização.
    * @param seed seed usada pelo gerador de números aleatórios.
    */
   public AleatorioPositivo(double max, long seed){
      if(max <= 0){
         throw new IllegalArgumentException(
            "O valor máximo deve ser maior que zero."
         );
      }

      super.setSeed(seed);
      this.max = max;
   }

   /**
    * Instancia um inicializador de valores aleatórios positivos
    * com seed aleatória.
    */
   public AleatorioPositivo(){
      this(1.0);
   }

   /**
    * Instancia um inicializador de valores aleatórios positivos.
    * @param seed seed usada pelo gerador de números aleatórios.
    */
   public AleatorioPositivo(long seed){
      super(seed);
      this.max = 1;
   }

   @Override
   public void inicializar(Tensor4D tensor){
      tensor.map((x) -> {
         return super.random.nextDouble(0, max);
      });
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1){
      tensor.map3D(dim1, (x) -> {
         return super.random.nextDouble(0, max);
      });
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1, int dim2){
      tensor.map2D(dim1, dim2, (x) -> {
         return super.random.nextDouble(0, max);
      });
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1, int dim2, int dim3){
      tensor.map1D(dim1, dim2, dim3, (x) -> {
         return super.random.nextDouble(0, max);
      });
   }
}
