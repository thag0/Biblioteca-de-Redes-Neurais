import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import ged.Ged;
import geim.Geim;
import rna.avaliacao.perda.*;
import rna.estrutura.*;
import rna.inicializadores.*;
import rna.modelos.Sequencial;
import rna.otimizadores.*;

public class MainConv{
   static Ged ged = new Ged();
   static Geim geim = new Geim();

   public static void main(String[] args){
      ged.limparConsole();
      
      double[][][][] entradas = new double[100][10][][];
      double[][] saidas = new double[100][10];
      entradas = carregarDadosMNIST();
      System.out.println("Imagens carregadas.");
      saidas = carregarRotulosMNIST();
      System.out.println("Rótulos carregados.");

      Sequencial cnn = criarModelo();

      //treinar e marcar tempo
      long t1, t2;
      long horas, minutos, segundos;

      System.out.println("Treinando.");
      t1 = System.nanoTime();
      cnn.treinar(entradas, saidas, 1);
      t2 = System.nanoTime();

      long tempoDecorrido = t2 - t1;
      long segundosTotais = TimeUnit.NANOSECONDS.toSeconds(tempoDecorrido);
      horas = segundosTotais / 3600;
      minutos = (segundosTotais % 3600) / 60;
      segundos = segundosTotais % 60;
      System.out.println("Tempo de treinamento: " + horas + "h " + minutos + "m " + segundos + "s");
      // testes.TesteSequencial.exportarHistoricoPerda(cnn);

      //-------------------------------------

      for(int i = 0; i < 10; i++){
         System.out.println("Real: " + i + ", Pred: " + testarImagem(cnn, entradas[i][0]));
      }

      System.out.println("\nTeste 6");
      double[][][] teste = new double[1][][];
      teste[0] = imagemParaMatriz("/dados/mnist/teste/6_teste.png");
      cnn.calcularSaida(teste);
      double[] previsao = cnn.saidaParaArray();
      for(int i = 0; i < previsao.length; i++){
         System.out.println("Prob: " + i + ": " + (int)(previsao[i]*100) + "%");
      }
   } 

   public static Sequencial criarModelo(){
      int[] formEntrada = {28, 28, 1};
      
      Sequencial modelo = new Sequencial(new Camada[]{
         new Convolucional(formEntrada, new int[]{5, 5}, 20, "tanh"),
         new Flatten(),
         new Densa(40, "tanh"),
         new Densa(10, "softmax"),
      });

      modelo.compilar(new SGD(0.001, 0.9), new EntropiaCruzada(), new Xavier());
      // modelo.configurarHistorico(true);

      return modelo;
   }

   public static double[][] imagemParaMatriz(String caminho){
      BufferedImage img = geim.lerImagem(caminho);
      double[][] imagem = new double[img.getHeight()][img.getWidth()];

      int[][] cinza = geim.obterCinza(img);

      for(int y = 0; y < imagem.length; y++){
         for(int x = 0; x < imagem[y].length; x++){
            imagem[y][x] = cinza[y][x];
         }
      }

      return imagem;
   }

   public static int testarImagem(Sequencial modelo, double[][] entrada){
      double[][][] e = new double[1][][];
      e[0] = entrada;

      modelo.calcularSaida(e);
      double[] prev = modelo.saidaParaArray();

      for(int i = 0; i < prev.length; i++){
         if(prev[i] > 0.66){
            return i;
         }
      }

      return -1;
   }

   public static double[][][][] carregarDadosMNIST(){
      double[][][][] entradas = new double[100][1][][];
      String caminho = "/dados/mnist/treino/";

      int amostras = 10;
      for(int i = 0; i < 100; i++){
         for(int j = 0; j < amostras; j++){
            double[][] imagem = imagemParaMatriz(caminho + j + "/img_" + j + ".jpg");
            entradas[i][0] = imagem;
         }
      }

      return entradas;
   }

   public static double[][] carregarRotulosMNIST(){
      int totalNumeros = 10;
      int rotulosPorNumero = 10;
  
      double[][] rotulos = new double[totalNumeros * rotulosPorNumero][totalNumeros];
  
      for (int numero = 0; numero < totalNumeros; numero++) {
         for (int i = 0; i < rotulosPorNumero; i++) {
         int indice = numero * rotulosPorNumero + i;
         rotulos[indice][numero] = 1;
         }
      }
  
      return rotulos;
   }
}