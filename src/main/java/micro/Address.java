package micro;

public class Address {
    public static final Address localhost = new Address(new byte[0], 0, 0);

    private byte[] ip;
    private int port;
    private int node;

    public Address(byte[] ip, int port, int node) {
        this.ip = ip;
        this.port = port;
        this.node = node;
    }

    public byte[] getIp() {
        return ip;
    }

    public void setIp(byte[] ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getNode() {
        return node;
    }

    public boolean nodeEqual(Address other){
        //todo
        return true;
    }

}
