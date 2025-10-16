import java.awt.AWTEvent;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.gui.Plot;

public class Histograma_Expansao_Equalizacao implements PlugIn, DialogListener {

    @Override
    public void run(String arg) {
        ImagePlus imagemOriginal = IJ.getImage();
        
        if (imagemOriginal.getType() == ImagePlus.GRAY8) {
            aplicarProcessamento(imagemOriginal);    
        } else {
            IJ.error("A imagem precisa estar em escala de cinza (8 bits)");
        }
    }

    private void aplicarProcessamento(ImagePlus imagemOriginal) {
      
        mostrarHistograma(imagemOriginal, "Histograma Original");

        ImagePlus imagemProcessada = imagemOriginal.duplicate();
        imagemProcessada.show();

        String[] opcoes = {"Expansão", "Equalização"};
        GenericDialog dialogo = new GenericDialog("Processamento de Histograma");
        dialogo.addDialogListener(this);
        dialogo.addRadioButtonGroup("Escolha o método:", opcoes, 1, 2, opcoes[0]);

        dialogo.showDialog();        
        
        if (dialogo.wasCanceled()) {
            IJ.showMessage("Operação cancelada.");
        } else if (dialogo.wasOKed()) {
            String metodoSelecionado = dialogo.getNextRadioButton();
            if (metodoSelecionado.equals("Expansão")) {
                aplicarExpansao(imagemProcessada);
            } else {
                aplicarEqualizacao(imagemProcessada);    
            }
            IJ.showMessage("Processamento concluído.");
        }
    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        return !gd.wasCanceled();
    }

    private void aplicarExpansao(ImagePlus imagem) {
        ImageProcessor editorPixels = imagem.getProcessor();
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        int[] histograma = new int[256];

        // Calcula histograma
        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                int valor = editorPixels.getPixel(x, y);
                histograma[valor]++;
            }
        }

      
        int tomMin = 0, tomMax = 255;
        for (int i = 0; i < 256; i++) {
            if (histograma[i] > 0) {
                tomMin = i;
                break;
            }
        }
        for (int i = 255; i >= 0; i--) {
            if (histograma[i] > 0) {
                tomMax = i;
                break;
            }
        }

        //aplicacao linear com os novos valores maximos e minimos
        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                int valor = editorPixels.getPixel(x, y);
                int novoValor = (255 * (valor - tomMin)) / (tomMax - tomMin);
                editorPixels.putPixel(x, y, novoValor);
            }
        }

        imagem.setProcessor(editorPixels);
        imagem.show();
        mostrarHistograma(imagem, "Histograma após Expansão");
    }

    private void aplicarEqualizacao(ImagePlus imagem) {
        ImageProcessor editorPixels = imagem.getProcessor();
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        int totalPixels = largura * altura;

        int[] histograma = new int[256];
        double[] probabilidade = new double[256];
        double[] cdfAcumulado = new double[256];
        int[] novosValores = new int[256];

        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                int valor = editorPixels.getPixel(x, y);
                histograma[valor]++;
            }
        }

  
        for (int i = 0; i < 256; i++) {
            probabilidade[i] = (double) histograma[i] / totalPixels;
        }

        // Calcula CDF acumulado e novos valores
        cdfAcumulado[0] = probabilidade[0] * 255;
        novosValores[0] = (int) cdfAcumulado[0];
        for (int i = 1; i < 256; i++) {
            cdfAcumulado[i] = cdfAcumulado[i - 1] + probabilidade[i] * 255;
            novosValores[i] = (int) cdfAcumulado[i];
        }

        // Aplica nova intensidade aos pixels
        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                int valor = editorPixels.getPixel(x, y);
                editorPixels.putPixel(x, y, novosValores[valor]);
            }
        }

        imagem.setProcessor(editorPixels);
        imagem.show();
        mostrarHistograma(imagem, "Histograma após Equalização");
    }

    private void mostrarHistograma(ImagePlus imagem, String titulo) {
        ImageStatistics estatisticas = imagem.getStatistics();
        int[] histograma = estatisticas.histogram;

        double[] x = new double[histograma.length];
        double[] y = new double[histograma.length];
        for (int i = 0; i < histograma.length; i++) {
            x[i] = i;
            y[i] = histograma[i];
        }

        Plot grafico = new Plot(titulo, "Intensidade", "Frequência", x, y);
        grafico.show();
    }
}
