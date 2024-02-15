import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import lib.ged.Dados;
import lib.ged.Ged;
import lib.geim.Geim;
import rna.camadas.*;
import rna.modelos.Modelo;
import rna.modelos.Sequencial;
import rna.otimizadores.SGD;
import rna.serializacao.Serializador;

public class MainConv{
   static Ged ged = new Ged();
   static Geim geim = new Geim();

   static final int NUM_DIGITOS_TREINO = 10;
   static final int NUM_DIGITOS_TESTE  = 10;
   static final int NUM_AMOSTRAS_TREINO = 200;
   static final int NUM_AMOSTRAS_TESTE  = 100;

   static final String caminhoTreino = "/dados/mnist/treino/";
   static final String caminhoTeste = "/dados/mnist/teste/";
   static final String caminhoSaidaModelo = "./dados/modelosMNIST/modelo-convolucional.txt";
   static final String caminhoHistorico = "historico-perda";

   public static void main(String[] args){
      ged.limparConsole();
      
      final var treinoX = carregarDadosMNIST(caminhoTreino, NUM_AMOSTRAS_TREINO, NUM_DIGITOS_TREINO);
      final var treinoY = criarRotulosMNIST(NUM_AMOSTRAS_TREINO, NUM_DIGITOS_TREINO);
      System.out.println(
         "Dados de treino (" + 
         treinoX.length + ", " + treinoX[0].length + ", " + treinoX[0][0].length + ", " + treinoX[0][0][0].length + 
         ")\n"
      );

      Sequencial modelo = criarModelo();
      modelo.configurarHistorico(true);
      modelo.info();

      // treinar e marcar tempo
      long t1, t2;
      long horas, minutos, segundos;

      System.out.println("Treinando.");
      t1 = System.nanoTime();
      modelo.treinar(treinoX, treinoY, 20, true);
      t2 = System.nanoTime();

      long tempoDecorrido = t2 - t1;
      long segundosTotais = TimeUnit.NANOSECONDS.toSeconds(tempoDecorrido);
      horas = segundosTotais / 3600;
      minutos = (segundosTotais % 3600) / 60;
      segundos = segundosTotais % 60;

      System.out.println();
      System.out.println("Tempo de treinamento: " + horas + "h " + minutos + "m " + segundos + "s");
      System.out.println(
         "Treino -> perda: " + modelo.avaliar(treinoX, treinoY) + 
         " - acurácia: " + (modelo.avaliador().acuracia(treinoX, treinoY) * 100) + "%"
      );

      System.out.println("\nCarregando dados de teste.");
      final var testeX = carregarDadosMNIST(caminhoTeste, NUM_AMOSTRAS_TESTE, NUM_DIGITOS_TESTE);
      final var testeY = criarRotulosMNIST(NUM_AMOSTRAS_TESTE, NUM_DIGITOS_TESTE);
      System.out.println(
         "Teste -> perda: " + modelo.avaliar(testeX, testeY) + 
         " - acurácia: " + (modelo.avaliador().acuracia(testeX, testeY) * 100) + "%"
      );
      
      exportarHistorico(modelo, caminhoHistorico);
      salvarModelo(modelo, caminhoSaidaModelo);
      MainImg.executarComando("python grafico.py " + caminhoHistorico);
   }

   /*
    * Criação de modelos para testes.
    */
   static Sequencial criarModelo(){
      int[] formEntrada = {1, 28, 28};

      Sequencial modelo = new Sequencial(new Camada[]{
         new Convolucional(formEntrada, new int[]{5, 5}, 20, "leaky-relu"),
         new MaxPooling(new int[]{3, 3}),
         // new Convolucional(new int[]{3, 3}, 16, "leaky-relu"),
         // new MaxPooling(new int[]{2, 2}),
         new Flatten(),
         new Densa(100, "sigmoid"),
         new Dropout(0.3),
         new Densa(NUM_DIGITOS_TREINO, "softmax")
      });

      modelo.compilar(new SGD(0.01, 0.9), "entropia-cruzada");

      return modelo;
   }

   /**
    * 
    * @param modelo
    * @param caminho
    */
   static void salvarModelo(Sequencial modelo, String caminho){
      System.out.println("Salvando modelo.");
      new Serializador().salvar(modelo, caminho, "double");
   }

   /**
    * Converte uma imagem numa matriz contendo seus valores de brilho entre 0 e 1.
    * @param caminho caminho da imagem.
    * @return matriz contendo os valores de brilho da imagem.
    */
   static double[][] imagemParaMatriz(String caminho){
      BufferedImage img = geim.lerImagem(caminho);
      double[][] imagem = new double[img.getHeight()][img.getWidth()];

      int[][] cinza = geim.obterCinza(img);

      for(int y = 0; y < imagem.length; y++){
         for(int x = 0; x < imagem[y].length; x++){
            imagem[y][x] = (double)cinza[y][x] / 255;
         }
      }
      return imagem;
   }

   /**
    * Testa as previsões do modelo no formato de probabilidade.
    * @param modelo modelo sequencial de camadas.
    * @param imagemTeste nome da imagem que deve estar no diretório /minst/teste/
    */
   static void testarPorbabilidade(Sequencial modelo, String imagemTeste){
      System.out.println("\nTestando: " + imagemTeste);
      double[][][] teste1 = new double[1][][];
      teste1[0] = imagemParaMatriz("/dados/mnist/teste/" + imagemTeste + ".jpg");
      modelo.calcularSaida(teste1);
      double[] previsao = modelo.saidaParaArray();
      for(int i = 0; i < previsao.length; i++){
         System.out.println("Prob: " + i + ": " + (int)(previsao[i]*100) + "%");
      }
   }

   /**
    * 
    * @param amostras quantidade de amostras por dígito
    * @param digitos quantidade de dígitos, iniciando do dígito 0.
    * @return
    */
   static double[][][][] carregarDadosMNIST(String caminho, int amostras, int digitos){
      double[][][][] entradas = new double[digitos * amostras][1][][];

      int id = 0;
      for(int i = 0; i < digitos; i++){
         for(int j = 0; j < amostras; j++){
            String caminhoCompleto = caminho + i + "/img_" + j + ".jpg";
            double[][] imagem = imagemParaMatriz(caminhoCompleto);
            entradas[id++][0] = imagem;
         }
      }

      System.out.println("Imagens carregadas (" + entradas.length + ").");
      return entradas;
   }

   /**
    * 
    * @param amostras
    * @param digitos
    * @return
    */
   static double[][] criarRotulosMNIST(int amostras, int digitos){
      double[][] rotulos = new double[digitos * amostras][digitos];
      for(int numero = 0; numero < digitos; numero++){
         for(int i = 0; i < amostras; i++){
            int indice = numero * amostras + i;
            rotulos[indice][numero] = 1;
         }
      }
      
      System.out.println("Rótulos gerados de 0 a " + (digitos-1) + ".");
      return rotulos;
   }

   /**
    * Formata o valor recebido para a quantidade de casas após o ponto
    * flutuante.
    * @param valor valor alvo.
    * @param casas quantidade de casas após o ponto flutuante.
    * @return
    */
   static String formatarDecimal(double valor, int casas){
      String valorFormatado = "";

      String formato = "#.";
      for(int i = 0; i < casas; i++) formato += "#";

      DecimalFormat df = new DecimalFormat(formato);
      valorFormatado = df.format(valor);

      return valorFormatado;
   }

   /**
    * 
    * @param sample
    */
   static void printImagemMNIST(double[][] sample){
      for(int y = 0; y < sample.length; y++){
         for(int x = 0; x < sample[y].length; x++){
            double v = sample[y][x];
            if(v < 0.5) System.out.print("    ");
            else System.out.print((int)(v*100) + " ");
         }
         System.out.println();
      }
   }

   /**
    * Salva um arquivo csv com o historico de desempenho do modelo.
    * @param modelo modelo.
    * @param caminho caminho onde será salvo o arquivo.
    */
   static void exportarHistorico(Modelo modelo, String caminho){
      System.out.println("Exportando histórico de perda");
      double[] perdas = modelo.historico();
      double[][] dadosPerdas = new double[perdas.length][1];

      for(int i = 0; i < dadosPerdas.length; i++){
         dadosPerdas[i][0] = perdas[i];
      }

      Dados dados = new Dados(dadosPerdas);
      ged.exportarCsv(dados, caminho);
   }
}
