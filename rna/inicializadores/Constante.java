package rna.inicializadores;

import rna.core.Tensor4D;

/**
 * Inicializador de valor constante para uso dentro da biblioteca.
 */
public class Constante extends Inicializador{
   
   /**
    * Valor de preenchimento.
    */
   private double val = 0;

   /**
    * Instância um inicializador de valor constante.
    * @param val valor usado de constante na inicialização.
    */
   public Constante(double val){
      this.val = val;
   }

   /**
    * Instância um inicializador de valor constante.
    * <p>
    *    Por padrão o valor é zero.
    * </p>
    */
   public Constante(){
      this(0);
   }

   @Override
   public void inicializar(Tensor4D tensor){
      tensor.map((x) -> val);
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1){
      tensor.map3D(dim1, (x) -> val);
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1, int dim2){
      tensor.map2D(dim1, dim2, (x) -> val);
   }

   @Override
   public void inicializar(Tensor4D tensor, int dim1, int dim2, int dim3){
      tensor.map1D(dim1, dim2, dim3, (x) -> val);
   }
}
