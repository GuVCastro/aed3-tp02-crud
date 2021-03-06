import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Cidade {
    protected String cidade;
    protected int id_conta;

    // Instanciamento de atributos
    public Cidade(String cidade, int id_conta) {
        this.cidade = cidade;
        this.id_conta = id_conta;
    }

    public Cidade(int id_conta) {
        this.cidade = "";
        this.id_conta = id_conta;
    }

    public Cidade() {
        this.cidade = "";
        this.id_conta = -1;
    }

    // Metodo converte atributos de objeto para vetor de bytes
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(cidade);
        dos.writeInt(id_conta);

        return baos.toByteArray();
    }

    // Metodo tras valores binários de arquivo para serem atribuidos a objetos
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        cidade = dis.readUTF();
        id_conta = dis.readInt();
    }
}   
