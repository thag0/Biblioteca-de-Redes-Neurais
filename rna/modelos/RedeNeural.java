package rna.modelos;

import rna.ativacoes.Ativacao;
import rna.avaliacao.perda.ErroMedioQuadrado;
import rna.avaliacao.perda.Perda;
import rna.estrutura.Camada;
import rna.estrutura.Densa;
import rna.inicializadores.Aleatorio;
import rna.inicializadores.Inicializador;
import rna.otimizadores.Otimizador;
import rna.otimizadores.SGD;

/**
 * <h3>
 *    Modelo de Rede Neural {@code Multilayer Perceptron} criado do zero
 * </h3>
 *  Possui um conjunto de camadas densas sequenciais que propagam os dados de entrada.
 * <p>
 *    O modelo pode ser usado tanto para problemas de {@code regressão e classificação}, contando com 
 *    algoritmos de treino e otimizadores variados para ajudar na convergência e desempenho da rede para 
 *    problemas diversos.
 * </p>
 * <p>
 *    Possui opções de configuração para funções de ativação de camadas individuais, valor de alcance 
 *    máximo e mínimo na aleatorização dos pesos iniciais, inicializadores de pesos e otimizadores que 
 *    serão usados durante o treino. 
 * </p>
 * <p>
 *    Após configurar as propriedades da rede, o modelo precisará ser {@code compilado} para efetivamente 
 *    poder ser utilizado.
 * </p>
 * <p>
 *    As predições do modelo são feitas usando o método de {@code calcularSaida()} onde é especificada
 *    uma única amostra de dados ou uma seqûencia de amostras onde é retornado o resultado de predição
 *    da rede.
 * </p>
 * <p>
 *    Opções de avaliação e desempenho do modelo podem ser acessadas através do {@code avaliador} da
 *    Rede Neural, que contém implementação de funções de perda e métricas para o modelo.
 * </p>
 * @author Thiago Barroso, acadêmico de Engenharia da Computação pela Universidade Federal do Pará, 
 * Campus Tucuruí. Maio/2023.
 */
public class RedeNeural extends Modelo implements Cloneable{
   
   /**
    * Região crítica
    * <p>
    *    Conjunto de camadas densas (ou fully connected) da Rede Neural.
    * </p>
    */
   private Densa[] camadas;

   /**
    * Array contendo a arquitetura de cada camada dentro da Rede Neural.
    * <p>
    *    Cada elemento da arquitetura representa a quantidade de neurônios 
    *    presente na camada correspondente.
    * </p>
    * <p>
    *    A "camada de entrada" não é considerada camada, pois não é alocada na
    *    rede, ela serve apenas de parâmetro para o tamanho de entrada da primeira
    *    camada densa da Rede Neural.
    * </p>
    */
   private int[] arquitetura;

   /**
    * Constante auxiliar que ajuda no controle do bias dentro da rede.
    */
   private boolean bias = true;

   /**
    * Valor máximo e mínimo na hora de aleatorizar os pesos da rede neural, para
    * alguns inicializadores.
    */
   private double alcancePeso = 0.5;

   /**
    * <p>
    *    Cria uma instância de rede neural artificial. A arquitetura da rede será baseada de acordo 
    *    com cada posição do array, cada valor contido nele representará a quantidade de neurônios da 
    *    camada correspondente.
    * </p> 
    * <p>
    *   Nenhum dos elementos de arquitetura deve ser menor do que 1.
    * </p>
    * <p>
    *    Exemplo de uso:
    * </p>
    * <pre>
    * int[] arq = {
    *    1, //tamanho de entrada da rede
    *    2, //neurônios da primeira camada
    *    3  //neurônios da segunda camada
    * };
    * </pre>
    * <p>
    *    É obrigatório que a arquitetura tenha no mínimo dois elementos, um para a entrada e outro
    *    para a saída da Rede Neural.
    * </p>
    * <p>
    *    Após instanciar o modelo, é necessário compilar por meio da função {@code compilar()};
    * </p>
    * <p>
    *    Certifique-se de configurar as propriedades da rede por meio das funções de configuração fornecidas 
    *    para obter os melhores resultados na aplicação específica. Caso não seja usada nenhuma das funções de 
    *    configuração, a rede será compilada com os valores padrão.
    * </p>
    * @author Thiago Barroso, acadêmico de Engenharia da Computação pela Universidade Federal do Pará, 
    * Campus Tucuruí. Maio/2023.
    * @param arquitetura modelo de arquitetura específico da rede.
    * @throws IllegalArgumentException se o array de arquitetura for nulo.
    * @throws IllegalArgumentException se o array de arquitetura não possuir, pelo menos, dois elementos.
    * @throws IllegalArgumentException se os valores fornecidos forem menores que um.
    */
   public RedeNeural(int[] arquitetura){
      if(arquitetura == null){
         throw new IllegalArgumentException("A arquitetura fornecida não deve ser nula.");
      }
      if(arquitetura.length < 2){
         throw new IllegalArgumentException(
            "A arquitetura fornecida deve conter no mínimo dois elementos (entrada e saída), tamanho recebido = " + arquitetura.length
         );
      }
      for(int i = 0; i < arquitetura.length; i++){
         if(arquitetura[i] < 1){
            throw new IllegalArgumentException(
               "Os valores de arquitetura fornecidos não devem ser maiores que zero."
            );
         }
      }

      this.arquitetura = arquitetura;
      this.compilado = false;
   }

   /**
    * Define o valor máximo e mínimo na hora de aleatorizar os pesos iniciais da 
    * rede para a compilação, os novos valores não podem ser menores ou iguais a zero.
    * <p>
    *    É necessário informar o alcance <strong>antes</strong> de compilar a rede.
    * </p>
    * <p>
    *    {@code O valor padrão de alcance é 0.5}
    * </p>
    * @param alcance novo valor máximo e mínimo.
    * @throws IllegalArgumentException se o novo valor for menor ou igual a zero.
    */
   public void configurarAlcancePesos(double alcance){
      if(alcance <= 0){
         throw new IllegalArgumentException(
            "O novo valor de alcance dos pesos (" + alcance + 
            ") deve ser maior que zero."
         );
      }
      
      this.alcancePeso = alcance;
   }

   /**
    * Define se a Rede Neural usará um viés para seus pesos.
    * <p>
    *    O viés é um atributo adicional para cada neurônio que sempre emite um valor de 
    *    saída constante. A presença de viés permite que a rede neural aprenda relações 
    *    mais complexas, melhorando a capacidade de modelagem.
    * </p>
    * <p>
    *    O bias deve ser configurado antes da compilação para ser aplicado.
    * </p>
    * <p>
    *    {@code O valor padrão para uso do bias é true}
    * </p>
    * @param usarBias novo valor para o uso do bias.
    */
   public void configurarBias(boolean usarBias){
      this.bias = usarBias;
   }

   /**
    * Configura a função de ativação de todas as camadas da rede. É preciso
    * compilar o modelo previamente para poder configurar suas funções de ativação.
    * <p>
    *    Letras maiúsculas e minúsculas não serão diferenciadas.
    * </p>
    * <p>
    *    Segue a lista das funções de ativação disponíveis:
    * </p>
    * <ul>
    *    <li> ReLU. </li>
    *    <li> Sigmoid. </li>
    *    <li> TanH. </li>
    *    <li> Leaky ReLU. </li>
    *    <li> ELU .</li>
    *    <li> Swish. </li>
    *    <li> GELU. </li>
    *    <li> Linear. </li>
    *    <li> Seno. </li>
    *    <li> Argmax. </li>
    *    <li> Softmax. </li>
    *    <li> Softplus. </li>
    *    <li> ArcTan. </li>
    * </ul>
    * <p>
    *    {@code A função de ativação padrão é a ReLU para todas as camadas}
    * </p>
    * @param ativacao instância da função de ativação.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   public void configurarAtivacao(Ativacao ativacao){
      super.verificarCompilacao();
      
      for(Camada camada : this.camadas){
         camada.configurarAtivacao(ativacao);
      }
   }

   /**
    * Configura a função de ativação de todas as camadas da rede. É preciso
    * compilar o modelo previamente para poder configurar suas funções de ativação.
    * <p>
    *    Letras maiúsculas e minúsculas não serão diferenciadas.
    * </p>
    * <p>
    *    Segue a lista das funções de ativação disponíveis:
    * </p>
    * <ul>
    *    <li> ReLU. </li>
    *    <li> Sigmoid. </li>
    *    <li> TanH. </li>
    *    <li> Leaky ReLU. </li>
    *    <li> ELU .</li>
    *    <li> Swish. </li>
    *    <li> GELU. </li>
    *    <li> Linear. </li>
    *    <li> Seno. </li>
    *    <li> Argmax. </li>
    *    <li> Softmax. </li>
    *    <li> Softplus. </li>
    *    <li> ArcTan. </li>
    * </ul>
    * <p>
    *    {@code A função de ativação padrão é a ReLU para todas as camadas}
    * </p>
    * @param ativacao nome da função de ativação.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   public void configurarAtivacao(String ativacao){
      super.verificarCompilacao();
      
      for(Camada camada : this.camadas){
         camada.configurarAtivacao(ativacao);
      }
   }

   /**
    * Configura a função de ativação da camada correspondente. É preciso
    * compilar o modelo previamente para poder configurar suas funções de ativação.
    * <p>
    *    Letras maiúsculas e minúsculas não serão diferenciadas.
    * </p>
    * <p>
    *    Segue a lista das funções de ativação disponíveis:
    * </p>
    * <ul>
    *    <li> ReLU. </li>
    *    <li> Sigmoid. </li>
    *    <li> TanH. </li>
    *    <li> Leaky ReLU. </li>
    *    <li> ELU .</li>
    *    <li> Swish. </li>
    *    <li> GELU. </li>
    *    <li> Linear. </li>
    *    <li> Seno. </li>
    *    <li> Argmax. </li>
    *    <li> Softmax. </li>
    *    <li> Softplus. </li>
    *    <li> ArcTan. </li>
    * </ul>
    * <p>
    *    {@code A função de ativação padrão é a ReLU para todas as camadas}
    * </p>
    * @param camada camada que será configurada.
    * @param ativacao instância da função de ativação.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   public void configurarAtivacao(Densa camada, Ativacao ativacao){
      if(camada == null){
         throw new IllegalArgumentException(
            "A camada não pode ser nula."
         );
      }

      camada.configurarAtivacao(ativacao);
   }

   /**
    * Configura a função de ativação da camada correspondente. É preciso
    * compilar o modelo previamente para poder configurar suas funções de ativação.
    * <p>
    *    Letras maiúsculas e minúsculas não serão diferenciadas.
    * </p>
    * <p>
    *    Segue a lista das funções de ativação disponíveis:
    * </p>
    * <ul>
    *    <li> ReLU. </li>
    *    <li> Sigmoid. </li>
    *    <li> TanH. </li>
    *    <li> Leaky ReLU. </li>
    *    <li> ELU .</li>
    *    <li> Swish. </li>
    *    <li> GELU. </li>
    *    <li> Linear. </li>
    *    <li> Seno. </li>
    *    <li> Argmax. </li>
    *    <li> Softmax. </li>
    *    <li> Softplus. </li>
    *    <li> ArcTan. </li>
    * </ul>
    * <p>
    *    {@code A função de ativação padrão é a ReLU para todas as camadas}
    * </p>
    * @param camada camada que será configurada.
    * @param ativacao nome da função de ativação.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   public void configurarAtivacao(Densa camada, String ativacao){
      if(camada == null){
         throw new IllegalArgumentException(
            "A camada não pode ser nula."
         );
      }

      camada.configurarAtivacao(ativacao);
   }

   /**
    * Configura o novo otimizador da Rede Neural com base numa nova instância de otimizador.
    * <p>
    *    Configurando o otimizador passando diretamente uma nova instância permite configurar
    *    os hiperparâmetros do otimizador fora dos valores padrão, o que pode ajudar a
    *    melhorar o desempenho de aprendizado da Rede Neural em cenário específicos.
    * </p>
    * Otimizadores disponíveis.
    * <ol>
    *    <li>
    *       <strong> GradientDescent </strong>: Método clássico de retropropagação de erro e 
    *       ajuste de pesos para treinamento de Redes Neurais.
    *    </li>
    *    <li>
    *       <strong> SGD (Gradiente Descendente Estocástico) </strong>: Atualiza os pesos 
    *       usando o conjunto de treino embaralhado a cada época, com adicional de momentum
    *       e correção de nesterov para a atualização.
    *    </li>
    *    <li>
    *       <strong> AdaGrad </strong>: Um otimizador que adapta a taxa de aprendizado para 
    *       cada parâmetro da rede com base em iterações anteriores.
    *    </li>
    *    <li>
    *       <strong> RMSProp </strong>: Um otimizador que utiliza a média móvel dos quadrados 
    *       dos gradientes acumulados para ajustar a taxa de aprendizado.
    *    </li>
    *    <li>
    *       <strong> Adam </strong>: Um otimizador que combina o AdaGrad e o Momentum para 
    *       convergência rápida e estável.
    *    </li>
    *    <li>
    *       <strong> Nadam </strong>: Possui as mesmas vantagens de se utilizar o adam, com 
    *       o adicional do acelerador de Nesterov na atualização dos pesos.
    *    </li>
    *    <li>
    *       <strong> AMSGrad </strong>: Um otimizador que mantém um histórico dos valores
    *       dos gradientes acumulados para evitar a degradação da taxa de aprendizado,
    *       proporcionando uma convergência mais estável.
    *    </li>
    *    <li>
    *       <strong> Adamax </strong>: Um otimizador que é uma variação do Adam e
    *       mantém o máximo absoluto dos valores dos gradientes acumulados em vez de usar
    *       a média móvel dos quadrados dos gradientes.
    *    </li>
    *    <li>
    *       <strong> Lion </strong>: Esse é particularmente novo e não conheço muito bem. 
    *    </li>
    *    <li>
    *       <strong> Adadelta </strong>: Também é novo pra mim e ainda to testando melhor. 
    *    </li>
    * </ol>
    * <p>
    *    {@code O otimizador padrão é o SGD}
    * </p>
    * @param otimizador novo otimizador.
    * @throws IllegalArgumentException se o novo otimizador for nulo.
    */
   public void configurarOtimizador(Otimizador otimizador){
      super.configurarOtimizador(otimizador);
   }

   /**
    * Compila o modelo de Rede Neural inicializando as camadas, neurônios e pesos respectivos, 
    * baseado nos valores fornecidos.
    * <p>
    *    Caso nenhuma configuração inicial seja feita, a rede será inicializada com os argumentos padrão. 
    * </p>
    * Após a compilação o modelo está pronto para ser usado, mas deverá ser treinado.
    * <p>
    *    Para treinar o modelo deve-se fazer uso da função função {@code treinar()} informando os 
    *    dados necessários para a rede.
    * </p>
    * <p>
    *    Para usar as predições da rede basta usar a função {@code calcularSaida()} informando os
    *    dados necessários. Após a predição pode-se obter o resultado da rede por meio da função 
    *    {@code obterSaidas()};
    * </p>
    * Os valores de função de perda, otimizador e inicializador serão definidos como os padrões 
    * {@code ErroMedioQuadrado (MSE)}, {@code SGD} e {@code Aleatorio}. 
    * <p>
    *    Valores de perda e otimizador configurados previamente são mantidos.
    * </p>
    */
   public void compilar(){
      //usando valores de configuração prévia, se forem criados.
      Otimizador o = (this.otimizador == null) ? new SGD() : this.otimizador;
      Perda p = (this.perda == null) ? new ErroMedioQuadrado() : this.perda;
      this.compilar(o, p, new Aleatorio());
   }

   /**
    * Compila o modelo de Rede Neural inicializando as camadas, neurônios e pesos respectivos, 
    * baseado nos valores fornecidos.
    * <p>
    *    Caso nenhuma configuração inicial seja feita, a rede será inicializada com os argumentos padrão. 
    * </p>
    * Após a compilação o modelo está pronto para ser usado, mas deverá ser treinado.
    * <p>
    *    Para treinar o modelo deve-se fazer uso da função função {@code treinar()} informando os 
    *    dados necessários para a rede.
    * </p>
    * <p>
    *    Para usar as predições da rede basta usar a função {@code calcularSaida()} informando os
    *    dados necessários. Após a predição pode-se obter o resultado da rede por meio da função 
    *    {@code obterSaidas()};
    * </p>
    * O valor do otimizador será definido como {@code SGD} o valor do inicializador 
    * será definido como o padrão {@code Aleatorio}.
    * <p>
    *    Valor de otimizador configurado previamente é mantido.
    * </p>
    * @param perda função de perda da Rede Neural usada durante o treinamento.
    * @throws IllegalArgumentException se a função de perda for nula.
    */
   public void compilar(Perda perda){
      if(perda == null){
         throw new IllegalArgumentException("A função de perda não pode ser nula.");
      }

      //usando valores de configuração prévia, se forem criados
      Otimizador o = (this.otimizador == null) ? new SGD() : this.otimizador;
      this.compilar(o, perda, new Aleatorio());
   }

   /**
    * Compila o modelo de Rede Neural inicializando as camadas, neurônios e pesos respectivos, 
    * baseado nos valores fornecidos.
    * <p>
    *    Caso nenhuma configuração inicial seja feita, a rede será inicializada com os argumentos padrão. 
    * </p>
    * Após a compilação o modelo está pronto para ser usado, mas deverá ser treinado.
    * <p>
    *    Para treinar o modelo deve-se fazer uso da função função {@code treinar()} informando os 
    *    dados necessários para a rede.
    * </p>
    * <p>
    *    Para usar as predições da rede basta usar a função {@code calcularSaida()} informando os
    *    dados necessários. Após a predição pode-se obter o resultado da rede por meio da função 
    *    {@code obterSaidas()};
    * </p>
    * A função de perda será definida como {@code ErroMedioQuadrado (MSE)} O valor do inicializador 
    * será definido como o padrão {@code Aleatorio}.
    * <p>
    *    Valor de função de perda configurada previamente é mantido.
    * </p>
    * @param otimizador otimizador que será usado durante o treino da Rede Neural.
    * @throws IllegalArgumentException se o otimizador for nulo.
    */
   public void compilar(Otimizador otimizador){
      if(otimizador == null){
         throw new IllegalArgumentException("O otimizador fornecido não pode ser nulo.");
      }

      //usando valores de configuração prévia, se forem criados
      Perda p = (this.perda == null) ? new ErroMedioQuadrado() : this.perda;
      this.compilar(otimizador, p, new Aleatorio());
   }

   /**
    * Compila o modelo de Rede Neural inicializando as camadas, neurônios e pesos respectivos, 
    * baseado nos valores fornecidos.
    * <p>
    *    Caso nenhuma configuração inicial seja feita, a rede será inicializada com os argumentos padrão. 
    * </p>
    * Após a compilação o modelo está pronto para ser usado, mas deverá ser treinado.
    * <p>
    *    Para treinar o modelo deve-se fazer uso da função função {@code treinar()} informando os 
    *    dados necessários para a rede.
    * </p>
    * <p>
    *    Para usar as predições da rede basta usar a função {@code calcularSaida()} informando os
    *    dados necessários. Após a predição pode-se obter o resultado da rede por meio da função 
    *    {@code obterSaidas()};
    * </p>
    * A função de perda usada será a {@code ErroMedioQuadrado (MSE)}.
    * <p>
    *    Valor de função de perda configurada previamente é mantido.
    * </p>
    * @param otimizador otimizador que será usando para o treino da Rede Neural.
    * @param iniPesos inicializador de pesos das camadas da Rede Neural.
    * @throws IllegalArgumentException se o otimizador ou inicializador forem nulos.
    */
   public void compilar(Otimizador otimizador, Inicializador iniPesos){
      if(otimizador == null){
         throw new IllegalArgumentException("O otimizador fornecido não pode ser nulo.");
      }
      if(iniPesos == null){
         throw new IllegalArgumentException("O inicializador fornecido não pode ser nulo.");
      }

      //usando valores de configuração prévia, se forem criados.
      if(this.perda == null){
         this.compilar(otimizador, new ErroMedioQuadrado(), iniPesos);

      }else{
         this.compilar(otimizador, this.perda, iniPesos);
      }   
   }

   @Override
   public void compilar(Otimizador otimizador, Perda perda, Inicializador iniPesos){
      this.compilar(otimizador, perda, iniPesos, null);
   }

   @Override
   public void compilar(Otimizador otimizador, Perda perda, Inicializador iniPesos, Inicializador iniBias){
      if(iniPesos == null){
         throw new IllegalArgumentException("O inicializador não pode ser nulo.");
      }
      if(perda == null){
         throw new IllegalArgumentException("A função de perda não pode ser nula.");
      }
      if(otimizador == null){
         throw new IllegalArgumentException("O otimizador não pode ser nulo.");
      }

      if(seedInicial != 0){
         iniPesos.configurarSeed(seedInicial);
         if(iniBias != null){
            iniBias.configurarSeed(seedInicial);
         } 
         this.treinador.configurarSeed(seedInicial);
      }

      this.camadas = new Densa[this.arquitetura.length-1];
      this.camadas[0] = new Densa(this.arquitetura[0], this.arquitetura[1], this.bias);

      for(int i = 1; i < this.camadas.length; i++){
         this.camadas[i] = new Densa(
            this.camadas[i-1].formatoSaida()[1], this.arquitetura[i+1], this.bias
         );
         this.camadas[i].configurarId(i);
      }

      for(Camada camada : this.camadas){
         camada.inicializar(iniPesos, iniBias, alcancePeso);
      }

      this.perda = perda;

      this.otimizador = otimizador;
      this.otimizador.construir(this.camadas);

      this.compilado = true;
   }

   /**
    * Alimenta os dados pela rede neural usando o método de feedforward através do conjunto
    * de dados fornecido. 
    * <p>
    *    Os dados são alimentados para as entradas dos neurônios e é calculado o produto junto 
    *    com os pesos. No final é aplicado a função de ativação da camada no neurônio e o resultado 
    *    fica armazenado na saída dele.
    * </p>
    * @param entrada dados usados para alimentar a camada de entrada.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    * @throws IllegalArgumentException se o tamanho dos dados de entrada for diferente da capacidade
    * de entrada da rede.
    */
   @Override
   public void calcularSaida(Object entrada){
      super.verificarCompilacao();
      if(entrada instanceof double[] == false){
         throw new IllegalArgumentException(
            "A entrada para o modelo RedeNeural deve ser um array do tipo double[]."
         );
      }
      double[] e = (double[]) entrada;
      if(e.length != this.obterTamanhoEntrada()){
         throw new IllegalArgumentException(
            "Dimensões dos dados de entrada (" + e.length + 
            ") com a camada de entrada (" + this.obterTamanhoEntrada() + 
            ") incompatíveis."
         );
      }

      this.camadas[0].calcularSaida(e);
      for(int i = 1; i < this.camadas.length; i++){
         this.camadas[i].calcularSaida(this.camadas[i-1].saida());
      }
   }

   /**
    * Alimenta os dados pela rede neural usando o método de feedforward através do conjunto
    * de dados fornecido. 
    * <p>
    *    Os dados são alimentados para as entradas dos neurônios e é calculado o produto junto 
    *    com os pesos. No final é aplicado a função de ativação da camada no neurônio e o resultado 
    *    fica armazenado na saída dele.
    * </p>
    * @param entradas dados usados para alimentar a camada de entrada.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    * @throws IllegalArgumentException se a quantidade de amostras em cada linha dos dados for diferente.
    * @throws IllegalArgumentException se o tamanho dos dados de entrada for diferente da capacidade
    * de entrada da rede.
    * @return matriz contendo os resultados das predições da rede.
    */
   @Override
   public double[][] calcularSaidas(Object[] entradas){
      super.verificarCompilacao();
      if(entradas instanceof double[][] == false){
         throw new IllegalArgumentException(
            "As entradas para o modelo RedeNeural devem ser uma matriz do tipo double[][]."
         );
      }        
      
      double[][] e = (double[][]) entradas;
      int cols = e[0].length;
      for(int i = 1; i < entradas.length; i++){
         if(e[i].length != cols){
            throw new IllegalArgumentException(
               "As dimensões dos dados de entrada possuem tamanhos diferentes."
            );
         }
      }

      int nEntrada = this.obterTamanhoEntrada();
      if(e[0].length != nEntrada){
         throw new IllegalArgumentException(
            "Dimensões dos dados de entrada (" + entradas.length +
            ") e capacidade de entrada da rede (" + nEntrada + 
            ") incompatíveis."
         );
      }

      //dimensões dos dados
      int nAmostras = entradas.length;
      int tamEntrada = this.obterTamanhoEntrada();
      int tamSaida = this.obterTamanhoSaida();
      double[][] previsoes = new double[nAmostras][tamSaida];
      double[] entradaRede = new double[tamEntrada];
      double[] saidaRede = new double[tamSaida];

      for(int i = 0; i < nAmostras; i++){
         System.arraycopy(entradas[i], 0, entradaRede, 0, e[i].length);
         this.calcularSaida(entradaRede);
         System.arraycopy(this.saidaParaArray(), 0, saidaRede, 0, saidaRede.length);
         System.arraycopy(saidaRede, 0, previsoes[i], 0, saidaRede.length);
      }

      return previsoes;
   }

   /**
    * Treina a Rede Neural de acordo com as configurações predefinidas.
    * <p>
    *    Certifique-se de configurar adequadamente o modelo para obter os 
    *    melhores resultados.
    * </p>
    * @param entradas dados de entrada do treino (features).
    * @param saidas dados de saída correspondente a entrada (class).
    * @param epochs quantidade de épocas de treinamento.
    * @param logs logs para perda durante as épocas de treinamento.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    * @throws IllegalArgumentException se houver alguma inconsistência dos dados de entrada e saída para a operação.
    * @throws IllegalArgumentException se o valor de épocas for menor que um.
    */
   @Override
   public void treinar(Object[] entradas, Object[] saidas, int epochs, boolean logs){
      super.verificarCompilacao();

      if(epochs < 1){
         throw new IllegalArgumentException(
            "O valor de epochs (" + epochs + ") não pode ser menor que um"
         );
      }

      this.treinador.treino(
         this,
         entradas,
         saidas,
         epochs,
         logs
      );
   }

   /**
    * Treina a rede de acordo com as configurações predefinidas.
    * <p>
    *    O modo de treinamento em lote costuma ser mais lento para convergir
    *    e não é recomendável utilizar as mesma configurações da rede que foram
    *    usadas pelo método sequencial convencional.
    * </p>
    * <p>
    *    Em compensação ele tende a ser mais estável e possui menos ruídos durante
    *    o treinamento.
    * </p>
    * Certifique-se de configurar adequadamente o modelo para obter os 
    * melhores resultados.
    * @param entradas dados de entrada do treino (features).
    * @param saidas dados de saída correspondente a entrada (class).
    * @param epochs quantidade de épocas de treinamento.
    * @param tamLote tamanho que o lote vai assumir durante o treino.
    * @param logs logs para perda durante as épocas de treinamento.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    * @throws IllegalArgumentException se houver alguma inconsistência dos dados de entrada e saída para a operação.
    * @throws IllegalArgumentException se o valor de épocas for menor que um.
    */
   @Override
   public void treinar(Object[] entradas, Object[] saidas, int epochs, int tamLote, boolean logs){
      super.verificarCompilacao();

      if(epochs < 1){
         throw new IllegalArgumentException(
            "O valor de epochs (" + epochs + ") não pode ser menor que um"
         );
      }
      if(tamLote <= 0 || tamLote > entradas.length){
         throw new IllegalArgumentException(
            "O valor de tamanho do lote (" + tamLote + ") é inválido."
         );
      }

      this.treinador.treino(
         this,
         entradas,
         saidas,
         epochs,
         tamLote,
         logs
      );
   }

   /**
    * Método alternativo no treino da rede neural usando diferenciação finita (finite difference), 
    * que calcula a "derivada" da função de custo levando a rede ao mínimo local dela. É importante 
    * encontrar um bom balanço entre a taxa de aprendizagem da rede e o valor de perturbação usado.
    * <p>
    *    Vale ressaltar que esse método é mais lento e menos eficiente que o backpropagation, em 
    *    arquiteturas de rede maiores e que tenha uma grande volume de dados de treino ou para 
    *    problemas mais complexos ele pode demorar muito para convergir ou simplemente não funcionar 
    *    como esperado.
    * </p>
    * <p>
    *    Ainda sim não deixa de ser uma abordagem válida.
    * </p>
    * @param entradas matriz com os dados de entrada 
    * @param saidas matriz com os dados de saída
    * @param eps valor de perturbação
    * @param tA valor de taxa de aprendizagem do método, contribui para o quanto os pesos
    * serão atualizados. Valores altos podem convergir rápido mas geram instabilidade, valores pequenos
    * atrasam a convergência.
    * @param epochs número de épocas do treinamento
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    * @throws IllegalArgumentException se houver alguma inconsistência dos dados de entrada e saída para a operação.
    * @throws IllegalArgumentException se o valor de perturbação for igual a zero.
    * @throws IllegalArgumentException se o valor de épocas for menor que um.
    * @throws IllegalArgumentException se o valor de custo mínimo for menor que zero.
    */
   public void diferencaFinita(double[][] entradas, double[][] saidas, double eps, double tA, int epochs){      
      double salvo;
      for(int e = 0; e < epochs; e++){

         double custo = this.avaliador.erroMedioQuadrado(entradas, saidas);
         for(Densa camada : this.camadas){   
            for(int i = 0; i < camada.pesos.lin(); i++){
               for(int j = 0; j < camada.pesos.col(); j++){
                  salvo = camada.pesos.elemento(i, j);
                  camada.pesos.add(i, j, eps);
                  double d = (this.avaliador.erroMedioQuadrado(entradas, saidas) - custo) / eps;
                  camada.gradPesos.editar(i, j, d);
                  camada.pesos.editar(i, j, salvo);
               }
            }
            for(int i = 0; i < camada.bias.lin(); i++){
               for(int j = 0; j < camada.bias.col(); j++){
                  salvo = camada.bias.elemento(i, j);
                  camada.bias.add(i, j, eps);
                  double d = (this.avaliador.erroMedioQuadrado(entradas, saidas) - custo) / eps;
                  camada.gradSaida.editar(i, j, d);
                  camada.bias.editar(i, j, salvo);    
               }
            }
         }

         for(Densa camada : this.camadas){            
            for(int i = 0; i < camada.pesos.lin(); i++){
               for(int j = 0; j < camada.pesos.col(); j++){
                  camada.pesos.sub(i, j, (tA * camada.gradPesos.elemento(i, j)));
               }
            }

            if(camada.temBias()){
               for(int i = 0; i < camada.bias.lin(); i++){
                  for(int j = 0; j < camada.bias.col(); j++){
                     camada.bias.sub(i, j, (tA * camada.gradSaida.elemento(i, j)));
                  }
               }
            }
         }
      }

   }

   @Override
   public Otimizador otimizador(){
      return this.otimizador;
   }

   @Override
   public Perda perda(){
      return this.perda;
   }

   /**
    * Retorna a {@code camada} da Rede Neural correspondente ao índice fornecido.
    * @param id índice da busca.
    * @return camada baseada na busca.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    * @throws IllegalArgumentException se o índice estiver fora do alcance do tamanho 
    * das camadas ocultas.
    */
   @Override
   public Densa camada(int id){
      super.verificarCompilacao();

      if((id < 0) || (id >= this.camadas.length)){
         throw new IllegalArgumentException(
            "O índice fornecido (" + id + 
            ") é inválido ou fora de alcance."
         );
      }
   
      return this.camadas[id];
   }

   /**
    * Retorna todo o conjunto de camadas densas presente na Rede Neural.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    * @return conjunto de camadas da rede.
    */
   @Override
   public Densa[] camadas(){
      super.verificarCompilacao();
      return this.camadas;
   }

   /**
    * Retorna a {@code camada de saída} da Rede Neural.
    * @return camada de saída, ou ultima camada densa.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   @Override
   public Densa camadaSaida(){
      super.verificarCompilacao();
      return this.camadas[this.camadas.length-1];
   }

   /**
    * Retorna os dados de saída da última camada da Rede Neural. 
    * <p>
    *    A ordem de cópia é crescente, do primeiro neurônio da saída ao último.
    * </p>
    * @return array com os dados das saídas da rede.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   @Override
   public double[] saidaParaArray(){
      super.verificarCompilacao();
      return this.camadaSaida().saidaParaArray();
   }

   @Override
   public void copiarDaSaida(double[] arr){
      double[] saida = this.saidaParaArray();
      if(saida.length != arr.length){
         throw new IllegalArgumentException(
            "Incompatibilidade de dimensões entre o array fornecido (" + arr.length + 
            ") e o array gerado pela saída da última camada (" + saida.length + ")."
         );
      }

      System.arraycopy(saida, 0, arr, 0, saida.length);
   }

   /**
    * Retorna o array que representa a estrutura da Rede Neural. Nele cada elemento 
    * indica uma camada da rede e cada valor contido nesse elemento indica a 
    * quantidade de neurônios daquela camada correspondente.
    * <p>
    *    Nessa estrutura de rede, a camada de entrada não é considerada uma camada,
    *    o que significa dizer também que ela não é uma instância de camada dentro
    *    da Rede Neural.
    * </p>
    * <p>
    *    A "camada de entrada" representa o tamanho de entrada da primeira camada densa
    *    da rede, ou seja, ela é apenas um parâmetro pro tamanho de entrada da primeira
    *    camada oculta. 
    * </p>
    * @return array com a arquitetura da rede.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   public int[] obterArquitetura(){
      super.verificarCompilacao();
      return this.arquitetura;
   }

   /**
    * Informa o nome configurado da Rede Neural.
    * @return nome específico da rede.
    */
   @Override
   public String nome(){
      return this.nome;
   }

   /**
    * Retorna a quantidade total de parâmetros da rede.
    * <p>
    *    isso inclui todos os pesos de todos os neurônios presentes 
    *    (incluindo o peso adicional do bias).
    * </p>
    * @return quantiade de parâmetros total da rede.
    */
   @Override
   public int numParametros(){
      int parametros = 0;
      for(Camada camada : this.camadas){
         parametros += camada.numParametros();
      }
      return parametros;
   }

   /**
    * Retorna a quantidade de camadas densas presente na Rede Neural.
    * <p>
    *    A {@code camada de entrada} não é considerada uma camada densa e é usada
    *    apenas para especificar o tamanho de entrada suportado pela rede.
    * </p>
    * @return quantidade de camadas da Rede Neural.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   @Override
   public int numCamadas(){
      super.verificarCompilacao();
      return this.camadas.length;
   }

   /**
    * Retorna a capacidade da camada de entrada da Rede Neural. Em outras palavras
    * diz quantos dados de entrada a rede suporta.
    * @return tamanho de entrada da Rede Neural.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   public int obterTamanhoEntrada(){
      super.verificarCompilacao();
      return this.arquitetura[0];
   }

   /**
    * Retorna a capacidade de saída da Rede Neural. Em outras palavras
    * diz quantos dados de saída a rede produz.
    * @return tamanho de saída da Rede Neural.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   public int obterTamanhoSaida(){
      super.verificarCompilacao();
      return this.arquitetura[this.arquitetura.length-1];
   }

   /**
    * Retorna o valor de uso do bias da Rede Neural.
    * @return valor de uso do bias da Rede Neural.
    */
   public boolean temBias(){
      return this.bias;
   }

   /**
    * Disponibiliza o histórico da função de perda da Rede Neural durante cada época
    * de treinamento.
    * <p>
    *    O histórico será o do ultimo processo de treinamento usado, seja ele sequencial ou em
    *    lotes. Sendo assim, por exemplo, caso o treino seja em sua maioria feito pelo modo sequencial
    *    mas logo depois é usado o treino em lotes, o histórico retornado será o do treinamento em lote.
    * </p>
    * @return lista contendo o histórico de perdas durante o treinamento da rede.
    * @throws IllegalArgumentException se não foi habilitado previamente o cálculo do 
    * histórico de custos.
    */
   @Override
   public double[] historico(){
      if(this.treinador.calcularHistorico){
         return this.treinador.obterHistorico();   
      
      }else{
         throw new UnsupportedOperationException(
            "O histórico de treino da rede deve ser configurado previamente."
         );
      }
   }

   /**
    * Exibe algumas informações importantes sobre a Rede Neural, como:
    * <ul>
    *    <li>
    *       Otimizador atual e suas informações específicas.
    *    </li>
    *    <li>
    *       Contém bias adicionado nas camadas.
    *    </li>
    *    <li>
    *       Função de ativação de todas as camadas.
    *    </li>
    *    <li>
    *       Arquitetura da rede.
    *    </li>
    * </ul>
    * @return buffer formatado contendo as informações.
    * @throws IllegalArgumentException se o modelo não foi compilado previamente.
    */
   @Override
   public void info(){
      super.verificarCompilacao();

      StringBuilder sb = new StringBuilder();
      String espacamento = "    ";
      System.out.println("\nInformações " + this.nome + " = [");

      //perda
      sb.append(espacamento + "Perda: " + this.perda.getClass().getSimpleName() + "\n\n");

      //otimizador
      sb.append(this.otimizador.info());

      //bias
      sb.append("\n" + espacamento + "Bias = " + this.bias);
      sb.append("\n\n");

      //ativações
      for(int i = 0; i < this.camadas.length; i++){
         sb.append(
            espacamento + "Ativação camada " + i + ": " + 
            this.camadas[i].obterAtivacao().getClass().getSimpleName() + "\n"
         );
      }

      //arquitetura
      sb.append("\n" + espacamento + "arquitetura = [(" + this.arquitetura[0] + ")");
      for(int i = 1; i < this.arquitetura.length; i++){
         sb.append(", " + this.arquitetura[i]);
      }
      sb.append("]");

      sb.append("\n]\n");

      System.out.println(sb.toString());
   }

   @Override
   public RedeNeural clonar(){
      try{
         RedeNeural clone = (RedeNeural) super.clone();

         clone.arquitetura = this.arquitetura.clone();
         clone.bias = this.bias;
         clone.otimizador = this.otimizador;
         clone.perda = this.perda;

         clone.camadas = new Densa[this.camadas.length];
         for(int i = 0; i < this.camadas.length; i++){
            clone.camadas[i] = this.camadas[i].clonar();
         }

         return clone;
      }catch(Exception e){
         throw new RuntimeException(e);
      }
   }
}
