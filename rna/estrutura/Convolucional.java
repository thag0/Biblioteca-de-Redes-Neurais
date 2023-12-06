package rna.estrutura;

import rna.ativacoes.Ativacao;
import rna.ativacoes.ReLU;
import rna.core.Mat;
import rna.core.OpMatriz;
import rna.inicializadores.Constante;
import rna.inicializadores.Inicializador;
import rna.serializacao.DicionarioAtivacoes;

//TODO conseguir um jeito de editar os valores do kernel pelo otimizador

/**
 * Implementação em andamento da camada convolucional.
 */
public class Convolucional extends Camada implements Cloneable{

   /**
    * Operador matricial para a camada.
    */
   OpMatriz opmat = new OpMatriz();

   private int altEntrada;
   private int largEntrada;
   private int profEntrada;

   private int altFiltro;
   private int largFiltro;
   private int numFiltros;

   private int altSaida;
   private int largSaida;

   /**
    * Array de matrizes contendo os valores de entrada para a camada,
    * que serão usados para o processo de feedforward.
    * <p>
    *    O formato da entrada é dado por:
    * </p>
    * <pre>
    *entrada = [profundidade entrada]
    *entrada[n] = [alturaEntrada][larguraEntrada]
    * </pre>
    */
   public Mat[] entrada;

   /**
    * Array bidimensional de matrizes contendo os filtros (ou kernels)
    * da camada.
    * <p>
    *    O formato dos filtros é dado por:
    * </p>
    * <pre>
    *filtros = [numFiltro][profundidadeEntrada]
    *filtros[i][j] = [alturaFiltro][larguraFiltro]
    * </pre>
    */
   public Mat[][] filtros;

   /**
    * Array de matrizes contendo os bias (vieses) para cada valor de 
    * saída da camada.
    * <p>
    *    O formato do bias é dado por:
    * </p>
    * <pre>
    *bias = [numeroFiltros]
    *bias[n] = [alturaSaida][larguraSaida]
    * </pre>
    */
   public Mat[] bias;

   /**
    * Auxiliar na verificação de uso do bias.
    */
   private boolean usarBias;

   /**
    * Array de matrizes contendo valores de somatório para cada valor de 
    * saída da camada.
    * <p>
    *    O formato somatório é dado por:
    * </p>
    * <pre>
    *somatorio = [numeroFiltros]
    *somatorio[n] = [alturaSaida][larguraSaida]
    * </pre>
    */
   public Mat[] somatorio;
   
   /**
    * Array de matrizes contendo os valores de saídas da camada.
    * <p>
    *    O formato da saída é dado por:
    * </p>
    * <pre>
    *saida = [numeroFiltros]
    *saida[n] = [alturaSaida][larguraSaida]
    * </pre>
    */
   public Mat[] saida;

   public Mat[] derivada;
   public Mat[] gradEntrada;
   public Mat[] gradSaida;
   public Mat[][] gradFiltros;
   public Mat[] gradBias;

   /**
    * Função de ativação da camada.
    */
   Ativacao ativacao = new ReLU();

   /**
    * Instancia uma camada convolucional de acordo com os formatos fornecidos.
    * <p>
    *    A disposição do formato de entrada deve ser da seguinte forma:
    * </p>
    * <pre>
    *    formEntrada = (altura, largura, profundidade)
    * </pre>
    * Onde largura e altura devem corresponder as dimensões dos dados de entrada
    * que serão processados pela camada e a profundidade diz respeito a quantidade
    * de entradas que a camada deve processar.
    * <p>
    *    A disposição do formato do filtro deve ser da seguinte forma:
    * </p>
    * <pre>
    *    formFiltro = (altura, largura)
    * </pre>
    * Onde largura e altura correspondem as dimensões que os filtros devem assumir.
    * @param formEntrada formato de entrada da camada.
    * @param formFiltro formato dos filtros da camada.
    * @param numFiltros quantidade de filtros.
    * @param usarBias adicionar uso do bias para a camada.
    * @throws IllegalArgumentException se as dimensões fornecidas não correspondenrem
    * ao padrão desejado ou se o número de filtros for menor que 1.
    */
   public Convolucional(int[] formEntrada, int[] formFiltro, int numFiltros, boolean usarBias){
      if(formEntrada.length != 3){
         throw new IllegalArgumentException(
            "O formato de entrada deve conter 3 elementos (altura, largura, profundidade)" 
         );
      }
      for(int i = 0; i < formEntrada.length; i++){
         if(formEntrada[i] < 1){
            throw new IllegalArgumentException(
               "O formato de entrada deve conter valores maiores do que zero." 
            ); 
         }
      }
      if(formFiltro.length != 2){
         throw new IllegalArgumentException(
            "O formato do filtro deve conter 2 elementos (altura, largura)" 
         );
      }
      for(int i = 0; i < formFiltro.length; i++){
         if(formFiltro[i] < 1){
            throw new IllegalArgumentException(
               "O formato do filtro deve conter valores maiores do que zero." 
            ); 
         }
      }
      if(numFiltros < 1){
         throw new IllegalArgumentException(
            "A camada deve conter ao menos 1 filtro."
         );
      }

      //formato da entrada da camada
      this.altEntrada  = formEntrada[0];
      this.largEntrada = formEntrada[1];
      this.profEntrada = formEntrada[2];

      //formato dos filtros da camada
      this.altFiltro  = formFiltro[0];
      this.largFiltro = formFiltro[1];
      this.numFiltros = numFiltros;
      
      this.entrada = new Mat[profEntrada];
      this.gradEntrada = new Mat[profEntrada];
      for(int i = 0; i < this.profEntrada; i++){
         this.entrada[i] = new Mat(this.altEntrada, this.largEntrada);
         this.gradEntrada[i] = new Mat(this.altEntrada, this.largEntrada);
      }
      
      this.filtros = new Mat[numFiltros][this.profEntrada];
      this.gradFiltros = new Mat[numFiltros][this.profEntrada];
      this.somatorio = new Mat[numFiltros];
      this.saida = new Mat[numFiltros];
      this.gradSaida = new Mat[numFiltros];

      this.usarBias = usarBias;
      if(this.usarBias){
         this.bias = new Mat[numFiltros];
         this.gradBias = new Mat[numFiltros];
      }

      this.altSaida = this.altEntrada - this.altFiltro + 1;
      this.largSaida = this.largEntrada - this.largFiltro + 1;

      for(int i = 0; i < this.numFiltros; i++){
         for(int j = 0; j < this.profEntrada; j++){
            this.filtros[i][j] = new Mat(this.altFiltro, this.largFiltro);
            this.gradFiltros[i][j] = new Mat(this.altFiltro, this.largFiltro);
         }

         this.somatorio[i] = new Mat(this.altSaida, this.largSaida);
         this.saida[i] = new Mat(this.altSaida, this.largSaida);
         this.gradSaida[i] = new Mat(this.altSaida, this.largSaida);

         if(this.usarBias){
            this.bias[i] = new Mat(this.altSaida, this.largSaida);
            this.gradBias[i] = new Mat(this.altSaida, this.largSaida);
         }
      }

      this.treinavel = true;
   }

   /**
    * Instancia uma camada convolucional de acordo com os formatos fornecidos.
    * <p>
    *    A disposição do formato de entrada deve ser da seguinte forma:
    * </p>
    * <pre>
    *    formEntrada = (altura, largura, profundidade)
    * </pre>
    * Onde largura e altura devem corresponder as dimensões dos dados de entrada
    * que serão processados pela camada e a profundidade diz respeito a quantidade
    * de entradas que a camada deve processar.
    * <p>
    *    A disposição do formato do filtro deve ser da seguinte forma:
    * </p>
    * <pre>
    *    formFiltro = (altura, largura)
    * </pre>
    * Onde largura e altura correspondem as dimensões que os filtros devem assumir.
    * <p>
    *    O valor de uso do bias será usado como {@code true} por padrão.
    * <p>
    * @param formEntrada formato de entrada da camada.
    * @param formFiltro formato dos filtros da camada.
    * @param numFiltros quantidade de filtros.
    * @throws IllegalArgumentException se as dimensões fornecidas não correspondenrem
    * ao padrão desejado ou se o número de filtros for menor que 1.
    */
   public Convolucional(int[] formEntrada, int[] formFiltro, int numFiltros){
      this(formEntrada, formFiltro, numFiltros, true);
   }

   @Override
   public void inicializar(Inicializador iniKernel, Inicializador iniBias, double x){
      if(iniKernel == null){
         throw new IllegalArgumentException(
            "O inicializador não pode ser nulo."
         );
      }

      for(int i = 0; i < numFiltros; i++){
         for(int j = 0; j < profEntrada; j++){
            iniKernel.inicializar(this.filtros[i][j], x);
         }
      }
      
      if(this.usarBias){
         for(Mat b : this.bias){
            if(iniBias == null) new Constante().inicializar(b, 0);
            else iniBias.inicializar(b, x);
         }
      }
   }

   @Override
   public void inicializar(Inicializador iniKernel, double x){
      this.inicializar(iniKernel, null, x);
   }

   @Override
   public void configurarAtivacao(String ativacao){
      DicionarioAtivacoes dic = new DicionarioAtivacoes();
      this.ativacao = dic.obterAtivacao(ativacao);
   }

   @Override
   public void configurarAtivacao(Ativacao ativacao){
      if(ativacao == null){
         throw new IllegalArgumentException(
            "A função de ativação não pode ser nula."
         );
      }

      this.ativacao = ativacao;
   }

   /**
    * Propagação direta dos dados de entrada através da camada convolucional.
    * Realiza a correlação cruzada entre os filtros da camada e os dados de entrada,
    * somando os resultados ponderados. Caso a camada tenha configurado o uso do bias, ele
    * é adicionado após a operação. Por fim é aplicada a função de ativação aos resultados
    * que serão salvos da saída da camada.
    * <p>
    *    A expressão que define a saída para cada filtro é dada por:
    * </p>
    * <pre>
    *somatorio[i] = correlacaoCruzada(filtros[i][j], entrada[j]) + bias[i]
    *saida[i] = ativacao(somatorio[i])
    * </pre>
    * onde {@code i} é o índice do filtro e {@code j} é o índice dos dados de entrada.
    * <p>
    *    Após a propagação dos dados, a função de ativação da camada é aplicada
    *    ao resultado do somatório e o resultado é salvo da saída da camada.
    * </p>
    * @param entrada dados de entrada que serão processados, deve ser um array 
    * tridimensional do tipo {@code double[][][]}.
    * @throws IllegalArgumentException caso a entrada fornecida não seja suportada 
    * pela camada.
    * @throws IllegalArgumentException caso haja alguma incompatibilidade entre a entrada
    * fornecida e a capacidade de entrada da camada.
    */
   @Override
   public void calcularSaida(Object entrada){
      if(entrada instanceof double[][][] == false){
         throw new IllegalArgumentException(
            "Os dados de entrada para a camada Convolucional devem ser " +
            "do tipo \"double[][][]\", objeto recebido é do tipo \"" + 
            entrada.getClass().getSimpleName() + "\""
         );
      }

      double[][][] e = (double[][][]) entrada;
      if(e.length != this.profEntrada){
         throw new IllegalArgumentException(
            "A profundidade de entrada (" + e.length + 
            ") dos dados fornecidos é diferente da profundidade " + 
            " de entrada da camada (" + this.profEntrada + ")."
         );
      }
      if(e[0].length != this.largEntrada){
         throw new IllegalArgumentException(
            "A largura de entrada (" + e[0].length + 
            ") dos dados fornecidos é diferente da largura " + 
            " de entrada da camada (" + this.largEntrada + ")."
         );
      }
      if(e[0][0].length != this.altEntrada){
         throw new IllegalArgumentException(
            "A altura de entrada (" + e[0][0].length + 
            ") dos dados fornecidos é diferente da altura " + 
            " de entrada da camada (" + this.altEntrada + ")."
         );
      }

      for(int i = 0; i < this.profEntrada; i++){
         this.entrada[i].copiar(e[i]);
      }

      //feedforward
      for(int i = 0; i < this.numFiltros; i++){
         for(int j = 0; j < this.profEntrada; j++){
            opmat.correlacaoCruzada(this.entrada[j], this.filtros[i][j], this.somatorio[i]);
            opmat.add(this.somatorio[i], this.saida[i], this.saida[i]);
         }

         if(this.usarBias){
            opmat.add(this.saida[i], this.bias[i], this.saida[i]);
         }
      }

      //deixar os valores calculados no somatório pra saída 
      //ficar com o resultado das ativações
      for(int i = 0; i < this.saida.length; i++){
         this.somatorio[i].copiar(this.saida[i]);
      }

      this.ativacao.calcular(this);
   }

   /**
    * Calcula os gradientes da camada para os pesos e bias baseado nos
    * gradientes fornecidos.
    * <p>
    *    Após calculdos, os gradientes em relação a entrada da camada são
    *    calculados e salvos em {@code gradEntrada} para serem retropropagados 
    *    para as camadas anteriores da Rede Neural em que a camada estiver.
    * </p>
    * Resultados calculados ficam salvos nas prorpiedades {@code camada.gradFiltros} e
    * {@code camada.gradBias}.
    * @param gradSeguinte gradiente da camada seguinte.
    */
   @Override
   public void calcularGradiente(Object gradSeguinte){
      if(gradSeguinte instanceof double[][][] == false){
         throw new IllegalArgumentException(
            "Os gradientes para a camada Convolucional devem ser " +
            "do tipo \"double[][][]\", objeto recebido é do tipo \"" + 
            gradSeguinte.getClass().getSimpleName() + "\""
         );
      }

      //transformação do array de gradientes para o objeto matricial
      //usado pela biblioteca
      double[][][] g = (double[][][]) gradSeguinte;
      for(int i = 0; i < g.length; i++){
         this.gradSaida[i] = new Mat(g[i].length, g[i][0].length);
         this.gradSaida[i].copiar(g[i]);
      }

      //backward
      for(int i = 0; i < this.numFiltros; i++){
         for(int j = 0; j < this.profEntrada; j++){
            opmat.correlacaoCruzada(this.entrada[j], this.gradSaida[i], this.gradFiltros[i][j]);

            Mat r = new Mat(this.gradEntrada[j].lin, this.gradEntrada[j].col);
            opmat.convolucaoFull(gradSaida[i], this.filtros[i][j], r);
            opmat.add(this.gradEntrada[j], r, this.gradEntrada[j]);
         }

         if(this.usarBias){
            this.gradBias[i].copiar(gradSaida[i]);
         }
      }
   }

   /**
    * Retorna a quantidade de filtros presentes na camada.
    * @return quantiadde de filtros presentes na camada.
    */
   public int numFiltros(){
      return this.numFiltros;
   }

   /**
    * Retorna a instância da função de ativação configurada para a camada.
    * @return função de ativação da camada.
    */
   public Ativacao obterAtivacao(){
      return this.ativacao;
   }

   @Override
   public boolean temBias(){
      return this.usarBias;
   }

   @Override
   public int numParametros(){
      int parametros = 0;

      parametros += this.numFiltros * this.profEntrada * this.altFiltro * this.largFiltro;
      parametros += this.bias.length * this.altSaida * this.altSaida;

      return parametros;
   }

   /**
    * Retorna o array de saídas da camada.
    * @return array de matrizes contendo as saídas da camada.
    */
   public Mat[] saidaParaMat(){
      return this.saida;
   }

   @Override
   public double[] obterSaida(){
      int n = 0;
      for(int i = 0; i < this.saida.length; i++){
         n += this.saida[i].tamanho();
      }

      int id = 0;
      double[] saida = new double[n];
      for(int i = 0; i < this.saida.length; i++){
         double[] s = this.saida[i].paraArray();
         for(int j = 0; j < s.length; j++){
            saida[id] = s[j];
            id++;
         }
      }

      return saida;
   }

   /**
    * Retorna as saídas da camada no formato de um array trimensional.
    * @return saída da camada.
    */
   public double[][][] saidaParaDouble(){
      double[][][] saida = new double[this.numFiltros][][];
      for(int i = 0; i < saida.length; i++){
         saida[i] = this.saida[i].paraDouble();
      }

      return saida;
   }

   /**
    * Clona a instância da camada, criando um novo objeto com as 
    * mesmas características mas em outro espaço de memória.
    * @return clone da camada.
    */
    @Override
   public Convolucional clone(){
      try{
         Convolucional clone = (Convolucional) super.clone();

         clone.ativacao = this.ativacao;

         clone.usarBias = this.usarBias;
         if(this.usarBias){
            clone.bias = this.bias.clone();
            clone.gradBias = this.gradBias.clone();
         }

         clone.entrada = this.entrada.clone();
         clone.filtros = this.filtros.clone();
         clone.somatorio = this.somatorio.clone();
         clone.saida = this.saida.clone();
         clone.gradSaida = this.gradSaida.clone();
         clone.derivada = this.derivada.clone();
         clone.gradFiltros = this.gradFiltros.clone();

         return clone;
      }catch(Exception e){
         throw new RuntimeException(e);
      }
   }

   /**
    * Retorna um array contendo o formato da saída da camada que 
    * é disposto do seguinte formato:
    * <pre>
    *    formato = (altura, largura, profundidade)
    * </pre>
    * @return array contendo as dimensões de saída da camada.
    */
   public int[] obterFormatoSaida(){
      return new int[]{this.altSaida, this.largSaida, this.numFiltros};
   }

   /**
    * Calcula o formato de entrada da camada Densa, que é disposto da
    * seguinte forma:
    * <pre>
    *    formato = (entrada.altura, entrada.largura, entrada.profundidade)
    * </pre>
    * @return formato de entrada da camada.
    */
   @Override
   public int[] formatoEntrada(){
      return new int[]{
         this.entrada[0].lin, 
         this.entrada[0].col, 
         this.entrada.length
      };
   }
 
    /**
     * Calcula o formato de saída da camada Densa, que é disposto da
     * seguinte forma:
     * <pre>
     *    formato = (saida.altura, saida.largura, saida.profundidade)
     * </pre>
     * @return formato de saída da camada.
     */
    @Override
   public int[] formatoSaida(){
      return new int[]{
         this.saida[0].lin, 
         this.saida[0].col, 
         this.saida.length
      };
   }

   @Override
   public double[] obterKernel(){
      int n = 0;
      for(int i = 0; i < this.filtros.length; i++){
         for(int j = 0; j < this.filtros[i].length; j++){
            n += this.filtros[i][j].tamanho();
         }
      }

      double[] kernel = new double[n];
      int cont = 0;
      for(int i = 0; i < this.filtros.length; i++){
         for(int j = 0; j < this.filtros[i].length; j++){
            double[] arr = this.filtros[i][j].paraArray();
            for(int k = 0; k < arr.length; k++){
               kernel[cont] = arr[k];
               cont++;
            }
         }
      }

      return kernel;
   }

   @Override
   public double[] obterGradKernel(){
      int n = 0;
      for(int i = 0; i < this.gradFiltros.length; i++){
         for(int j = 0; j < this.gradFiltros[i].length; j++){
            n += this.gradFiltros[i][j].tamanho();
         }
      }

      double[] grad = new double[n];
      int cont = 0;
      for(int i = 0; i < this.gradFiltros.length; i++){
         for(int j = 0; j < this.gradFiltros[i].length; j++){
            double[] arr = this.gradFiltros[i][j].paraArray();
            for(int k = 0; k < arr.length; k++){
               grad[cont] = arr[k];
               cont++;
            }
         }
      }

      return grad;
   }

   @Override
   public double[] obterBias(){
      int n = 0;
      for(int i = 0; i < this.bias.length; i++){
         n += this.bias[i].tamanho();
      }

      double[] bias = new double[n];
      int cont = 0;
      for(int i = 0; i < this.bias.length; i++){
         double[] arr = this.bias[i].paraArray();
         for(int j = 0; j < arr.length; j++){
            bias[cont] = arr[j];
            cont++;
         }
      }

      return bias;
   }

   @Override
   public double[] obterGradBias(){
      int n = 0;
      for(int i = 0; i < this.gradBias.length; i++){
         n += this.gradBias[i].tamanho();
      }

      double[] grad = new double[n];
      int cont = 0;
      for(int i = 0; i < this.gradBias.length; i++){
         double[] arr = this.gradBias[i].paraArray();
         for(int j = 0; j < arr.length; j++){
            grad[cont] = arr[j];
            cont++;
         }
      }

      return grad;
   }

   @Override
   public void editarKernel(double[] kernel){
      if(kernel.length != (this.altFiltro * this.largFiltro * this.numFiltros)){
         throw new IllegalArgumentException(
            "A dimensão do kernel fornecido não é igual a quantidade de " +
            " parâmetros para os kernels da camada."
         );
      }
         
      int id = 0;
      for(Mat[] filtro : this.filtros){
         for(Mat camada : filtro){
            for(int i = 0; i < camada.lin; i++){
               for(int j = 0; j < camada.col; j++){
                  camada.editar(i, j, kernel[id++]);
               }
            }
         }
      }
   }

   @Override
   public void editarBias(double[] bias){
      if(bias.length != (this.altSaida * this.largSaida * this.numFiltros)){
         throw new IllegalArgumentException(
            "A dimensão do bias fornecido não é igual a quantidade de " +
            " parâmetros para os bias da camada."
         );
      }
      
      int id = 0;
      for(Mat camada : this.bias){
         for(int i = 0; i < camada.lin; i++){
            for(int j = 0; j < camada.col; j++){
               camada.editar(i, j, bias[id++]);
            }
         }
      }
   }
}