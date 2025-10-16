import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.*;
import java.util.*;

public class Componentes_Conexos_ implements PlugInFilter {

    @Override
    public int setup(String argumento, ImagePlus imagemEntrada) {
        return DOES_8G; 
    }

    @Override
    public void run(ImageProcessor imagemOriginal) {
        int largura = imagemOriginal.getWidth();
        int altura = imagemOriginal.getHeight();

        if (!imagemEhBinaria(imagemOriginal)) {
            IJ.error("Erro", "A imagem de entrada precisa ser binária (valores 0 ou 255).");
            return;
        }

        String[] opcoesSaida = {"Tons de Cinza", "Colorida (RGB)"};
        GenericDialog dialogo = new GenericDialog("Escolha o tipo de saída");
        dialogo.addChoice("Tipo de saída:", opcoesSaida, opcoesSaida[0]);
        dialogo.showDialog();
        if (dialogo.wasCanceled()) return;

        boolean usarCores = dialogo.getNextChoice().equals("Colorida (RGB)");

        ImagePlus imagemSaida = IJ.createImage(
            "Componentes Rotulados",
            usarCores ? "RGB" : "8-bit",
            largura, altura, 1
        );
        ImageProcessor processadorSaida = imagemSaida.getProcessor();

    
        int[][] matrizRotulos = new int[largura][altura];
        int rotuloAtual = 1;

        //cria fila FIFO
        Queue<Point> filaPixels = new LinkedList<>();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (imagemOriginal.getPixel(x, y) == 255 && matrizRotulos[x][y] == 0) {
                    //atribui ao pixel o componente e salva
                    matrizRotulos[x][y] = rotuloAtual;
                    filaPixels.add(new Point(x, y));

                    //enquanto a fila nao tiver vazia ele vai checar e pega coordenadas
                    while (!filaPixels.isEmpty()) {
                        Point pixel = filaPixels.poll();
                        int px = pixel.x;
                        int py = pixel.y;

            
                        int[][] vizinhos = {
                            {px + 1, py}, {px - 1, py},
                            {px, py + 1}, {px, py - 1}
                        };

                        for (int[] vizinho : vizinhos) {
                            int vx = vizinho[0];
                            int vy = vizinho[1];

                            //ve se ta na imagem e se foi rotulado
                            if (vx >= 0 && vx < largura && vy >= 0 && vy < altura) {
                                if (matrizRotulos[vx][vy] == 0 && imagemOriginal.getPixel(vx, vy) == 255) {
                                    matrizRotulos[vx][vy] = rotuloAtual;
                                    filaPixels.add(new Point(vx, vy));
                                }
                            }
                        }
                    }
                    rotuloAtual++;
                }
            }
        }

      
        if (usarCores) {
        
            //vetor para guardar valores de cores usadas
            Set<Color> coresUsadas = new HashSet<>();
            Color[] coresComponentes = new Color[rotuloAtual];
            Random gerador = new Random();

            //gerar cores aleatorias no rgb para cada rotulo
            for (int i = 1; i < rotuloAtual; i++) {
                Color novaCor;
                do {
                    novaCor = new Color(
                        gerador.nextInt(256),
                        gerador.nextInt(256),
                        gerador.nextInt(256)
                    );
                } while (coresUsadas.contains(novaCor));
                coresUsadas.add(novaCor);
                coresComponentes[i] = novaCor;
            }

            // ve que pertence a quem o rotulo e pinta
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    if (matrizRotulos[x][y] != 0) {
                        processadorSaida.setColor(coresComponentes[matrizRotulos[x][y]]);
                        processadorSaida.drawPixel(x, y);
                    }
                }
            }
        } else {
            // escala diferenciar cinza dos rotul
            int intervalo = 255 / rotuloAtual;
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    if (matrizRotulos[x][y] != 0) {
                        int valorCinza = 50 + (matrizRotulos[x][y] * intervalo) % 205;
                        processadorSaida.putPixel(x, y, valorCinza);
                    }
                }
            }
        }

        imagemSaida.show();
        IJ.log("Número total de componentes conexos: " + (rotuloAtual - 1));
    }

    private boolean imagemEhBinaria(ImageProcessor imagem) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int valor = imagem.getPixel(x, y);
                if (valor != 0 && valor != 255) {
                    return false;
                }
            }
        }
        return true;
    }
}
