import java.io.RandomAccessFile;
// import java.lang.invoke.ConstantBootstraps;
// import java.io.Console;
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
        RandomAccessFile fin = new RandomAccessFile("index.bin", "r");
        
        long low = 0, high = fin.length() / 12, mid, endereco;
        int id_lido;

        try {
            while (low <= high) {
                mid = ((low + high) / 2);
                fin.seek(mid * 12);
                id_lido = fin.readInt();

                if (id_conta < id_lido) {
                    high = mid - 1;
                }
                else if (id_conta > id_lido) {
                    low = mid + 1;
                }
                else {
                    endereco = fin.readLong();
                    fin.close();
                    return endereco;
                }
            }
        } catch(EOFException err) {
            fin.close();
            return -1;
        }

        fin.close();
        return -1;
    }

    public void create_conta(String nome, String cpf, String cidade, Float saldo_inicial, RandomAccessFile fout, RandomAccessFile fout_id) throws IOException {
        int ultimo_id, proximo_id;
        byte ba[];

        // Le Id do cabecalho (Id do novo objeto)
        fout.seek(0);
        ultimo_id = fout.readInt();

        // Instancia objeto para novo registro
        Contas conta = new Contas(ultimo_id, nome, cpf, cidade, 0, saldo_inicial);
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

        // Converte objeto para vetor de bites
        ba = conta.toByteArray();

        // Escreve registro no arquivo
        fout.writeByte(0);          // Lapide
        fout.writeInt(ba.length);   // Tamanho do registro
        fout.write(ba);             // Conteudo do registro

        imprime_mensagem("Cliente cadastrado com sucesso!\nDados: ");
        System.out.println(conta);
    }

    public void update_conta(Contas conta_atualizada, RandomAccessFile fout, RandomAccessFile fout_id) throws IOException {
        long pos, pos_index = 0;
        int tam_registro;
        byte ba[], conta_novo_registro[];

        Index index = new Index();
        Contas conta = new Contas();

        pos = busca_binaria(conta_atualizada.id_conta);

        try {
            if (pos != -1) {
                fout.seek(pos);
                fout.readByte();
                tam_registro = fout.readInt();

                conta_atualizada.qtd_transferencias = conta.qtd_transferencias;
                conta_novo_registro = conta_atualizada.toByteArray();

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
                    pos = fout.getFilePointer();

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

                long pos = fin.getFilePointer();

                // Se encontrar registro com Id inserido, mostre os dados 
                if(lapide == 0 && conta.id_conta == id_conta) {
                    flag_encontrou = true;
                    System.out.println(pos);
                    System.out.println(tam_registro);
                    System.out.println("\n" + conta);
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

    public void debug(int id_conta, RandomAccessFile fout, RandomAccessFile fout_id) throws IOException {
        byte ba[];
        int tam_registro, lapide;
        long pos;
        Boolean flag_encontrou = false;

        Ordenacao od = new Ordenacao();

        od.ordenacao_externa(fout_id);

        System.out.print("Tamanho do arquivo de indices: "); System.out.println(fout_id.length());

        fout.seek(0);
        fout_id.seek(0);


        // while (true) {
        //     try {
        //         int id = fout_id.readInt();

        //         if (id == id_conta) {
        //             pos = fout_id.readLong();
        //             break;
        //         }
        //         else {
        //             fout_id.readLong();
        //         }
        //     } catch(EOFException err) {
        //         pos = -1;
        //         break;
        //     }

        // }

        Index index = new Index();
        Contas conta = new Contas();

        pos = busca_binaria(id_conta);

        try {
            if (pos != -1) {

                System.out.println("boo");
                System.out.println(pos);

                fout.seek(pos);
                fout.readByte();
                tam_registro = fout.readInt();

                // Le dados do registro
                ba = new byte[tam_registro];
                fout.read(ba);
                conta.fromByteArray(ba);

                //long pos = fin.getFilePointer();

                System.out.println("\n" + conta);
            }
            else {
                imprime_mensagem("Cliente não econtrado!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        // pos = busca_binaria(id_conta);

        //   try {
        //     if (pos != -1) {
        //         System.out.println(pos);
        //         System.out.println("boo");

        //         if (id_conta == 1)
        //             pos++;
                
        //         fout.seek(pos);

        //         lapide = fout.readByte();
        //         tam_registro = fout.readInt();

        //         ba = new byte[tam_registro];
        //         fout.read(ba);
        //         conta.fromByteArray(ba);

        //         System.out.println(conta);
        //     }
        //     else {
        //         imprime_mensagem("Cliente não econtrado!");
        //     }
        // } catch(Exception e) {
        //     e.printStackTrace();
        // }

        // ba = new byte[12];
        // fout_id.read(ba);
        // index.fromByteArray(ba);

        // fout.seek(index.endereco);

        // lapide = fout.readByte();
        // tam_registro = fout.readInt();

        // ba = new byte[tam_registro];
        // fout.read(ba);
        // conta.fromByteArray(ba);

        // while(pos <= fout_id.length()) {
        //     try {
        //         ba = new byte[12];
        //         fout_id.read(ba);
        //         index.fromByteArray(ba);

        //         pos += 12;
                
        //         if(index.id_conta == id_conta) {
        //             flag_encontrou = true;

        //             System.out.println(index.id_conta);
        //             System.out.println(index.endereco);

        //             fout.seek(index.endereco);

        //             lapide = fout.readByte();
        //             tam_registro = fout.readInt();

        //             ba = new byte[tam_registro];
        //             fout.read(ba);
        //             conta.fromByteArray(ba);

        //             System.out.println(conta);

        //             break;
        //         }
        //     }
        //     catch(EOFException err) {
        //         break;
        //     }
        // }

        // if(!flag_encontrou) {
        //     imprime_mensagem("Cliente não encontrado!");
        // }
    }
    

    // public void update_conta(Contas conta_autualizada, RandomAccessFile fout) throws IOException {
    //     long pos;
    //     int tam_registro;
    //     byte lapide, ba[], conta_novo_registro[];
    //     boolean flag_encontrou = false;

    //     Contas conta = new Contas();

    //     // Le Id do ultimo registro
    //     fout.seek(0);
    //     fout.readInt();

    //     // Verifica todos os registros ate encontrar Id inserido
    //     while(true) {
    //         try {
    //             // Le cabecalho do registro
    //             pos = fout.getFilePointer();
    //             lapide = fout.readByte();
    //             tam_registro = fout.readInt();

    //             // Le dados de registro
    //             ba = new byte[tam_registro];
    //             fout.read(ba);
    //             conta.fromByteArray(ba);

    //             if(lapide == 0 && conta.id_conta == conta_autualizada.id_conta) {
    //                 flag_encontrou = true;

    //                 // Atualiza registro e converto para vetor de bytes
    //                 conta_autualizada.qtd_transferencias = conta.qtd_transferencias;
    //                 conta_novo_registro = conta_autualizada.toByteArray();

    //                 // Se tamanho do registro nao aumentou, escreve na mesma posicao
    //                 if(conta_novo_registro.length <= tam_registro) {
    //                     fout.seek(pos);

    //                     fout.readByte();
    //                     fout.readInt();
    //                     fout.write(conta_novo_registro);
    //                 }
    //                 // Se aumentou, deleta antigo registro e escreve novo no final do arquivo
    //                 else{
    //                     fout.seek(pos);
    //                     fout.writeByte(1);

    //                     fout.seek(fout.length());
    //                     fout.writeByte(0);
    //                     fout.writeInt(conta_novo_registro.length);
    //                     fout.write(conta_novo_registro);
    //                 }

    //                 imprime_mensagem("Conta atualizada!\nNovos dados: ");
    //                 System.out.println(conta_autualizada);

    //                 break;
    //             }
    //         }
    //         catch(EOFException err) {
    //             break;
    //         }
    //     }

    //     if(!flag_encontrou) {
    //         imprime_mensagem("Cliente não encontrado!");
    //     }
    // }
}
