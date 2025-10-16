import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.util.Arrays;

public class Filtro_Nao_Linear implements PlugIn {

    private String filtroEscolhido = "Sobel";

    @Override
    public void run(String arg) {
        ImagePlus imagem = IJ.getImage();

        if (imagem == null) {
            IJ.showMessage("Erro", "Nenhuma imagem aberta.");
            return;
        }

        if (imagem.getType() != ImagePlus.GRAY8) {
            IJ.showMessage("Erro", "A imagem precisa estar em tons de cinza (8 bits).");
            return;
        }

        GenericDialog dialogo = new GenericDialog("Aplicar Filtro Não Linear");
        dialogo.addRadioButtonGroup("Selecione o filtro:", new String[]{"Sobel", "Mediana"}, 2, 1, filtroEscolhido);
        dialogo.showDialog();

        if (dialogo.wasCanceled()) return;

        filtroEscolhido = dialogo.getNextRadioButton();
        ImageProcessor processadorOriginal = imagem.getProcessor().duplicate();

        if (filtroEscolhido.equals("Sobel")) {
            aplicarFiltroSobel(processadorOriginal);
        } else {
            aplicarFiltroMediana(processadorOriginal);
        }
    }

    private void aplicarFiltroSobel(ImageProcessor imagem) {
        int[][] matrizSobelVertical = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };

        int[][] matrizSobelHorizontal = {
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
        };

        ImageProcessor imagemSobelX = imagem.duplicate();
        ImageProcessor imagemSobelY = imagem.duplicate();

        aplicarKernel(imagemSobelX, matrizSobelVertical);
        aplicarKernel(imagemSobelY, matrizSobelHorizontal);

        ImageProcessor imagemResultado = imagem.duplicate();
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                int valorX = imagemSobelX.getPixel(x, y);
                int valorY = imagemSobelY.getPixel(x, y);
                int magnitude = (int) Math.sqrt(valorX * valorX + valorY * valorY);
                imagemResultado.putPixel(x, y, magnitude);
            }
        }

        new ImagePlus("Sobel - Vertical", imagemSobelX).show();
        new ImagePlus("Sobel - Horizontal", imagemSobelY).show();
        new ImagePlus("Sobel - Resultado", imagemResultado).show();
    }

    private void aplicarKernel(ImageProcessor imagem, int[][] kernel) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        ImageProcessor imagemOriginal = imagem.duplicate();

        for (int x = 1; x < largura - 1; x++) {
            for (int y = 1; y < altura - 1; y++) {
                int soma = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        soma += imagemOriginal.getPixel(x + i, y + j) * kernel[i + 1][j + 1];
                    }
                }
                imagem.putPixel(x, y, soma);
            }
        }
    }

    private void aplicarFiltroMediana(ImageProcessor imagem) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        ImageProcessor imagemOriginal = imagem.duplicate();

        for (int y = 1; y < altura - 1; y++) {
            for (int x = 1; x < largura - 1; x++) {
                int[] vizinhanca = new int[9];
                int index = 0;

                for (int j = -1; j <= 1; j++) {
                    for (int i = -1; i <= 1; i++) {
                        vizinhanca[index++] = imagemOriginal.getPixel(x + i, y + j);
                    }
                }

                Arrays.sort(vizinhanca);
                imagem.putPixel(x, y, vizinhanca[4]); // valor da mediana
            }
        }

        new ImagePlus("Filtro de Mediana", imagem).show();
    }
}
