package testes.modelos;

import java.awt.image.BufferedImage;

import lib.ged.Dados;
import lib.ged.Ged;
import lib.geim.Geim;
import rna.avaliacao.perda.*;
import rna.camadas.*;
import rna.otimizadores.*;
import rna.modelos.Modelo;
import rna.modelos.Sequencial;

public class TesteImagem{
   public static void main(String[] args){
      Ged ged = new Ged();
      Geim geim = new Geim();

      ged.limparConsole();

      //importando imagem para treino da rede
      final String caminho = "/dados/mnist/treino/8/img_0.jpg";
      BufferedImage imagem = geim.lerImagem(caminho);
      double[][] dados = geim.imagemParaDadosTreinoEscalaCinza(imagem);
      int nEntrada = 2;// posição x y do pixel
      int nSaida = 1;// valor de escala de cinza/brilho do pixel

      //preparando dados para treinar a rede
      double[][] dadosEntrada = (double[][]) ged.separarDadosEntrada(dados, nEntrada);
      double[][] dadosSaida = (double[][]) ged.separarDadosSaida(dados, nSaida);

      //criando rede neural para lidar com a imagem
      //nesse exemplo queremos que ela tenha overfitting
      Sequencial modelo = new Sequencial(new Camada[]{
         new Densa(nEntrada, 7, "sigmoid"),
         new Densa(7, "sigmoid"),
         new Densa(nSaida, "sigmoid"),
      });
      modelo.setHistorico(true);
      Otimizador otm = new SGD(0.001, 0.995);
      modelo.compilar(otm, new MSE());
      modelo.treinar(dadosEntrada, dadosSaida, 2_000, false);

      //avaliando resultados
      double precisao = 1 - modelo.avaliador().erroMedioAbsoluto(dadosEntrada, dadosSaida);
      System.out.println("Precisão = " + (precisao * 100));
      System.out.println("Perda = " + modelo.avaliar(dadosEntrada, dadosSaida));

      exportarHistoricoPerda(modelo, ged);
   }

   /**
    * Salva um arquivo csv com o historico de desempenho da rede.
    * @param rede rede neural.
    * @param ged gerenciador de dados.
    */
   public static void exportarHistoricoPerda(Modelo rede, Ged ged){
      System.out.println("Exportando histórico de perda");
      double[] perdas = rede.historico();
      double[][] dadosPerdas = new double[perdas.length][1];

      for(int i = 0; i < dadosPerdas.length; i++){
         dadosPerdas[i][0] = perdas[i];
      }

      Dados dados = new Dados(dadosPerdas);
      ged.exportarCsv(dados, "historico-perda");
   }
}
