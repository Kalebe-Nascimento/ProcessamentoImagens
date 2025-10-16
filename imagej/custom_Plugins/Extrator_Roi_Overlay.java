import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class Extrator_Roi_Overlay implements PlugIn {

    @Override
    public void run(String argumento) {
        File diretorioEntrada = selecionarDiretorio("Selecione o diretório de entrada");
        if (diretorioEntrada == null) return;

        File diretorioSaida = selecionarDiretorio("Selecione o diretório de saída");
        if (diretorioSaida == null) return;

        File[] imagens = listarImagens(diretorioEntrada);
        if (imagens == null || imagens.length == 0) {
            IJ.showMessage("Nenhuma imagem encontrada no diretório de entrada.");
            return;
        }

        for (File imagem : imagens) {
            IJ.log("Processando imagem: " + imagem.getName());
            processarImagem(imagem, diretorioSaida);
        }

        IJ.showMessage("Extração de ROIs concluída!");
    }

    private File selecionarDiretorio(String titulo) {
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle(titulo);
        seletor.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return seletor.showOpenDialog((Component) null) == JFileChooser.APPROVE_OPTION
                ? seletor.getSelectedFile()
                : null;
    }

    private File[] listarImagens(File diretorio) {
        return diretorio.listFiles((dir, nome) -> {
            String nomeArquivo = nome.toLowerCase();
            return nomeArquivo.endsWith(".tif") ||
                   nomeArquivo.endsWith(".gif") ||
                   nomeArquivo.endsWith(".png") ||
                   nomeArquivo.endsWith(".jpg") ||
                   nomeArquivo.endsWith(".jpeg");
        });
    }

    private void processarImagem(File arquivoImagem, File diretorioSaida) {
        
        ImagePlus imagemOriginal = IJ.openImage(arquivoImagem.getAbsolutePath());
        if (imagemOriginal == null) {
            IJ.log("Erro ao abrir imagem: " + arquivoImagem.getName());
            return;
        }

        ImagePlus imagemBinaria = prepararImagemParaROIs(imagemOriginal.duplicate());
        IJ.run(imagemBinaria, "Analyze Particles...", "size=5000-Infinity add");

        RoiManager roiManager = RoiManager.getInstance();
        if (roiManager == null) roiManager = new RoiManager();

        salvarROIs(imagemOriginal, roiManager.getRoisAsArray(), arquivoImagem, diretorioSaida);

        roiManager.reset();
        imagemOriginal.close();
        imagemBinaria.close();
    }

    private ImagePlus prepararImagemParaROIs(ImagePlus imagem) {
        IJ.run(imagem, "Subtract Background...", "rolling=20 light sliding");
        IJ.run(imagem, "Gaussian Blur...", "sigma=5");
        IJ.run(imagem, "8-bit", "");
        IJ.setAutoThreshold(imagem, "Otsu dark");
        IJ.run(imagem, "Convert to Mask", "");
        IJ.run(imagem, "Invert", "");
        IJ.run(imagem, "Fill Holes", "");
        IJ.run(imagem, "Remove Outliers...", "radius=2 threshold=30 which=Bright");
        return imagem;
    }

    private void salvarROIs(ImagePlus imagemOriginal, Roi[] rois, File arquivoImagem, File diretorioSaida) {
        String nomeBase = arquivoImagem.getName().replaceAll("\\.\\w+$", "");

        //quantidade de roi achado
        for (int i = 0; i < rois.length; i++) {
            imagemOriginal.setRoi(rois[i]);
          
            ImageProcessor recorte = imagemOriginal.getProcessor().crop();
           
            ImagePlus roiImagem = new ImagePlus("ROI_" + i, recorte);

            File arquivoDeSaida = new File(diretorioSaida, nomeBase + "_ROI" + (i + 1) + ".png");
            try {
                ImageIO.write(roiImagem.getBufferedImage(), "PNG", arquivoDeSaida);
                IJ.log("ROI salva: " + arquivoDeSaida.getAbsolutePath());
            } catch (IOException e) {
                IJ.log("Erro ao salvar ROI: " + e.getMessage());
            }
        }
    }
}
