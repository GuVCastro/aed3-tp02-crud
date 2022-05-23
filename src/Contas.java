import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.DecimalFormat;


public class Contas {
    protected int id_conta;
    protected String nome;
    protected String cpf;
    protected String cidade;
    protected int qtd_transferencias;
    protected float saldo;
    DecimalFormat decimal = new DecimalFormat("#,##0.00");

    // Instanciamento de atributos
    public Contas(int id_conta, String nome, String cpf, String cidade, int qtd_transferencias, float saldo) {

        this.id_conta = id_conta;
        this.nome = nome;
        this.cpf = cpf;
        this.cidade = cidade;
        this.qtd_transferencias = qtd_transferencias;
        this.saldo = saldo;
    }

    public Contas(int id_conta, String nome, String cpf, String cidade, float saldo) {

        this.id_conta = id_conta;
        this.nome = nome;
        this.cpf = cpf;
        this.cidade = cidade;
        this.qtd_transferencias = 0;
        this.saldo = saldo;
    }

    public Contas(){

        this.id_conta = -1;
        this.nome = "";
        this.cpf = "";
        this.cidade = "";
        this.qtd_transferencias = 0;
        this.saldo = 0F;
    }

    // Metodo concatena atributos para uma só String
    public String toString() {
        return "Id : " + id_conta +
               "\nNome: " + nome +
               "\nCPF: " + cpf +
               "\nCidade: " + cidade +
               "\nTransferências Realizadas: " + qtd_transferencias +
               "\nSaldo: R$ " + decimal.format(saldo) + "\n";
    }

    // Metodo converte atributos de objeto para vetor de bytes
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id_conta);
        dos.writeUTF(nome);
        dos.writeUTF(cpf);
        dos.writeUTF(cidade);
        dos.writeInt(qtd_transferencias);
        dos.writeFloat(saldo);

        return baos.toByteArray();
    }

    // Metodo tras valores binários de arquivo para serem atribuidos a objetos
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id_conta = dis.readInt();
        nome = dis.readUTF();
        cpf = dis.readUTF();
        cidade = dis.readUTF();
        qtd_transferencias = dis.readInt();
        saldo = dis.readFloat();
    }
}
