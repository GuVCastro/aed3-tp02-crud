import java.io.RandomAccessFile;
import java.util.Scanner;

public class Main {
    // Funcao imprime mensagem (String) dentro de moldura
    private static void imprime_mensagem(String mensagem) {
        System.out.println("__________________________________________");
        System.out.println("\t" + mensagem);
        System.out.println("__________________________________________\n");
    }

    public static void main(String[] args) {
        // Variaveis relacionadas a io
        Scanner input;
        RandomAccessFile db;
        Dados dados;

        // Objeto que recebe dados da conta atualizada
        Contas conta_atualizada;
        
        // Variaveis que recebem input do usuario
        int id_conta;
        String nome, cpf, cidade;
        float saldo;

        int op;

        try{
            // Inicializa variaveis e instancia objetos
            op = 0;
            input = new Scanner(System.in);
            db = new RandomAccessFile("contas.bin", "rw");
            dados = new Dados();

            // Escreve 0 no cabeçalho se arquivo é novo
            try {
                db.readInt();
            }
            catch (Exception e) {
                db.writeInt(0);
            }

            do {
                // Menu principal
                System.out.println("_________________________________________");
                System.out.println("      Conta Bancária - TP01 - AED3      ");
                System.out.println(" Por:   Gustavo Valadares Castro,       ");
                System.out.println("        Matheus Crivellari Bueno Jorge  ");
                System.out.println("_________________________________________");
                System.out.println(" Opção 1  Cadastrar Conta               ");
                System.out.println(" Opção 2  Atualizar Conta               ");
                System.out.println(" Opção 3  Buscar Conta                  ");
                System.out.println(" Opção 4  Deletar Conta                 ");
                System.out.println(" Opção 5  Realizar Transferência        ");
                System.out.println(" Opção 0  Encerrar Programa             ");
                System.out.println("_________________________________________\n");
                System.out.print("Digite uma Opção: ");

                op = input.nextByte();
                input.nextLine();

                // Metodos estao dispostos na mesma ordem do menu principal 
                switch (op) {
                    case 1:
                        imprime_mensagem("CADASTRO DE CLIENTE");

                        System.out.print("Insira o Nome: ");
                        nome = input.nextLine();
                        System.out.print("Insira o CPF: ");
                        cpf = input.nextLine();
                        System.out.print("Insira a Cidade: ");
                        cidade = input.nextLine();
                        System.out.print("Insira o Saldo Inicial: ");
                        saldo = input.nextFloat();

                        dados.create_conta(nome, cpf, cidade, saldo, db);
                        break;

                    case 2:
                        imprime_mensagem("ATUALIZAR CONTA");

                        System.out.print("Insira o Id do cliente: ");
                        id_conta = input.nextInt();
                        input.nextLine();
                        System.out.print("Insira o Nome: ");
                        nome = input.nextLine();
                        System.out.print("Insira o CPF: ");
                        cpf = input.nextLine();
                        System.out.print("Insira a Cidade: ");
                        cidade = input.nextLine();
                        System.out.print("Insira o Saldo: ");
                        saldo = input.nextFloat();

                        conta_atualizada = new Contas(id_conta, nome, cpf, cidade, saldo);

                        dados.update_conta(conta_atualizada, db);
                        break;

                    case 3:
                        imprime_mensagem("PESQUISA");

                        System.out.print("Insira o Id do cliente: ");
                        id_conta = input.nextInt();

                        dados.pesquisa_conta(id_conta, db);
                        break;

                    case 4:
                        imprime_mensagem("DELETAR CONTA");

                        System.out.print("Insira o Id do cliente: ");
                        id_conta = input.nextInt();

                        dados.delete_conta(id_conta, db);
                        break;

                    case 5:
                        imprime_mensagem("TRANSFERENCIA");

                        System.out.print("Insira o Id do cliente de origem: ");
                        int id_conta1 = input.nextInt();
                        input.nextLine();
                        System.out.print("Insira o Id do cliente de destino: ");
                        int id_conta2 = input.nextInt();
                        input.nextLine();
                        System.out.print("Insira o valor: ");
                        float valor = input.nextFloat();

                        dados.transferencia_contas(id_conta1, id_conta2, valor, db);
                        break;

                    case 0:
                        imprime_mensagem("PROGRAMA ENCERRADO");
                        break;

                    default:
                        imprime_mensagem("OPCAO INVALIDA");
                        break;
                }
            } while (op != 0);

            db.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
