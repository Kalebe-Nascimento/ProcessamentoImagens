import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.IJ;
import ij.process.ImageProcessor;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import java.awt.AWTEvent;

public class RGB_Para_Escala_Cinza_Tres_Tipos implements PlugIn, DialogListener {

    public void run(String arg) {
        apresentarInterfaceGrafica(); // Inicia interface gráfica
    }

    public void apresentarInterfaceGrafica() {
        GenericDialog gd = new GenericDialog("Converter Imagem RGB para Escala de Cinza");
        gd.addDialogListener(this);
        gd.addMessage("Selecione a Estratégia RGB para conversão.");

        String[] estrategia = {"Media Aritmetica", "Luminance Analogica", "Luminance Digital"};

        gd.addMessage("Escolha a Imagem a ser convertida para escala de cinza");
        gd.addImageChoice("Imagem: ", "");

        gd.addRadioButtonGroup("Escolher uma das estratégias", estrategia, 1, 3, estrategia[0]);
        gd.addCheckbox("Criar uma nova imagem", true);

        gd.showDialog();

        if (gd.wasCanceled()) {
            IJ.showMessage("PlugIn cancelado!");
            return;
        }

        if (gd.wasOKed()) {
            ImagePlus imagem = gd.getNextImage();
            boolean criarNova = gd.getNextBoolean();
            String metodo = gd.getNextRadioButton();

            if (imagem.getType() != ImagePlus.COLOR_RGB) {
                IJ.showMessage("Imagem Selecionada não é RGB!");
                return;
            }

            // Chama a função que executa o método escolhido
            selecionarMetodoConversao(metodo, imagem, criarNova);
        }
    }

    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        return !gd.wasCanceled();
    }

    //  Função central que escolhe e aplica a estratégia de conversão
    private void selecionarMetodoConversao(String metodo, ImagePlus imagem, boolean nova) {
        switch (metodo) {
            case "Media Aritmetica":
                converterImagem(imagem, nova, "media");
                break;
            case "Luminance Analogica":
                converterImagem(imagem, nova, "analogica");
                break;
            case "Luminance Digital":
                converterImagem(imagem, nova, "digital");
                break;
            default:
                IJ.showMessage("Método de conversão não reconhecido.");
        }
    }

    // Conversão genérica com base no tipo passado
    private void converterImagem(ImagePlus imagem, boolean criarNova, String tipo) {
        ImageProcessor proc = imagem.getProcessor();
        int w = imagem.getWidth(), h = imagem.getHeight();
        int[] rgb = new int[3];

        ImageProcessor destino;
        ImagePlus imagemDestino;

        if (criarNova) {
            imagemDestino = IJ.createImage("Imagem Cinza", "8-bit", w, h, 1);
            destino = imagemDestino.getProcessor();
        } else {
            imagemDestino = imagem;
            destino = proc;
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                rgb = proc.getPixel(x, y, rgb);
                int cinza = calcularValorCinza(rgb, tipo);

                if (criarNova) {
                    destino.putPixel(x, y, cinza); // 8-bit
                } else {
                    destino.putPixel(x, y, new int[]{cinza, cinza, cinza}); // RGB
                }
            }
        }

        imagemDestino.setProcessor(destino);
        imagemDestino.show();
    }

    //  Define o valor cinza conforme o tipo
    private int calcularValorCinza(int[] rgb, String tipo) {
        switch (tipo) {
            case "media":
                return (rgb[0] + rgb[1] + rgb[2]) / 3;
            case "analogica":
                return (int)(0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]);
            case "digital":
                return (int)(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
            default:
                return 0;
        }
    }
}
