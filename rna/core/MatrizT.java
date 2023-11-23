package rna.core;

public class MatrizT{

   private int nThreads;

   public MatrizT(int nThreads){
      this.nThreads = nThreads;
   }

   public MatrizT(){
      this(1);
   }

   public void configurarThreads(int numThreads){
      if(numThreads < 1){
         throw new IllegalArgumentException(
            "O número de threads deve ser maior que zero."
         );
      }

      if(numThreads > Runtime.getRuntime().availableProcessors()){
         System.out.println("Aviso: alto número de threads pode causar intabilidade ou lentidão.");
      }

      this.nThreads = numThreads;
   }

   /**
    * Multiplicação matricial em paralelo seguindo a expressão:
    * <pre>
    * R = A * B
    * </pre>
    * @param a primeita matriz.
    * @param b segunda matriz.
    * @param r matriz contendo o resultado da multiplicação.
    */
   public synchronized void mult(double[][] a, double[][] b, double[][] r){
      int linA = a.length;
      int colA = a[0].length;
      int linB = b.length;
      int colB = b[0].length;
      int linR = r.length;
      int colR = r[0].length;

      if(colA != linB){
         throw new IllegalArgumentException("Dimensões de A e B incompatíveis");
      }
      if(linR != linA){
         throw new IllegalArgumentException(
            "As linhas de A (" + linA + 
            ") e R (" + linR + 
            ") devem ser iguais."
         );
      }
      if(colR != colB){
         throw new IllegalArgumentException(
            "As colunas de B (" + colB + 
            ") e R (" + colR + 
            ") devem ser iguais."
         );
      }

      int linPorThread = linA / nThreads;
      Thread[] threads = new Thread[nThreads];

      double[][] tempA = a.clone();
      double[][] tempB = b.clone();
      double[][] tempR = r.clone();

      for(int t = 0; t < nThreads; t++){
         final int id = t;

         threads[t] = new Thread(() -> {
            int inicio = id * linPorThread;
            int fim = (id == nThreads - 1) ? linA : (id + 1) * linPorThread;
            
            for(int i = inicio; i < fim; i++){
               for(int j = 0; j < colR; j++){
                  double res = 0;
                  for(int k = 0; k < colA; k++){
                     res += tempA[i][k] * tempB[k][j];
                  }
                  tempR[i][j] = res;
               }
            }
         });

         threads[t].start();
      }
   
      try{
         for(int i = 0; i < nThreads; i++){
            threads[i].join(1);
         }
      }catch(InterruptedException e){
         e.printStackTrace();
         System.exit(1);
      }

      for(int i = 0; i < r.length; i++){
         System.arraycopy(tempR[i], 0, r[i], 0, r[i].length);
      }
   }

   /**
    * Adiciona o conteúdo resultante da soma entre A e B na matriz R de acordo
    * com a expressão:
    * <pre>
    * R = A + B
    * </pre>
    * @param a primeita matriz.
    * @param b segunda matriz.
    * @param r matriz contendo o resultado da soma.
    */
   public void add(double[][] a, double[][] b, double[][] r){
      if(a.length != b.length){
         throw new IllegalArgumentException("Linhas de A e B são diferentes.");
      }
      if(a[0].length != b[0].length){
         throw new IllegalArgumentException("Colunas de A e B são diferentes.");
      }
      if(a.length != r.length){
         throw new IllegalArgumentException("Linhas de R são diferentes.");
      }
      if(a[0].length != r[0].length){
         throw new IllegalArgumentException("Colunas de R são diferentes.");
      }

      if(a.length == 1){
         Array.add(a[0], b[0], r[0]);

      }else{
         for(int i = 0; i < r.length; i++){
            for(int j = 0; j < r[0].length; j++){
               r[i][j] = a[i][j] + b[i][j];
            }
         }
      }

   }

}
