package rna.ativacoes;

/**
 * Implementação da função de ativação ReLU para uso dentro 
 * dos modelos.
 * <p>
 *    A função ReLU (Rectified Linear Unit) retorna o próprio valor
 *    recebido caso ele seja maior que zero, e zero caso contrário.
 * </p>
 */
public class ReLU extends Ativacao{

   /**
    * Instancia a função de ativação ReLU.
    */
   public ReLU(){
      super.construir(
         (x) -> { return (x > 0) ? x : 0; },
         (x) -> { return (x > 0) ? 1 : 0; }
      );
   }
}
