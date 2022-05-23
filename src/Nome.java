import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Nome {
    protected String nome;
    protected int id_conta;

    public Nome(String nome, int id_conta) {
        this.nome = nome;
        this.id_conta = id_conta;
    }

    public Nome(int id_conta) {
        this.nome = "";
        this.id_conta = id_conta;
    }

    public Nome() {
        this.nome = "";
        this.id_conta = -1;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(nome);
        dos.writeInt(id_conta);

        return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        nome = dis.readUTF();
        id_conta = dis.readInt();
    }
}   
