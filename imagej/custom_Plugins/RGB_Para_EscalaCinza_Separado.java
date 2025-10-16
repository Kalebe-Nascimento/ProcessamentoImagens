import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.IJ;
import ij.process.ImageProcessor;


public class RGB_Para_EscalaCinza_Separado implements PlugIn {

    @Override
    public void run(String arg) {
        //starta o id da imagem colorida aberta
        ImagePlus imagemColorida = IJ.getImage();
        
    
        if (imagemColorida.getType() == ImagePlus.COLOR_RGB) {
            separarCanais(imagemColorida);
        } else {
            IJ.error("A imagem não está no formato RGB.");
        }
    }
  
    public void separarCanais(ImagePlus imagemRGB) {
       
        ImageProcessor processadorRGB = imagemRGB.getProcessor();
        int largura = imagemRGB.getWidth();
        int altura = imagemRGB.getHeight();

  
        ImagePlus canalR = IJ.createImage("Canal_R", "8-bit", largura, altura, 1);
        ImagePlus canalG = IJ.createImage("Canal_G", "8-bit", largura, altura, 1);
        ImagePlus canalB = IJ.createImage("Canal_B", "8-bit", largura, altura, 1);

        //starta  processador imagens de entrada para leitura dos pixels por cada canal por processor
        ImageProcessor procR = canalR.getProcessor();
        ImageProcessor procG = canalG.getProcessor();
        ImageProcessor procB = canalB.getProcessor();

        //array para armazenar os valores RGB temporariamente de cada canal 
        int[] rgbTemp = new int[3];
        int intensidade;

       
        for (int col = 0; col < largura; col++) {
            for (int lin = 0; lin < altura; lin++) {
                rgbTemp = processadorRGB.getPixel(col, lin, rgbTemp);

                intensidade = rgbTemp[0];
                procR.putPixel(col, lin, intensidade);

                intensidade = rgbTemp[1];
                procG.putPixel(col, lin, intensidade);

                intensidade = rgbTemp[2];
                procB.putPixel(col, lin, intensidade);
            }
        }

        

        canalR.setProcessor(procR);
        canalG.setProcessor(procG);
        canalB.setProcessor(procB);

        canalR.show();
        canalG.show();
        canalB.show();
    }
}

