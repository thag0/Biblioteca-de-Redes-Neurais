

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
      
      int[] formEntrada = {28, 28, 1};
      int[] formFiltro = {3, 3};

      double[][][][] entradas = new double[10][formEntrada[2]][formEntrada[0]][formEntrada[1]];
      double[][] saidas = new double[10][10];
      for(int i = 0; i < 10; i++){
         entradas[i][0] = imagemParaMatriz("/dados/mnist/" + i + ".png");
         saidas[i][i] = 1;
      }

      Sequencial cnn = criarModelo(formEntrada, formFiltro);

      //treinar e marcar tempo
      long t1, t2;
      long horas, minutos, segundos;

      t1 = System.nanoTime();
      System.out.println("Treinando.");
      cnn.treinar(entradas, saidas, 401, true);
      t2 = System.nanoTime();

      long tempoDecorrido = t2 - t1;
      long segundosTotais = TimeUnit.NANOSECONDS.toSeconds(tempoDecorrido);
      horas = segundosTotais / 3600;
      minutos = (segundosTotais % 3600) / 60;
      segundos = segundosTotais % 60;
      System.out.println("Tempo de treinamento: " + horas + "h " + minutos + "m " + segundos + "s");

      for(int i = 0; i < 10; i++){
         System.out.println("Real: " + i + ", Pred: " + testarImagem(cnn, entradas[i][0]));
      }
   } 

   public static Sequencial criarModelo(int[] formEntrada, int[] formFiltro){
      Inicializador ini = new Xavier();
      Convolucional conv1 = new Convolucional(formEntrada, formFiltro, 8, "tanh");
      Convolucional conv2 = new Convolucional(conv1.formatoSaida(), formFiltro, 8, "tanh");
      Flatten flat = new Flatten(conv2.formatoSaida());
      Densa densa1 = new Densa(flat.tamanhoSaida(), 20, "leakyrelu");
      Densa densa2 = new Densa(densa1.tamanhoSaida(), 10, "softmax");

      Sequencial cnn = new Sequencial(new Camada[]{
         conv1,
         conv2,
         flat,
         densa1,
         densa2,
      });

      cnn.compilar(new SGD(0.001, 0.995), new EntropiaCruzada(), ini);

      return cnn;
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
      double[] prev = modelo.obterSaida();

      for(int i = 0; i < prev.length; i++){
         if(prev[i] > 0.95){
            return i;
         }
      }

      return -1;
   }
}
