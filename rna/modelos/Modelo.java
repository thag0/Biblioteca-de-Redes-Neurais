package rna.modelos;

import rna.avaliacao.Avaliador;
import rna.avaliacao.perda.Perda;
import rna.estrutura.Camada;
import rna.inicializadores.Inicializador;
import rna.otimizadores.Otimizador;
import rna.treinamento.Treinador;

/**
 * <h3>
 *    Basa para crianção de modelos dentro da biblioteca.
 * </h3>
 * Contém a inteface para os métodos necessários que são usados
 * para implementação de modelos.
 */
public abstract class Modelo{

   /**
    * Nome da instância do modelo.
    */
   protected String nome = this.getClass().getSimpleName();

   /**
    * Auxiliar no controle da compilação do modelo, ajuda a evitar uso 
    * indevido caso ainda não tenha suas variáveis e dependências inicializadas 
    * previamente.
    */
   public boolean compilado;

   /**
    * Função de perda usada durante o processo de treinamento.
    */
   protected Perda perda;

    /**
     * Otimizador que será utilizado durante o processo de aprendizagem da
     * da Rede Neural.
     */
   protected Otimizador otimizador;

   /**
    * Ponto inicial para os geradores aleatórios.
    * <p>
    *    0 não é considerado uma seed e ela só é configurada quando esse valor é
    *    alterado.
    * </p>
    */
   protected long seedInicial = 0;

   /**
    * Gerenciador de treino do modelo. contém implementações dos 
    * algoritmos de treino para o ajuste de parâmetros treináveis.
    */
   protected Treinador treinador = new Treinador();

   /**
    * Auxiliar na verificação para o salvamento do histórico
    * de perda do modelo durante o treinamento.
    */
   protected boolean calcularHistorico = false;

   /**
    * Responsável pelo retorno de desempenho da Rede Neural.
    * Contém implementações de métodos tanto para cálculo de perdas
    * quanto de métricas.
    * <p>
    *    Cada instância de rede neural possui seu próprio avaliador.
    * </p>
    */
   public Avaliador avaliador = new Avaliador(this);
   
   /**
    * Inicializa um modelo vazio, sem implementações de métodos.
    * <p>
    *    Para modelos mais completos, use {@code RedeNeural} ou {@code Sequencial}.
    * </p>
    */
   protected Modelo(){}

   /**
    * <p>
    *    Altera o nome do modelo.
    * </p>
    * O nome é apenas estético e não influencia na performance ou na 
    * usabilidade do modelo.
    * <p>
    *    O nome padrão é o mesmo nome da classe.
    * </p>
    * @param nome novo nome da rede.
    * @throws IllegalArgumentException se o novo nome for uulo ou inválido.
    */
   public void configurarNome(String nome){
      if(nome == null){
         throw new IllegalArgumentException("O novo nome não pode ser nulo.");
      }
      if(nome.isBlank() || nome.isEmpty()){
         throw new IllegalArgumentException("O novo nome não pode estar vazio.");
      }

      this.nome = nome;
   }

   /**
    * Configura a nova seed inicial para os geradores de números aleatórios utilizados 
    * durante o processo de inicialização de parâmetros treináveis do modelo.
    * <p>
    *    Configurações personalizadas de seed permitem fazer testes com diferentes
    *    parâmetros, buscando encontrar um melhor ajuste para o modelo.
    * </p>
    * <p>
    *    A configuração de seed deve ser feita antes da compilação do modelo.
    * </p>
    * @param seed nova seed.
    */
   public void configurarSeed(long seed){
      this.seedInicial = seed;
   }

   /**
    * Define se durante o processo de treinamento, o modelo vai salvar dados relacionados a 
    * função de custo/perda de cada época.
    * <p>
    *    Calcular a perda é uma operação que pode ser computacionalmente cara dependendo do 
    *    tamanho da rede e do conjunto de dados, então deve ser bem avaliado querer habilitar 
    *    ou não esse recurso.
    * </p>
    * <p>
    *    {@code O valor padrão é false}
    * </p>
    * @param calcular se verdadeiro, o modelo armazenará o histórico de perda durante cada época.
    */
   public void configurarHistorico(boolean calcular){
      this.calcularHistorico = calcular;
      this.treinador.configurarHistoricoCusto(calcular);
   }

   /**
    * Configura a função de perda que será utilizada durante o processo
    * de treinamento do modelo.
    * @param perda nova função de perda.
    */
   public void configurarPerda(Perda perda){
      if(perda == null){
         throw new IllegalArgumentException("A função de perda não pode ser nula.");
      }

      this.perda = perda;
   }

   /**
    * Configura o novo otimizador do Modelo com base numa nova instância de otimizador.
    * <p>
    *    Configurando o otimizador passando diretamente uma nova instância permite configurar
    *    os hiperparâmetros do otimizador fora dos valores padrão, o que pode ajudar a
    *    melhorar o desempenho de aprendizado do modelo em cenários específicos.
    * </p>
    * Otimizadores disponíveis.
    * <ol>
    *    <li> GradientDescent  </li>
    *    <li> SGD (Gradiente Descendente Estocástico) </li>
    *    <li> AdaGrad </li>
    *    <li> RMSProp </li>
    *    <li> Adam  </li>
    *    <li> Nadam </li>
    *    <li> AMSGrad </li>
    *    <li> Adadelta </li>
    * </ol>
    * @param otimizador novo otimizador.
    */
   public void configurarOtimizador(Otimizador otimizador){
      if(otimizador == null){
         throw new IllegalArgumentException("O novo otimizador não pode ser nulo.");
      }
      this.otimizador = otimizador;
   }

   /**
    * Inicializa os parâmetros necessários para cada camada do modelo,
    * além de gerar os valores iniciais para os kernels e bias.
    * @param otimizador otimizador usando para ajustar os parâmetros treinavéis do modelo.
    * @param perda função de perda usada para o treinamento do modelo.
    * @param iniKernel inicializador para os kernels.
    */
   public abstract void compilar(Otimizador otimizador, Perda perda, Inicializador iniKernel);

   /**
    * Inicializa os parâmetros necessários para cada camada do modelo,
    * além de gerar os valores iniciais para os kernels e bias.
    * <p>
    *    Caso nenhuma configuração inicial seja feita, o modelo será inicializado com os argumentos padrões. 
    * </p>
    * <p>
    *    Para treinar o modelo deve-se fazer uso da função função {@code treinar()} informando os 
    *    dados necessários para a rede.
    * </p>
    * @param otimizador otimizador usando para ajustar os parâmetros treinavéis do modelo.
    * @param perda função de perda usada para o treinamento do modelo.
    * @param iniKernel inicializador para os kernels.
    * @param iniBias inicializador para os bias.
    */
   public abstract void compilar(Otimizador otimizador, Perda perda, Inicializador iniKernel, Inicializador iniBias);

   /**
    * Auxiliar na verificação da compilação do modelo.
    */
   protected void verificarCompilacao(){
      if(this.compilado == false){
         throw new IllegalArgumentException("O modelo ainda não foi compilado.");
      }
   }

   /**
    * Propaga os dados de entrada através das camadas do modelo.
    * @param entrada dados de entrada que serão processados, o tipo
    * de dado depende do tipo da camada inicial do modelo.
    */
   public abstract void calcularSaida(Object entrada);

   /**
    * Propaga os dados de entrada através das camadas do modelo.
    * @param entradas array contendo multiplas entradas.
    * @return array contendo as saídas correspondentes.
    */
   public abstract Object[] calcularSaidas(Object[] entradas);

   /**
    * Treina o modelo de acordo com as configurações predefinidas.
    * <p>
    *    Certifique-se de configurar adequadamente o modelo para obter os 
    *    melhores resultados.
    * </p>
    * @param entradas dados de entrada do treino (features). Dependendo da entrada
    * do modelo, pode assumir diferentes formatos, para camadas convolucionais é
    * {@code double[][][][]}, para camadas densas é {@code double[][]}.
    * @param saidas dados de saída correspondente a entrada (class).
    * @param epochs quantidade de épocas de treinamento.
    * @param logs logs para perda durante as épocas de treinamento.
    */
   public abstract void treinar(Object[] entradas, Object[] saidas, int epochs, boolean logs);
   
   /**
    * Treina o modelo de acordo com as configurações predefinidas.
    * <p>
    *    Certifique-se de configurar adequadamente o modelo para obter os 
    *    melhores resultados.
    * </p>
    * @param entradas dados de entrada do treino (features). Dependendo da entrada
    * do modelo, pode assumir diferentes formatos, para camadas convolucionais é
    * {@code double[][][][]}, para camadas densas é {@code double[][]}.
    * @param saidas dados de saída correspondente a entrada (class).
    * @param epochs quantidade de épocas de treinamento.
    * @param tamLote tamanho do lote de treinamento.
    * @param logs logs para perda durante as épocas de treinamento.
    */
   public abstract void treinar(Object[] entradas, Object[] saidas, int epochs, int tamLote, boolean logs);

   /**
    * Avalia o modelo calcular o valor de perda usando a função de perda
    * que foi configurada.
    * <p>
    *    É possível utilizar outras funções de perda mesmo que sejam diferentes
    *    da que o modelo usa, através de:
    * </p>
    * <pre>
    * modelo.avaliador
    * </pre>
    * @param entrada dados de entrada para avaliação.
    * @param saida dados de saída correspondente as entradas fornecidas.
    * @return valor de perda do modelo.
    */
   public double avaliar(Object[] entrada, Object[] saida){
      verificarCompilacao();
 
      if(entrada.length != saida.length){
         throw new IllegalArgumentException(
            "A quantidade de dados de entrada (" + entrada.length + ") " +
            "e saída (" + saida.length + ") " + "devem ser iguais."
         );
      }
 
      if(saida instanceof double[][] == false){
         throw new IllegalArgumentException(
            "A saída deve ser do tipo double[][]" + 
            " recebido " + saida.getClass().getTypeName()
         );
      }
 
      double[][] s = (double[][]) saida;
      int n = entrada.length;
      double perda = 0;
      for(int i = 0; i < n; i++){
         this.calcularSaida(entrada[i]);
         perda += this.perda.calcular(this.saidaParaArray(), s[i]);
      }

      return perda/n;
   }

   /**
    * Retorna o otimizador que está sendo usado para o treino do modelo.
    * @return otimizador atual do modelo.
    */
   public abstract Otimizador otimizador();

   /**
    * Retorna a função de perda configurada do modelo.
    * @return função de perda atual do modelo.
    */
   public abstract Perda perda();

   /**
    * Retorna a {@code camada} do Modelo correspondente ao índice fornecido.
    * @param id índice da busca.
    * @return camada baseada na busca.
    */
   public abstract Camada camada(int id);

   /**
    * Retorna todo o conjunto de camadas presente no modelo.
    * @return conjunto de camadas do modelo.
    */
   public abstract Camada[] camadas();

   /**
    * Retorna a {@code camada de saída}, ou última camada, do modelo.
    * @return camada de saída.
    */
   public abstract Camada camadaSaida();
   
   /**
    * Retorna um array contendo a saída serializada do modelo.
    * @return saída do modelo.
    */
   public abstract double[] saidaParaArray();

   /**
    * Copia os dados de saída da última camada do modelo para o array.
    * <p>
    *    Esse método considera que as camadas finais dos modelos são camadas densas.
    * </p>
    * @param arr array para cópia.
    */
   public abstract void copiarDaSaida(double[] arr);

   /**
    * Informa o nome configurado do modelo.
    * @return nome do modelo.
    */
   public String nome(){
      return this.nome;
   }

   /**
    * Retorna a quantidade total de parâmetros do modelo.
    * <p>
    *    isso inclui todos os kernels e bias (caso configurados).
    * </p>
    * @return quantiade de parâmetros total do modelo.
    */
   public abstract int numParametros();

   /**
    * Retorna a quantidade de camadas presente no modelo.
    * @return quantidade de camadas do modelo.
    */
   public abstract int numCamadas();

   /**
    * Disponibiliza o histórico da função de perda do modelo durante cada época
    * de treinamento.
    * <p>
    *    O histórico será o do ultimo processo de treinamento usado, seja ele sequencial ou em
    *    lotes. Sendo assim, por exemplo, caso o treino seja em sua maioria feito pelo modo sequencial
    *    mas logo depois é usado o treino em lotes, o histórico retornado será o do treinamento em lote.
    * </p>
    * @return array contendo o valor de perda durante cada época de treinamento do modelo.
    */
   public double[] historico(){
      if(this.calcularHistorico){
         return this.treinador.obterHistorico();
      
      }else{
         throw new UnsupportedOperationException(
            "O histórico de treino do modelo deve ser configurado previamente."
         );
      }
   }

   /**
    * Mostra as informações sobre o modelo.
    * @return buffer formatado contendo as informações do modelo.
    */
   public abstract void info();

   /**
    * Clona as características principais do modelo.
    * @return clone do modelo.
    */
   public abstract Modelo clonar();
}
