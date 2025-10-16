import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.WindowManager;
import ij.IJ;
import ij.process.ImageProcessor;

public class Juntar_Canais_Para_RGB implements PlugIn {

    @Override
    public void run(String arg) {
     
        if (WindowManager.getWindowCount() == 3) {
            //faz um array para definir as imagens abertas na ordem RGB
            int[] listaIDs = WindowManager.getIDList();

            //starta as imagens na ordem RGB
            ImagePlus canalR = WindowManager.getImage(listaIDs[0]);
            ImagePlus canalG = WindowManager.getImage(listaIDs[1]);
            ImagePlus canalB = WindowManager.getImage(listaIDs[2]);

          
            if (canalR.getType() == ImagePlus.GRAY8 &&
                canalG.getType() == ImagePlus.GRAY8 &&
                canalB.getType() == ImagePlus.GRAY8) {

                combinarCanais(canalR, canalG, canalB);

            } else {
                IJ.error("Todas as imagens devem estar em 8-bits (escala de cinza).");
            }

        } else {
            IJ.error("Você deve abrir exatamente 3 imagens.");
        }
    }

  
    public void combinarCanais(ImagePlus imgR, ImagePlus imgG, ImagePlus imgB) {
         //starta valores imagens de entrada para leitura dos pixels por processor
        ImageProcessor procR = imgR.getProcessor();
        ImageProcessor procG = imgG.getProcessor();
        ImageProcessor procB = imgB.getProcessor();

        int largura = imgR.getWidth();
        int altura = imgR.getHeight();

        //starta a criacao de imagem RGB
        ImagePlus imagemRGB = IJ.createImage("Imagem_Combinada_RGB", "RGB", largura, altura, 1);
        ImageProcessor procRGB = imagemRGB.getProcessor();

         //arrays para armazenar valores de pixel se é colorido ou RGB
        int[] pixelCinza = new int[1];
        int[] pixelRGB = new int[3];

        
        for (int col = 0; col < largura; col++) {
            for (int lin = 0; lin < altura; lin++) {
                pixelCinza = procR.getPixel(col, lin, pixelCinza);
                pixelRGB[0] = pixelCinza[0];

                pixelCinza = procG.getPixel(col, lin, pixelCinza);
                pixelRGB[1] = pixelCinza[0];

                pixelCinza = procB.getPixel(col, lin, pixelCinza);
                pixelRGB[2] = pixelCinza[0];

                procRGB.putPixel(col, lin, pixelRGB);
            }
        }
 
        imagemRGB.setProcessor(procRGB);
        imagemRGB.show();
    }
}

