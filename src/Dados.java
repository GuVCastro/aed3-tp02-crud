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

    public long busca_binaria(int id_conta) throws IOException {
        // Abre arq e indices com permissao de leitura
        RandomAccessFile fin = new RandomAccessFile("index.bin", "r");
        
        // Variaveis relacionadas a busca binaria
        long esq = 0, dir = fin.length() / 12, meio, endereco;
        int id_lido;

        try {
            while (esq <= dir) {
                meio = ((esq + dir) / 2);
                fin.seek(meio * 12);        // Divisao por 12 ocorre porque registros individuais tem 12 bytes
                id_lido = fin.readInt();

                if (id_conta < id_lido) {
                    dir = meio - 1;
                }
                else if (id_conta > id_lido) {
                    esq = meio + 1;
                }
                else {
                    // Se endereco e encontrado, leia, feche o arq e retorne seu valor
                    endereco = fin.readLong();
                    fin.close();
                    return endereco;
                }
            }
        } catch(EOFException err) {
            // Fecha arquivo e retorna -1 em caso de erro
            fin.close();
            return -1;
        }

        // Fecha arquivo e retorna -1 caso endereco nao seja encontrado
        fin.close();
        return -1;
    }

    public void create_conta(String nome, String cpf, String cidade, Float saldo_inicial, RandomAccessFile fout, RandomAccessFile fout_id) throws IOException {
        int ultimo_id, proximo_id;
        byte ba[];

        // Le Id do cabecalho (Id do novo objeto)
        fout.seek(0);
        ultimo_id = fout.readInt();

        // Instancia objeto para novo registro no arq de dados
        Contas conta = new Contas(ultimo_id, nome, cpf, cidade, 0, saldo_inicial);

        // Instancia objeto para novo registro no arq de indice
        Index index = new Index(ultimo_id);

        // Escreve Id do ultimo registro acrescido de 1 no cabecalho
        fout.seek(0);
        proximo_id = ultimo_id + 1;
        fout.writeInt(proximo_id);

        fout.seek(fout.length());
        
        index.endereco = fout.getFilePointer();

        ba = index.toByteArray();
        fout_id.seek(fout_id.length());
        fout_id.write(ba);

        // Converte objeto conta para vetor de bites
        ba = conta.toByteArray();

        // Escreve registro no arquivo de dados
        fout.writeByte(0);          // Lapide
        fout.writeInt(ba.length);   // Tamanho do registro
        fout.write(ba);             // Conteudo do registro

        
        // Insere nome do cliente e id no arquivo de nomes
        RandomAccessFile fout_nome = new RandomAccessFile("nome.bin", "rw");
        Nome registro_nome = new Nome();

        registro_nome.nome = nome;
        registro_nome.id_conta = ultimo_id;

        ba = registro_nome.toByteArray();

        fout_nome.seek(fout_nome.length());
        fout_nome.writeInt(ba.length);

        fout_nome.write(ba);

         // Insere cidade do cliente e id no arquivo de cidades
         RandomAccessFile fout_cidade = new RandomAccessFile("cidade.bin", "rw");
         Cidade registro_cidade = new Cidade();
 
         registro_cidade.cidade = cidade;
         registro_cidade.id_conta = ultimo_id;
 
         ba = registro_cidade.toByteArray();
 
         fout_cidade.seek(fout_cidade.length());
         fout_cidade.writeInt(ba.length);
 
         fout_cidade.write(ba);
 
         // Exibe dados da conta criada
         imprime_mensagem("Cliente cadastrado com sucesso!\nDados: ");
         System.out.println(conta);
    }

    public void update_conta(Contas conta_atualizada, RandomAccessFile fout, RandomAccessFile fout_id) throws IOException {
        long pos, pos_index = 0;
        int tam_registro;
        byte ba[], conta_novo_registro[];

        // Instancia objetos relacionados ao arq de indices, dados e objeto responsável pela ordenação externa
        Index index = new Index();
        Contas conta = new Contas();
        Ordenacao od = new Ordenacao();

        // Realiza ordenacao externa do arquivo de indices
        od.ordenacao_externa(fout_id);

        // Realiza busca binária
        pos = busca_binaria(conta_atualizada.id_conta);

        try {
            // Se busca encontra endereco, altera dados
            if (pos != -1) {
                fout.seek(pos);
                fout.readByte();
                tam_registro = fout.readInt();

                conta_atualizada.qtd_transferencias = conta.qtd_transferencias;
                conta_novo_registro = conta_atualizada.toByteArray();

                // Se tamanho do registro nao aumentou, escreve na mesma posicao no arq de dados
                if(conta_novo_registro.length <= tam_registro) {
                    fout.seek(pos);

                    fout.readByte();
                    fout.readInt();
                    fout.write(conta_novo_registro);
                }
                // Se aumentou, deleta antigo registro e escreve novo no final do arq de dados
                else{
                    // Escreve lápide no registro antigo
                    fout.seek(pos);
                    fout.writeByte(1);

                    fout.seek(fout.length());
                    pos = fout.getFilePointer();

                    // Altera endereco do registro no arq de indices
                    while(pos_index <= fout_id.length()) {
                        try {
                            ba = new byte[12];
                            fout_id.read(ba);
                            index.fromByteArray(ba);
            
                            if(index.id_conta == conta_atualizada.id_conta) {
                                fout_id.seek(pos_index);
                                index.id_conta = -1;
                                ba = index.toByteArray();
                                fout_id.write(ba);

                                fout_id.seek(fout_id.length());
                                index.id_conta = conta_atualizada.id_conta;
                                index.endereco = pos;
                                ba = index.toByteArray();
                                fout_id.write(ba);

                                break;
                            }

                            pos_index += 12;
                        }
                        catch(EOFException err) {
                            break;
                        }
                    }

                    // Escreve registro no final do arq de dados
                    fout.writeByte(0);
                    fout.writeInt(conta_novo_registro.length);
                    fout.write(conta_novo_registro);
                }

                imprime_mensagem("Conta atualizada!\nNovos dados: ");
                System.out.println(conta_atualizada);
            }
            else {
                imprime_mensagem("Cliente não econtrado!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void pesquisa_conta(int id_conta, RandomAccessFile fin, RandomAccessFile fin_id) throws IOException {
        byte ba[];
        int tam_registro;
        long pos;

        // Instancia objetos relacionados ao arq de dados e objeto responsável pela ordenação externa
        Contas conta = new Contas();
        Ordenacao od = new Ordenacao();

        // Realiza ordenacao externa do arq de indices
        od.ordenacao_externa(fin_id);

        // Realiza busca binária
        pos = busca_binaria(id_conta);

        // Se busca encontra endereco, lê dados do registro no arq de dados
        try {
            if (pos != -1) {
                fin.seek(pos);
                fin.readByte();
                tam_registro = fin.readInt();

                // Le dados do registro
                ba = new byte[tam_registro];
                fin.read(ba);
                conta.fromByteArray(ba);

                System.out.println("\n" + conta);
            }
            else {
                imprime_mensagem("Cliente não econtrado!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void delete_conta(int id_conta, RandomAccessFile fout, RandomAccessFile fout_id) throws IOException{
        long pos, pos_index = 0;
        int tam_registro;
        byte lapide, ba[];
        
        // Instancia objetos relacionados ao arq de indices, dados e objeto responsável pela ordenação externa
        Index index = new Index();
        Contas conta = new Contas();
        Ordenacao od = new Ordenacao();

        // Realiza ordenacao externa do arq de indices
        od.ordenacao_externa(fout_id);

        // Realiza busca binária
        pos = busca_binaria(id_conta);

        // Se busca encontra endereco, remove registro no arq de dados e insere -1 no campo id do arq de indices
        if (pos != -1) {
            fout.seek(pos);
            fout.readByte();
            tam_registro = fout.readInt();

            ba = new byte[tam_registro];
            fout.read(ba);
            conta.fromByteArray(ba);

            fout.seek(pos);
            fout.writeByte(1);

            // Remove no arq de indices
            while(pos_index <= fout_id.length()) {
                try {
                    ba = new byte[12];
                    fout_id.read(ba);
                    index.fromByteArray(ba);
    
                    if(index.id_conta == id_conta) {
                        fout_id.seek(pos_index);
                        index.id_conta = -1;
                        ba = index.toByteArray();
                        fout_id.write(ba);

                        imprime_mensagem("Conta removida!\n Dados removidos:");
                        System.out.println(conta);

                        break;
                    }

                    pos_index += 12;
                }
                catch(EOFException err) {
                    break;
                }
            }
        }
        else {
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
                // Le cabecalho de registro no arq de dados
                lapide = fout.readByte();
                tam_registro = fout.readInt();

                // Guarda posicao do registro caso Id correspondente ainda nao foi encontrado
                if(!flag1_encontrou) 
                    pos_conta1 = fout.getFilePointer();
                if(!flag2_encontrou) 
                    pos_conta2 = fout.getFilePointer();

                // Le dados do registro no arq de dados
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

    public void pesquisar_por_nome(String nome) throws IOException {
        byte ba[];
        int tam_nome;

        Nome registro_nome = new Nome();
        RandomAccessFile fin = new RandomAccessFile("nome.bin", "r");

        fin.seek(0);

        while (true) {
            try {
                tam_nome = fin.readInt();
                ba = new byte[tam_nome];

                fin.read(ba);
                registro_nome.fromByteArray(ba);

                if (registro_nome.nome.equals(nome)) {
                    System.out.print("Id encontrado: " + registro_nome.id_conta);
                    break;
                }
            }
            catch(EOFException err) {
                imprime_mensagem("Id correspondente não encontrado");
                break;
            }
        }    
    }
    
    public void pesquisar_por_cidade(String cidade) throws IOException {
        byte ba[];
        int tam_cidade;

        Cidade registro_cidade = new Cidade();
        RandomAccessFile fin = new RandomAccessFile("cidade.bin", "r");

        fin.seek(0);

        while (true) {
            try {
                tam_cidade = fin.readInt();
                ba = new byte[tam_cidade];

                fin.read(ba);
                registro_cidade.fromByteArray(ba);

                if (registro_cidade.cidade.equals(cidade)) {
                    System.out.print("Id encontrado: " + registro_cidade.id_conta);
                    break;
                }
            }
            catch(EOFException err) {
                imprime_mensagem("Id correspondente não encontrado");
                break;
            }
        }    
    } 
}
