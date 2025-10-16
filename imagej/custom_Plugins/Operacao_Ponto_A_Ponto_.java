import java.awt.AWTEvent;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Operacao_Ponto_A_Ponto_ implements PlugIn, DialogListener {

    //run para verificar se a imagem é tons de cinza
    public void run(String arg) {
        ImagePlus imagemOriginal = IJ.getImage(); 
        
        if (imagemOriginal.getType() == ImagePlus.COLOR_RGB) {
            abrirInterface(imagemOriginal); 
        } else {
            IJ.error("A imagem não está no formato RGB."); 
        }
    }

    // Starta interface com sliders para cada operação
    public void abrirInterface(ImagePlus imagemOriginal) {
        ImagePlus imagemCopia = imagemOriginal.duplicate();
        imagemCopia.show(); 

        GenericDialog janela = new GenericDialog("Operações");
        janela.addDialogListener(this); 

        janela.addSlider("Brilho", -255, 255, 0, 1); 
        janela.addSlider("Contraste", -255, 255, 0, 1); 
        janela.addSlider("Solarização", 0, 255, 0, 1); 
        janela.addSlider("Dessaturação", 0, 1, 1, 0.01); 

        janela.showDialog(); 

        if (janela.wasCanceled()) {
            IJ.showMessage("Operação cancelada."); 
        } else if (janela.wasOKed()) {
            IJ.showMessage("Operação concluída."); 
        }
    }

    // Listener da interface para ver se foi cancelada ou aplicar em tempo real
    @Override
    public boolean dialogItemChanged(GenericDialog janela, AWTEvent evento) {
        if (janela.wasCanceled()) return false; 
        aplicarOperacoes(janela); 
        return true;
    }

    // Aplica efeitos ponto-a-ponto
    public void aplicarOperacoes(GenericDialog janela) {
        int[] listaIds = WindowManager.getIDList(); 

        ImagePlus imagemOriginal = WindowManager.getImage(listaIds[0]); 
        ImagePlus imagemModificada = WindowManager.getImage(listaIds[1]);

        ImageProcessor procOriginal = imagemOriginal.getProcessor(); 
        ImageProcessor procModificado = imagemModificada.getProcessor();

        //variaveis que recebe os valores das operacoes dos sliders
        int brilho = (int) janela.getNextNumber();
        int contraste = (int) janela.getNextNumber();
        int solarizacao = (int) janela.getNextNumber();
        double dessaturacao = janela.getNextNumber();

        int largura = imagemOriginal.getWidth(); 
        int altura = imagemOriginal.getHeight();

        int[] pixelOriginal = new int[3]; 
        int[] pixelNovo = new int[3];

        //calcular o valor de constraste e percorre a imagem para calcular a media de dessaturacao
        float fatorContraste = calcularFatorContraste(contraste); 

        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                procOriginal.getPixel(x, y, pixelOriginal); 

                int media = (pixelOriginal[0] + pixelOriginal[1] + pixelOriginal[2]) / 3;

                
                pixelNovo[0] = processarPixel(pixelOriginal[0], brilho, fatorContraste, dessaturacao, solarizacao, media);
                pixelNovo[1] = processarPixel(pixelOriginal[1], brilho, fatorContraste, dessaturacao, solarizacao, media);
                pixelNovo[2] = processarPixel(pixelOriginal[2], brilho, fatorContraste, dessaturacao, solarizacao, media);

                procModificado.putPixel(x, y, pixelNovo); 
            }
        }

        imagemModificada.setProcessor(procModificado); 
        imagemModificada.show();
        
    }

    // Encapsula todas as transformações em um único pixel
    public int processarPixel(int valor, int brilho, float fatorContraste, double dessaturacao, int solarizacao, int media) {
        valor = aplicarBrilho(valor, brilho); 
        valor = aplicarContraste(valor, fatorContraste); 
        valor = aplicarDessaturacao(valor, dessaturacao, media);
        valor = limitarValorPixel(valor); 
        valor = aplicarSolarizacao(valor, solarizacao); 
        return valor;
    }

    
    public float calcularFatorContraste(int contraste) {
        return (259f * (contraste + 255f)) / (255f * (259f - contraste));
    }

    
    public int aplicarBrilho(int valor, int brilho) {
        return valor + brilho;
    }

    
    public int aplicarContraste(int valor, float fatorContraste) {
        return (int) ((valor - 128) * fatorContraste + 128);
    }


    public int aplicarDessaturacao(int valor, double dessaturacao, int media) {
        if (dessaturacao < 1) {
            valor = (int) (media + ((valor - media) * dessaturacao));
        }
        return valor;
    }

    //Reduz a intensidade da cor, aproximando-a da média.
    public int aplicarSolarizacao(int valor, int limiarSolarizacao) {
        if (valor < limiarSolarizacao) {
            valor = 255 - valor;
        }
        return valor;
    }

    public int limitarValorPixel(int valor) {
        if (valor > 255) return 255;
        if (valor < 0) return 0;
        return valor;
    }
}
