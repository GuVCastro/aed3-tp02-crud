import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Ordenacao {
    public void ordenacao_externa(RandomAccessFile index_file) throws IOException {
        RandomAccessFile arq_tmp1 = new RandomAccessFile("arq_temp1.bin", "rw");
        RandomAccessFile arq_tmp2 = new RandomAccessFile("arq_temp2.bin", "rw");

        new RandomAccessFile("arq_temp1.bin", "rw").setLength(0);
        new RandomAccessFile("arq_temp2.bin", "rw").setLength(0);

        if (index_file.length() < 12)
            return;

        ArrayList<Index> lista_index = new ArrayList<>();
        byte ba[];
        int registros_inseridos = 0, troca_arq = 0;
        long bytes_restantes = index_file.length(), tam_arquivo = index_file.length(), tam_distribuido = 0;

        index_file.seek(0);

        while(index_file.getFilePointer() < tam_arquivo) {
            try {
                Index index = new Index();

                ba = new byte[12];
                index_file.read(ba);
                index.fromByteArray(ba);

                bytes_restantes -= 12;

                if (index.id_conta != -1) {
                    lista_index.add(index);
                    registros_inseridos++;
                }

                if (registros_inseridos >= 10 || bytes_restantes == 0) {
                    lista_index.sort((o1, o2) -> o1.id_conta == o2.id_conta ? 0 : (o1.id_conta > o2.id_conta ? 1 : -1));

                    if (troca_arq == 0) {
                        arq_tmp1.seek(arq_tmp1.length());

                        for (int k = 0; k < lista_index.size(); k++) {
                            index = lista_index.get(k);

                            ba = index.toByteArray();
                            arq_tmp1.write(ba);
                        }

                        lista_index.clear();
                        troca_arq = 1;
                    }
                    else {
                        arq_tmp2.seek(arq_tmp2.length());

                        for (int k = 0; k < lista_index.size(); k++) {
                            index = lista_index.get(k);

                            ba = index.toByteArray();
                            arq_tmp2.write(ba);
                        }

                        lista_index.clear();
                        troca_arq = 0;
                    }

                    registros_inseridos = 0;
                }
            }
            catch(EOFException err) {
                break;
            }
        }

        index_file.setLength(0);
        arq_tmp1.seek(0);
        arq_tmp2.seek(0);

        tam_distribuido = (arq_tmp1.length() + arq_tmp2.length())/12;

        try {
            for (int k = 0; k < arq_tmp1.length()/12; k++) {
                Index index = new Index();
            
                ba = new byte[12];
                arq_tmp1.read(ba);
                index.fromByteArray(ba);

                lista_index.add(index);
            }

            for (int k = 0; k < arq_tmp2.length()/12; k++) {
                Index index = new Index();
            
                ba = new byte[12];
                arq_tmp2.read(ba);
                index.fromByteArray(ba);

                lista_index.add(index);

                lista_index.sort((o1, o2) -> o1.id_conta == o2.id_conta ? 0 : (o1.id_conta > o2.id_conta ? 1 : -1));
            }

            for (int k = 0; k < tam_distribuido; k++) {
                Index index = new Index();

                index = lista_index.get(k);

                ba = index.toByteArray();
                index_file.write(ba);
            }
        }
        catch(EOFException err) {
            arq_tmp1.close();
            arq_tmp2.close();
            return;
        }

        arq_tmp1.close();
        arq_tmp2.close();
    }
}