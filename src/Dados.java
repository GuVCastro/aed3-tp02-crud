import java.io.RandomAccessFile;
import java.io.EOFException;
import java.io.IOException;
import java.text.DecimalFormat;

public class Dados {
    // Funcao imprime mensagem (String) dentro de moldura
    private static void imprime_mensagem(String mensagem) {
        System.out.println("__________________________________________");
        System.out.println("\t" + mensagem);
        System.out.println("__________________________________________\n");
    }

    public void create_conta(String nome, String cpf, String cidade, Float saldo_inicial, RandomAccessFile fout) throws IOException {
        int ultimo_id, proximo_id;
        byte ba[];

        // Le Id do cabecalho (Id do novo objeto)
        fout.seek(0);
        ultimo_id = fout.readInt();

        // Instancia objeto para novo registro
        Contas conta = new Contas(ultimo_id, nome, cpf, cidade, 0, saldo_inicial);

        // Escreve Id do ultimo registro acrescido de 1 no cabecalho
        fout.seek(0);
        proximo_id = ultimo_id + 1;
        fout.writeInt(proximo_id);

        // Converte objeto para vetor de bites
        ba = conta.toByteArray();
        fout.seek(fout.length());

        // Escreve registro no arquivo
        fout.writeByte(0);          // Lapide
        fout.writeInt(ba.length);   // Tamanho do registro
        fout.write(ba);             // Conteudo do registro

        imprime_mensagem("Cliente cadastrado com sucesso!\nDados: ");
        System.out.println(conta);
    }

    public void update_conta(Contas conta_autualizada, RandomAccessFile fout) throws IOException {
        long pos;
        int tam_registro;
        byte lapide, ba[], conta_novo_registro[];
        boolean flag_encontrou = false;

        Contas conta = new Contas();

        // Le Id do ultimo registro
        fout.seek(0);
        fout.readInt();

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho do registro
                pos = fout.getFilePointer();
                lapide = fout.readByte();
                tam_registro = fout.readInt();

                // Le dados de registro
                ba = new byte[tam_registro];
                fout.read(ba);
                conta.fromByteArray(ba);

                if(lapide == 0 && conta.id_conta == conta_autualizada.id_conta) {
                    flag_encontrou = true;

                    // Atualiza registro e converto para vetor de bytes
                    conta_autualizada.qtd_transferencias = conta.qtd_transferencias;
                    conta_novo_registro = conta_autualizada.toByteArray();

                    // Se tamanho do registro nao aumentou, escreve na mesma posicao
                    if(conta_novo_registro.length <= tam_registro) {
                        fout.seek(pos);

                        fout.readByte();
                        fout.readInt();
                        fout.write(conta_novo_registro);
                    }
                    // Se aumentou, deleta antigo registro e escreve novo no final do arquivo
                    else{
                        fout.seek(pos);
                        fout.writeByte(1);

                        fout.seek(fout.length());
                        fout.writeByte(0);
                        fout.writeInt(conta_novo_registro.length);
                        fout.write(conta_novo_registro);
                    }

                    imprime_mensagem("Conta atualizada!\nNovos dados: ");
                    System.out.println(conta_autualizada);

                    break;
                }
            }
            catch(EOFException err) {
                break;
            }
        }

        if(!flag_encontrou) {
            imprime_mensagem("Cliente não encontrado!");
        }
    }

    public void pesquisa_conta(int id_conta, RandomAccessFile fin) throws IOException {
        byte lapide;
        int tam_registro;
        byte ba[];
        boolean flag_encontrou = false;
        
        Contas conta = new Contas();

        // Le Id do ultimo registro
        fin.seek(0);
        fin.readInt();

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho do registro 
                lapide = fin.readByte();
                tam_registro = fin.readInt();

                // Le dados do registro 
                ba = new byte[tam_registro];
                fin.read(ba);
                conta.fromByteArray(ba);

                // Se encontrar registro com Id inserido, mostre os dados 
                if(lapide == 0 && conta.id_conta == id_conta) {
                    flag_encontrou = true;
                    System.out.println("\n" + conta);
                    break;
                }
            }
            catch(EOFException err) {
                break;
            }
        }

        if(!flag_encontrou){
            imprime_mensagem("Cliente não encontrado!");
        }
    }

    public void delete_conta(int id_conta, RandomAccessFile fout) throws IOException{
        long pos;
        int tam_registro;
        byte lapide, ba[];
        boolean flag_encontrou = false;  
        
        Contas conta = new Contas();

        // Le Id do ultimo registro
        fout.seek(0);
        fout.readInt();

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho do registro 
                pos = fout.getFilePointer();
                lapide = fout.readByte();
                tam_registro = fout.readInt();

                // Le dados do registro 
                ba = new byte[tam_registro];
                fout.read(ba);
                conta.fromByteArray(ba);

                // Se encontrar registro, alterar valor da lapide para 1 
                if(lapide == 0 && conta.id_conta == id_conta) {
                    flag_encontrou = true;

                    fout.seek(pos);
                    fout.writeByte(1);

                    imprime_mensagem("Conta removida!\n Dados removidos:");
                    System.out.println(conta);

                    break;
                }
            }
            catch(EOFException err) {
                break;
            }
        }

        if(!flag_encontrou) {
            imprime_mensagem("Cliente não encontrado!");
        }
    }

    public void transferencia_contas(int id_conta1, int id_conta2, float valor, RandomAccessFile fout) throws IOException {
        long pos_conta1 = -1, pos_conta2 = -1;
        int tam_registro;
        byte lapide, ba[];
        boolean flag1_encontrou = false, flag2_encontrou = false;
        DecimalFormat decimal = new DecimalFormat("#,##0.00");

        Contas conta1 = new Contas(), conta2 = new Contas(), conta = new Contas();

        // Le Id do ultimo registro
        fout.seek(0);
        fout.readInt();

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho de registro
                lapide = fout.readByte();
                tam_registro = fout.readInt();

                // Guarda posicao do registro caso Id correspondente ainda nao foi encontrado
                if(!flag1_encontrou) 
                    pos_conta1 = fout.getFilePointer();
                if(!flag2_encontrou) 
                    pos_conta2 = fout.getFilePointer();

                // Le dados do registro 
                ba = new byte[tam_registro];
                fout.read(ba);
                conta.fromByteArray(ba);

                // Registra dados em objeto correspondente se Id for encontrado
                if(lapide == 0) {
                    if(conta.id_conta == id_conta1) {
                        flag1_encontrou = true;

                        conta1.fromByteArray(ba);
                    }
                    else if(conta.id_conta == id_conta2) {
                        flag2_encontrou = true;

                        conta2.fromByteArray(ba);
                    }
                }

                if(flag1_encontrou && flag2_encontrou) break;
            }
            catch(EOFException err) {
                break;
            }
        }

        // Registros de ambas as contas devem ser encontrados
        if(!(flag1_encontrou && flag2_encontrou)) {
            imprime_mensagem("Cliente não encontrado!");       
        }
        else {
            // Atualiza saldo e qtd de transferencias das contas 
            conta1.saldo -= valor;
            conta2.saldo += valor;
            conta1.qtd_transferencias++;
            conta2.qtd_transferencias++;

            // Escreve valores atualizados no arquivo
            fout.seek(pos_conta1);
            fout.write(conta1.toByteArray());
            fout.seek(pos_conta2);
            fout.write(conta2.toByteArray());

            imprime_mensagem("Transferência realizada!\nDados: ");
            System.out.println("Cliente (origem): " + conta1.nome + "\nValor tranferido: R$ " + decimal.format(valor) + "\nCliente (destino): " + conta2.nome);
        }
    }
}
