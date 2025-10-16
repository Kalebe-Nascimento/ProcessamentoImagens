import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Operacoes_Morfologicas_Binarias_ implements PlugIn {

    public void run(String argumento) {
        ImagePlus imagemEntrada = WindowManager.getCurrentImage();
        if (imagemEntrada == null) {
            IJ.showMessage("Erro", "Nenhuma imagem aberta.");
            return;
        }

        if (!verificarSeImagemEhBinaria(imagemEntrada.getProcessor())) {
            IJ.showMessage("Erro", "A imagem não é binária.");
            return;
        }

        GenericDialog dialogo = new GenericDialog("Operações Morfológicas");
        dialogo.addRadioButtonGroup(
            "Escolha a operação:",
            new String[]{"Dilatação", "Erosão", "Abertura", "Fechamento", "Borda", "Esqueleto"},
            6, 1, "Dilatação"
        );
        dialogo.showDialog();

        if (dialogo.wasCanceled()) return;

        String operacaoSelecionada = dialogo.getNextRadioButton();
        ImageProcessor imagem = imagemEntrada.getProcessor().convertToByteProcessor();

        switch (operacaoSelecionada) {
            case "Dilatação":
                aplicarOperacaoMorfologica(imagem, "dilatacao");
                break;
            case "Erosão":
                aplicarOperacaoMorfologica(imagem, "erosao");
                break;
            case "Abertura":
                aplicarOperacaoMorfologica(imagem, "erosao");
                aplicarOperacaoMorfologica(imagem, "dilatacao");
                break;
            case "Fechamento":
                aplicarOperacaoMorfologica(imagem, "dilatacao");
                aplicarOperacaoMorfologica(imagem, "erosao");
                break;
            case "Borda":
                ByteProcessor copia = (ByteProcessor) imagem.duplicate();
                aplicarOperacaoMorfologica(copia, "erosao");
                subtrairImagens(imagem, copia);
                break;
            case "Esqueleto":
                ByteProcessor resultado = esqueletizarPorErosaoComAbertura((ByteProcessor) imagem.duplicate());
                imagem.setPixels(resultado.getPixels());
                break;
        }

        new ImagePlus("Resultado - " + operacaoSelecionada, imagem).show();
    }

    private boolean verificarSeImagemEhBinaria(ImageProcessor imagem) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int pixel = imagem.getPixel(x, y);
                if (pixel != 0 && pixel != 255) {
                    return false;
                }
            }
        }
        return true;
    }

    private void aplicarOperacaoMorfologica(ImageProcessor imagem, String tipo) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        ByteProcessor resultado = new ByteProcessor(largura, altura);
        ByteProcessor copia = (ByteProcessor) imagem.duplicate();

        int[][] elemento = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };

        for (int y = 1; y < altura - 1; y++) {
            for (int x = 1; x < largura - 1; x++) {
                if (tipo.equals("dilatacao") && copia.getPixel(x, y) == 255) {
                    aplicarDilatacao(resultado, x, y, elemento);
                }
                else if (tipo.equals("erosao")) {
                    aplicarErosao(copia, resultado, x, y, elemento);
                }
            }
        }

        imagem.setPixels(resultado.getPixels());
    }

    private void aplicarDilatacao(ByteProcessor imagem, int x, int y, int[][] elemento) {
        for (int j = -1; j <= 1; j++) {
            for (int i = -1; i <= 1; i++) {
                if (elemento[j + 1][i + 1] == 1) {
                    imagem.putPixel(x + i, y + j, 255);
                }
            }
        }
    }

    private void aplicarErosao(ByteProcessor original, ByteProcessor destino, int x, int y, int[][] elemento) {
        boolean erodir = false;

        for (int j = -1; j <= 1 && !erodir; j++) {
            for (int i = -1; i <= 1 && !erodir; i++) {
                if (elemento[j + 1][i + 1] == 1) {
                    int pixelVizinho = original.getPixel(x + i, y + j);
                    if (pixelVizinho == 0) {
                        erodir = true;
                    }
                }
            }
        }

        destino.putPixel(x, y, erodir ? 0 : 255);
    }

    private void subtrairImagens(ImageProcessor original, ImageProcessor outra) {
        int largura = original.getWidth();
        int altura = original.getHeight();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int valor = original.getPixel(x, y) - outra.getPixel(x, y);
                original.putPixel(x, y, Math.max(0, valor));
            }
        }
    }

    private ByteProcessor esqueletizarPorErosaoComAbertura(ByteProcessor imagemOriginal) {
        ByteProcessor imagemAtual = (ByteProcessor) imagemOriginal.duplicate();
        ByteProcessor esqueletoFinal = new ByteProcessor(imagemOriginal.getWidth(), imagemOriginal.getHeight());

        while (temPixelBranco(imagemAtual)) {
            ByteProcessor imagemErodida = (ByteProcessor) imagemAtual.duplicate();
            aplicarOperacaoMorfologica(imagemErodida, "erosao");

            ByteProcessor abertura = (ByteProcessor) imagemErodida.duplicate();
            aplicarOperacaoMorfologica(abertura, "dilatacao");

            ByteProcessor camada = (ByteProcessor) imagemErodida.duplicate();
            subtrairImagens(camada, abertura);

            somarImagens(esqueletoFinal, camada);

            imagemAtual = imagemErodida;
        }

        return esqueletoFinal;
    }

    private boolean temPixelBranco(ByteProcessor imagem) {
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (imagem.getPixel(x, y) == 255) {
                    return true;
                }
            }
        }
        return false;
    }

    private void somarImagens(ImageProcessor destino, ImageProcessor adicionar) {
        int largura = destino.getWidth();
        int altura = destino.getHeight();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int val1 = destino.getPixel(x, y);
                int val2 = adicionar.getPixel(x, y);
                destino.putPixel(x, y, Math.min(255, val1 + val2));
            }
        }
    }
}