import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Index {
    protected int id_conta;
    protected long endereco;

    // Instanciamento de atributos
    public Index(int id_conta, long endereco) {
        this.id_conta = id_conta;
        this.endereco = endereco;
    }

    public Index(int id_conta) {
        this.id_conta = id_conta;
        this.endereco = -1;
    }

    public Index() {
        this.id_conta = -1;
        this.endereco = -1;
    }

    // Metodo converte atributos de objeto para vetor de bytes
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id_conta);
        dos.writeLong(endereco);

        return baos.toByteArray();
    }

    // Metodo tras valores binários de arquivo para serem atribuidos a objetos
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id_conta = dis.readInt();
        endereco = dis.readLong();
    }
}
