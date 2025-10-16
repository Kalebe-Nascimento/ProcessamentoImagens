import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;

public class Filtro_Linear implements PlugIn {

    private String filtroEscolhido = "Passa-Baixa (Média)";

    @Override
    public void run(String arg) {
        // Criar a interface gráfica para seleção do filtro
        GenericDialog caixaDialogo = new GenericDialog("Seleção de Filtro");
        caixaDialogo.addRadioButtonGroup("Escolha o filtro:", new String[]{
                "Passa-Baixa (Média)",
                "Passa-Alta",
                "Detecção de Bordas"
        }, 3, 1, filtroEscolhido);

        caixaDialogo.addDialogListener((gd, e) -> {
            filtroEscolhido = gd.getNextRadioButton();
            return true;
        });

        caixaDialogo.showDialog();
        if (caixaDialogo.wasCanceled()) {
            IJ.showMessage("Operação cancelada.");
            return;
        }

        ImagePlus imagemOriginal = IJ.getImage();
        if (imagemOriginal == null) {
            IJ.showMessage("Erro", "Nenhuma imagem aberta.");
            return;
        }

        if (imagemOriginal.getType() != ImagePlus.GRAY8) {
            IJ.showMessage("Erro", "A imagem deve estar em tons de cinza (8 bits).");
            return;
        }

        ImageProcessor processador = imagemOriginal.getProcessor();
        ImageProcessor imagemProcessada = processador.duplicate();

        switch (filtroEscolhido) {
            case "Passa-Baixa (Média)":
                aplicarFiltroMedia(imagemProcessada);
                exibirImagemFiltrada(imagemProcessada, "Filtro Passa-Baixa");
                break;
            case "Passa-Alta":
                aplicarFiltroAltaFrequencia(imagemProcessada);
                exibirImagemFiltrada(imagemProcessada, "Filtro Passa-Alta");
                break;
            case "Detecção de Bordas":
                aplicarFiltroDeteccaoBordas(imagemProcessada);
                exibirImagemFiltrada(imagemProcessada, "Filtro de Bordas");
                break;
        }

        imagemOriginal.show(); // Reexibe a imagem original (opcional)
    }

    private void aplicarFiltroMedia(ImageProcessor imagem) {
        int[][] mascaraMedia = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        aplicarConvolucao(imagem, mascaraMedia, 9); // divisor = soma dos pesos
    }

    private void aplicarFiltroAltaFrequencia(ImageProcessor imagem) {
        int[][] mascaraAlta = {
            {1, -2, 1},
            {-2, 5, -2},
            {1, -2, 1}
        };
        aplicarConvolucao(imagem, mascaraAlta, 1);
    }

    private void aplicarFiltroDeteccaoBordas(ImageProcessor imagem) {
        int[][] mascaraBorda = {
            {1, 0, -1},
            {1, 0, -1},
            {1, 0, -1}
        };
        aplicarConvolucao(imagem, mascaraBorda, 1);
    }

    private void aplicarConvolucao(ImageProcessor imagem, int[][] mascara, int divisor) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        ImageProcessor imagemOriginal = imagem.duplicate();

        for (int x = 1; x < largura - 1; x++) {
            for (int y = 1; y < altura - 1; y++) {
                int soma = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int valorPixelVizinho = imagemOriginal.getPixel(x + i, y + j);
                        soma += valorPixelVizinho * mascara[i + 1][j + 1];
                    }
                }

                int novoValor = soma / divisor;
                imagem.putPixel(x, y, novoValor);
            }
        }
    }

    private void exibirImagemFiltrada(ImageProcessor imagemFiltrada, String titulo) {
        ImagePlus imagemResultado = new ImagePlus(titulo, imagemFiltrada);
        imagemResultado.show();
    }
}
