import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopiaConteudo {
    // Metodo copia registros de arquivo temporario para arquivo de indices
    public void copia_conteudo(String arq_origem, String arq_destino) {
 
        FileInputStream fis = null;
        FileOutputStream fos = null;
 
        try {
            fis = new FileInputStream(arq_origem);
            fos = new FileOutputStream(arq_destino);
             
            byte[] buffer = new byte[120];
            int numero_bytes = 0;
 
            System.out.println("Copiando dados entre arquivos");
 
            // Lê bytes do arquivo de origem em blocos de até 120 bytes e copia para arquivo de destino
            while ((numero_bytes = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, numero_bytes);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado" + e);
        }
        catch (IOException ioe) {
            System.out.println("Erro ao copiar arquivo " + ioe);
        }
        finally {
            // Fecha arquivos
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error ao fechar aquivos: " + ioe);
            }
        }
    }
}
